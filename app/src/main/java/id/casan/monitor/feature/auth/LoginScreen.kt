package id.casan.monitor.feature.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import id.casan.monitor.R
import id.casan.monitor.core.designsystem.components.*
import id.casan.monitor.core.designsystem.theme.MonitorTheme
import id.casan.monitor.core.designsystem.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var showDemoMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val validEmail = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val emailError = attempted && !validEmail
    val passwordError = attempted && password.isBlank()

    fun submit() {
        if (loading) return
        attempted = true
        showDemoMessage = false
        if (!validEmail || password.isBlank()) return
        focus.clearFocus()
        loading = true
        scope.launch {
            delay(350)
            loading = false
            password = ""
            showPassword = false
            attempted = false
            showDemoMessage = true
        }
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
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        singleLine = true,
                        isError = emailError,
                        supportingText = if (emailError) ({ Text(stringResource(R.string.email_error)) }) else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading,
                        singleLine = true,
                        isError = passwordError,
                        supportingText = if (passwordError) ({ Text(stringResource(R.string.password_error)) }) else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }, enabled = !loading) {
                                MonitorIcon(
                                    if (showPassword) R.drawable.ic_eye_off else R.drawable.ic_eye,
                                    stringResource(if (showPassword) R.string.hide_password else R.string.show_password),
                                )
                            }
                        },
                    )
                    Button(onClick = ::submit, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.sign_in))
                    }
                    if (loading) LoadingState()
                    if (showDemoMessage) Text(stringResource(R.string.login_demo))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    MonitorTheme { LoginScreen() }
}
