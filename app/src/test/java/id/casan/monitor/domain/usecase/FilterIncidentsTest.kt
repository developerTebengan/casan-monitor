package id.casan.monitor.domain.usecase

import id.casan.monitor.data.local.MockIncidents
import id.casan.monitor.domain.model.IncidentSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterIncidentsTest {
    private val incidents = MockIncidents.snapshot(now = 200_000_000L).incidents

    @Test
    fun blankSearchWithAllSeveritiesPreservesOrder() {
        assertEquals(incidents, filterIncidents(incidents, "  ", null))
    }

    @Test
    fun searchMatchesSourceCaseInsensitivelyAndCombinesWithSeverity() {
        val results = filterIncidents(incidents, " payments ", IncidentSeverity.CRITICAL)
        assertEquals(listOf("1", "6"), results.map { it.id })
        assertTrue(filterIncidents(incidents, "payments", IncidentSeverity.HIGH).isEmpty())
    }
}
