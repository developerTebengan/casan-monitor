package id.casan.monitor.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.casan.monitor.domain.model.Incident
import id.casan.monitor.domain.model.IncidentSeverity
import id.casan.monitor.domain.model.IncidentStatus
import id.casan.monitor.domain.repository.IncidentRepository
import id.casan.monitor.domain.usecase.filterIncidents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MonitoringState {
    data object Loading : MonitoringState
    data object Error : MonitoringState
    data class Content(
        val incidents: List<Incident>,
        val updatedAt: Long,
    ) : MonitoringState {
        val recent = incidents.take(4)
        val resolved = incidents.filter { it.status == IncidentStatus.RESOLVED }
        val criticalCount = incidents.count {
            it.status != IncidentStatus.RESOLVED && it.severity == IncidentSeverity.CRITICAL
        }
        val highCount = incidents.count {
            it.status != IncidentStatus.RESOLVED && it.severity == IncidentSeverity.HIGH
        }
    }
}

data class AlertSearchState(
    val query: String = "",
    val severity: IncidentSeverity? = null,
    val incidents: List<Incident> = emptyList(),
)

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val repository: IncidentRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow<MonitoringState>(MonitoringState.Loading)
    val state = mutableState.asStateFlow()
    val alertSearch = combine(
        state,
        savedState.getStateFlow("alert_query", ""),
        savedState.getStateFlow<String?>("alert_severity", null),
    ) { monitoring, query, severityName ->
        val severity = IncidentSeverity.entries.firstOrNull { it.name == severityName }
        val incidents = (monitoring as? MonitoringState.Content)?.incidents.orEmpty()
        AlertSearchState(query, severity, filterIncidents(incidents, query, severity))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AlertSearchState())

    fun updateAlertQuery(query: String) {
        savedState["alert_query"] = query
    }

    fun updateAlertSeverity(severity: IncidentSeverity?) {
        savedState["alert_severity"] = severity?.name
    }

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            mutableState.value = MonitoringState.Loading
            try {
                val snapshot = repository.snapshot()
                mutableState.value = MonitoringState.Content(
                    snapshot.incidents.sortedByDescending(Incident::occurredAt),
                    snapshot.updatedAt,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.value = MonitoringState.Error
            }
        }
    }
}
