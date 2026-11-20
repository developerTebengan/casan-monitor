package id.casan.monitor.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import id.casan.monitor.BuildConfig
import id.casan.monitor.domain.model.MonitorAccount
import id.casan.monitor.domain.model.MonitorSession
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "monitor-session"))

    fun read(): MonitorSession? {
        if (!file.baseFile.exists()) return null
        return try {
            val bytes = file.readFully()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes, 0, IV_LENGTH))
            val json = JSONObject(String(cipher.doFinal(bytes, IV_LENGTH, bytes.size - IV_LENGTH), Charsets.UTF_8))
            if (json.optString("principal") != "user" || json.getString("backend") != BuildConfig.BACKEND_URL) {
                clear()
                return null
            }
            MonitorSession(
                MonitorAccount(
                    json.getLong("id"), json.getString("name"), json.getString("email"), json.getString("phone"),
                ),
                json.getString("access"), json.getString("refresh"), json.getLong("expiry"),
            )
        } catch (_: Exception) {
            clear()
            null
        }
    }

    fun write(session: MonitorSession) {
        val json = JSONObject().apply {
            put("backend", BuildConfig.BACKEND_URL)
            put("principal", "user")
            put("id", session.account.id)
            put("name", session.account.name)
            put("email", session.account.email)
            put("phone", session.account.phone)
            put("access", session.accessToken)
            put("refresh", session.refreshToken)
            put("expiry", session.expiresAt)
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        check(cipher.iv.size == IV_LENGTH)
        val encrypted = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        val stream = file.startWrite()
        try {
            stream.write(encrypted)
            file.finishWrite(stream)
        } catch (failure: Exception) {
            file.failWrite(stream)
            throw failure
        }
    }

    fun clear() = file.delete()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return store.getKey(KEY_ALIAS, null) as? SecretKey
            ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
                init(
                    KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build(),
                )
                generateKey()
            }
    }

    private companion object {
        const val KEY_ALIAS = "casan-monitor-session"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
    }
}
