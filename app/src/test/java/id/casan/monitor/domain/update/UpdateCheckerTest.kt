package id.casan.monitor.domain.update

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class UpdateCheckerTest {
    private val release = InternalRelease(10, "0.2.0", 8, "Local fixture")
    private val checker = UpdateChecker { UpdateMetadataResult.Available(release) }

    @Test
    fun comparesNumericCodesAndDoesNotSuggestDowngrades() = runBlocking {
        assertEquals(UpdateStatus.Available(release), checker.check(9))
        assertEquals(UpdateStatus.UpToDate, checker.check(10))
        assertEquals(UpdateStatus.UpToDate, checker.check(11))
    }

    @Test
    fun requiresUpgradeOnlyBelowAnExplicitValidMinimum() = runBlocking {
        assertEquals(UpdateStatus.Required(release), checker.check(7))
        assertEquals(UpdateStatus.Available(release), checker.check(8))
        val optional = release.copy(minimumSupportedVersionCode = null)
        val optionalChecker = UpdateChecker { UpdateMetadataResult.Available(optional) }
        assertEquals(UpdateStatus.Available(optional), optionalChecker.check(1))
    }

    @Test
    fun rejectsInvalidMetadataWithoutDemandingAnUpdate() = runBlocking {
        val invalidReleases = listOf(
            release.copy(versionCode = 0),
            release.copy(versionName = " "),
            release.copy(minimumSupportedVersionCode = 0),
            release.copy(minimumSupportedVersionCode = 11),
        )
        for (invalid in invalidReleases) {
            val invalidChecker = UpdateChecker { UpdateMetadataResult.Available(invalid) }
            assertEquals(UpdateStatus.UnableToCheck, invalidChecker.check(1))
        }
        assertEquals(UpdateStatus.UnableToCheck, checker.check(0))
    }

    @Test
    fun distinguishesOfflineFromAnUnavailableSource() = runBlocking {
        val offline = UpdateChecker { UpdateMetadataResult.Offline }
        val unavailable = UpdateChecker { UpdateMetadataResult.Unavailable }
        val ioFailure = UpdateChecker { throw IOException("Local fixture") }
        assertEquals(UpdateStatus.Offline, offline.check(1))
        assertEquals(UpdateStatus.UnableToCheck, unavailable.check(1))
        assertEquals(UpdateStatus.UnableToCheck, ioFailure.check(1))
    }

    @Test
    fun preservesCallerCancellation() = runBlocking {
        val cancelled = CancellationException("Local fixture")
        val cancelledChecker = UpdateChecker { throw cancelled }
        val failure = runCatching { cancelledChecker.check(1) }.exceptionOrNull()
        assertSame(cancelled, failure)
    }
}
