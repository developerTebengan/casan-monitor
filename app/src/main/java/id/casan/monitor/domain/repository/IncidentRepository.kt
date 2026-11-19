package id.casan.monitor.domain.repository

import id.casan.monitor.domain.model.MonitoringSnapshot

interface IncidentRepository {
    suspend fun snapshot(): MonitoringSnapshot
}
