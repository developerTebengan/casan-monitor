package id.casan.monitor.data.repository

import id.casan.monitor.data.local.MockIncidents
import id.casan.monitor.domain.repository.IncidentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockIncidentRepository @Inject constructor() : IncidentRepository {
    private val sample = MockIncidents.snapshot()
    override suspend fun snapshot() = sample
}
