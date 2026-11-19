package id.casan.monitor.domain.model

data class Incident(
    val id: String,
    val title: String,
    val description: String,
    val source: String,
    val severity: IncidentSeverity,
    val status: IncidentStatus,
    val occurredAt: Long,
)

enum class IncidentSeverity { CRITICAL, HIGH, INFO }
enum class IncidentStatus { OPEN, ACKNOWLEDGED, RESOLVED }

data class MonitoringSnapshot(
    val incidents: List<Incident>,
    val updatedAt: Long,
)
