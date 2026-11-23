package id.casan.monitor.domain.update

import java.io.IOException

/** Maps an approved metadata source into the Android version policy. */
fun interface UpdateMetadataSource {
    suspend fun fetch(): UpdateMetadataResult
}

sealed interface UpdateMetadataResult {
    data class Available(val release: InternalRelease) : UpdateMetadataResult
    data object Offline : UpdateMetadataResult
    data object Unavailable : UpdateMetadataResult
}

data class InternalRelease(
    val versionCode: Long,
    val versionName: String,
    val minimumSupportedVersionCode: Long? = null,
    val releaseNotes: String = "",
)

sealed interface UpdateStatus {
    data object UpToDate : UpdateStatus
    data class Available(val release: InternalRelease) : UpdateStatus
    data class Required(val release: InternalRelease) : UpdateStatus
    data object UnableToCheck : UpdateStatus
    data object Offline : UpdateStatus
}

class UpdateChecker(private val source: UpdateMetadataSource) {
    suspend fun check(installedVersionCode: Long): UpdateStatus {
        if (installedVersionCode <= 0) return UpdateStatus.UnableToCheck
        val result = try {
            source.fetch()
        } catch (_: IOException) {
            return UpdateStatus.UnableToCheck
        }
        return when (result) {
            UpdateMetadataResult.Offline -> UpdateStatus.Offline
            UpdateMetadataResult.Unavailable -> UpdateStatus.UnableToCheck
            is UpdateMetadataResult.Available -> classify(installedVersionCode, result.release)
        }
    }

    private fun classify(installedVersionCode: Long, release: InternalRelease): UpdateStatus {
        val minimum = release.minimumSupportedVersionCode
        return when {
            release.versionCode <= 0 || release.versionName.isBlank() -> UpdateStatus.UnableToCheck
            minimum != null && minimum !in 1..release.versionCode -> UpdateStatus.UnableToCheck
            installedVersionCode >= release.versionCode -> UpdateStatus.UpToDate
            minimum != null && installedVersionCode < minimum -> UpdateStatus.Required(release)
            else -> UpdateStatus.Available(release)
        }
    }
}
