package dev.kyralabs.pingogram.credentials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.kyralabs.pingogram.R

/**
 * Where the user enters the Telegram API credentials TDLib signs in with, before the first login: the build may ship
 * none (the debug APK of a fork), and anyone can use their own application from my.telegram.org. Saving restarts the
 * app, because TDLib's parameters are built while the application starts.
 *
 * @property initialApiId the stored id, or null when there is none to show.
 * @property initialApiHash the stored hash, or an empty string when there is none.
 * @property showTimeoutMessage whether TDLib stayed at `WaitTdlibParameters`, which means these credentials did not
 *   work; the screen then offers [onContinue] to restart and try again.
 */
@Composable
fun ApiCredentialsScreen(
    initialApiId: Int?,
    initialApiHash: String,
    showTimeoutMessage: Boolean,
    onSave: (apiId: Int, apiHash: String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    var apiIdText by rememberSaveable { mutableStateOf(initialApiId?.takeIf { it > 0 }?.toString().orEmpty()) }
    var apiHashText by rememberSaveable { mutableStateOf(initialApiHash) }
    var error by rememberSaveable { mutableStateOf<Error?>(null) }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.api_credentials_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.api_credentials_intro),
                style = MaterialTheme.typography.bodyLarge,
            )
            TextButton(onClick = { uriHandler.openUri("https://my.telegram.org/apps") }) {
                Text(stringResource(R.string.api_credentials_open_site))
            }
            if (showTimeoutMessage) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = stringResource(R.string.api_credentials_timeout),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            OutlinedTextField(
                value = apiIdText,
                onValueChange = {
                    apiIdText = it.filter(Char::isDigit)
                    error = null
                },
                label = { Text(stringResource(R.string.api_credentials_id_label)) },
                singleLine = true,
                isError = error == Error.InvalidId,
                supportingText = {
                    if (error == Error.InvalidId) Text(stringResource(R.string.api_credentials_error_id))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = apiHashText,
                onValueChange = {
                    apiHashText = it.trim().take(API_HASH_LENGTH)
                    error = null
                },
                label = { Text(stringResource(R.string.api_credentials_hash_label)) },
                singleLine = true,
                isError = error == Error.InvalidHash,
                supportingText = {
                    if (error == Error.InvalidHash) Text(stringResource(R.string.api_credentials_error_hash))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    val apiId = apiIdText.toIntOrNull()
                    val apiHash = apiHashText.trim().lowercase()
                    error =
                        when {
                            apiId == null || apiId <= 0 -> Error.InvalidId
                            !ApiCredentialsStore.isValid(apiId, apiHash) -> Error.InvalidHash
                            else -> null
                        }
                    if (error == null && apiId != null) onSave(apiId, apiHash)
                },
                enabled = apiIdText.isNotBlank() && apiHashText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.api_credentials_save))
            }
            if (showTimeoutMessage) {
                TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.api_credentials_continue))
                }
            }
        }
    }
}

private enum class Error {
    InvalidId,
    InvalidHash,
}

private const val API_HASH_LENGTH = 32
