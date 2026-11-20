package id.casan.monitor.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.core.designsystem.theme.Space

private val internationalPhone = Regex("^\\+[1-9][0-9]{1,14}$")

@Composable
fun LoginScreen(
    state: AuthUiState,
    onSendOtp: (String, Int) -> Unit,
    onLogin: (String, String, Int) -> Unit,
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var channel by rememberSaveable { mutableIntStateOf(2) }
    var code by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current
    val normalizedPhone = phone.trim()
    val validPhone = internationalPhone.matches(normalizedPhone)
    val sent = state.sentTo == OtpDestination(normalizedPhone, channel)
    val canSubmit = !state.loading && validPhone && code.length == 4

    fun submit() {
        if (!canSubmit) return
        focus.clearFocus()
        onLogin(normalizedPhone, code, channel)
        code = ""
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        PageFrame { modifier ->
            Column(
                modifier.safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(Space.large),
                verticalArrangement = Arrangement.spacedBy(Space.large),
            ) {
                BrandMark()
                PageHeading(stringResource(R.string.app_name), stringResource(R.string.login_intro))
                MonitorCard(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            code = ""
                        },
                        label = { Text("Phone number") },
                        supportingText = { Text("Use international format, for example +6281234567890.") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.loading,
                        singleLine = true,
                        isError = phone.isNotBlank() && !validPhone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.small)) {
                        listOf(2 to "WhatsApp", 1 to "SMS").forEach { (option, label) ->
                            FilterChip(
                                selected = channel == option,
                                onClick = {
                                    channel = option
                                    code = ""
                                },
                                label = { Text(label) },
                                enabled = !state.loading,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { onSendOtp(normalizedPhone, channel) },
                        enabled = !state.loading && validPhone,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (sent) "Resend code" else "Send code") }
                    if (sent) Text("Check your selected channel for the code.")
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter { digit -> digit in '0'..'9' }.take(4) },
                        label = { Text("Four-digit code") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.loading,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Button(onClick = ::submit, enabled = canSubmit, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.sign_in))
                    }
                    if (state.loading) LoadingState()
                    state.error?.let { Text(it.message(), color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    MonitorTheme { LoginScreen(AuthUiState(), { _, _ -> }, { _, _, _ -> }) }
}
