package id.casan.monitor.data.local

import id.casan.monitor.domain.model.Incident
import id.casan.monitor.domain.model.IncidentSeverity.*
import id.casan.monitor.domain.model.IncidentStatus.*
import id.casan.monitor.domain.model.MonitoringSnapshot

object MockIncidents {
    fun snapshot(now: Long = System.currentTimeMillis()) = MonitoringSnapshot(
        updatedAt = now,
        incidents = listOf(
            Incident("1", "Charging Refund Failed", "The payment gateway rejected a charging session refund.", "Payments", CRITICAL, OPEN, now - 120_000),
            Incident("2", "SPKLU Device Offline", "The station controller missed its latest heartbeat.", "Station Telemetry", HIGH, OPEN, now - 900_000),
            Incident("3", "OCPP Connection Timeout", "The charger did not respond within the connection window.", "OCPP Gateway", HIGH, ACKNOWLEDGED, now - 2_400_000),
            Incident("4", "Charging Session Completed", "The session closed and metering data was recorded.", "Charging Sessions", INFO, RESOLVED, now - 3_600_000),
            Incident("5", "Meter Reading Delayed", "The pending meter readings were processed successfully.", "Station Telemetry", HIGH, RESOLVED, now - 86_400_000),
            Incident("6", "Payment Webhook Restored", "Payment events resumed after the retry queue drained.", "Payments", CRITICAL, RESOLVED, now - 172_800_000),
        ),
    )
}
