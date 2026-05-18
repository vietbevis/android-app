package vn.vietbevis.apkbasic.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.ui.components.SnapPrimaryButton
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme
import vn.vietbevis.apkbasic.ui.theme.SnapBorder
import vn.vietbevis.apkbasic.ui.theme.SnapBorderSoft
import vn.vietbevis.apkbasic.ui.theme.SnapCoral
import vn.vietbevis.apkbasic.ui.theme.SnapCream
import vn.vietbevis.apkbasic.ui.theme.SnapCreamSurface
import vn.vietbevis.apkbasic.ui.theme.SnapNavy
import vn.vietbevis.apkbasic.ui.theme.SnapSlate
import vn.vietbevis.apkbasic.ui.theme.SnapWhite

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnapCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 64.dp),
        verticalArrangement = Arrangement.spacedBy(80.dp),
    ) {
        AuthBrand(modifier = Modifier.align(Alignment.CenterHorizontally))
        AuthPanel(
            uiState = uiState,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onConfirmPasswordChange = onConfirmPasswordChange,
            onToggleMode = onToggleMode,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun AuthBrand(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
            color = SnapCoral,
            contentColor = SnapWhite,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("S", style = MaterialTheme.typography.headlineLarge)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = SnapNavy)
            Text(stringResource(R.string.auth_product_label), style = MaterialTheme.typography.labelMedium, color = SnapSlate)
        }
    }
}

@Composable
private fun AuthPanel(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(if (uiState.isSignUp) R.string.auth_sign_up_title else R.string.auth_sign_in_title),
                style = MaterialTheme.typography.headlineMedium,
                color = SnapNavy,
            )
            Text(
                text = stringResource(if (uiState.isSignUp) R.string.auth_sign_up_description else R.string.auth_sign_in_description),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                color = SnapSlate,
                textAlign = TextAlign.Center,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SnapCreamSurface,
            border = BorderStroke(1.dp, SnapBorderSoft),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 36.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                AuthTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = stringResource(R.string.auth_email_label),
                    placeholder = stringResource(R.string.auth_email_placeholder),
                    keyboardType = KeyboardType.Email,
                    enabled = !uiState.isLoading,
                )
                AuthTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.auth_password_label),
                    placeholder = stringResource(R.string.auth_password_placeholder),
                    keyboardType = KeyboardType.Password,
                    enabled = !uiState.isLoading,
                    isPassword = true,
                )
                if (uiState.isSignUp) {
                    AuthTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = stringResource(R.string.auth_confirm_password_label),
                        placeholder = stringResource(R.string.auth_password_placeholder),
                        keyboardType = KeyboardType.Password,
                        enabled = !uiState.isLoading,
                        isPassword = true,
                    )
                }
                uiState.errorMessage?.let { AuthMessage(text = it, isError = true) }
                uiState.infoMessage?.let { AuthMessage(text = it, isError = false) }
                SnapPrimaryButton(
                    text = stringResource(if (uiState.isSignUp) R.string.auth_action_sign_up else R.string.auth_action_sign_in),
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    isLoading = uiState.isLoading,
                )
                TextButton(
                    onClick = onToggleMode,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = stringResource(
                            if (uiState.isSignUp) R.string.auth_switch_to_sign_in_hint else R.string.auth_switch_to_sign_up_hint,
                        ),
                        color = SnapCoral,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = SnapNavy)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = SnapSlate) },
            enabled = enabled,
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SnapCream,
                unfocusedContainerColor = SnapCream,
                disabledContainerColor = SnapCream,
                focusedBorderColor = SnapBorder,
                unfocusedBorderColor = SnapBorder,
                focusedTextColor = SnapNavy,
                unfocusedTextColor = SnapNavy,
            ),
        )
    }
}

@Composable
private fun AuthMessage(text: String, isError: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer else SnapCream,
        contentColor = if (isError) MaterialTheme.colorScheme.error else SnapSlate,
        border = BorderStroke(1.dp, if (isError) MaterialTheme.colorScheme.error else SnapBorder),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true, name = "Auth Login")
@Composable
private fun AuthScreenLoginPreview() {
    APKBasicTheme {
        AuthScreen(
            uiState = AuthUiState(email = "demo@example.com", isSignUp = false),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onToggleMode = {},
            onSubmit = {},
        )
    }
}

@Preview(showBackground = true, name = "Auth Sign Up")
@Composable
private fun AuthScreenSignUpPreview() {
    APKBasicTheme {
        AuthScreen(
            uiState = AuthUiState(email = "new@example.com", isSignUp = true),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onToggleMode = {},
            onSubmit = {},
        )
    }
}
