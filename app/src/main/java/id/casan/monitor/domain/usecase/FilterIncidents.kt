package id.casan.monitor.domain.usecase

import id.casan.monitor.domain.model.Incident
import id.casan.monitor.domain.model.IncidentSeverity

fun filterIncidents(
    incidents: List<Incident>,
    query: String,
    severity: IncidentSeverity?,
): List<Incident> {
    val term = query.trim()
    return incidents.filter { incident ->
        (severity == null || incident.severity == severity) &&
            (incident.title.contains(term, ignoreCase = true) ||
                incident.source.contains(term, ignoreCase = true) ||
                incident.description.contains(term, ignoreCase = true))
    }
}
