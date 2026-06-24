package vn.vietbevis.apkbasic.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.AppLanguage
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.ThemeMode
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.WeekStart
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.components.SnapAvatar
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
    onSignOut: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit = {},
) {
    val viewModel = remember(userProfile.id) {
        ProfileViewModel(
            userProfile = userProfile,
            authRepository = appContainer.authRepository,
            photoRepository = appContainer.photoRepository,
            transactionRepository = appContainer.transactionRepository,
            userPreferenceRepository = appContainer.userPreferenceRepository,
            onProfileUpdated = onProfileUpdated,
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    ProfileContent(
        uiState = uiState,
        userProfile = uiState.userProfile,
        onSignOut = onSignOut,
        onLanguageSelected = viewModel::setLanguage,
        onThemeModeSelected = viewModel::setThemeMode,
        onWeekStartSelected = viewModel::setWeekStart,
        onDisplayNameChange = viewModel::updateDisplayName,
        onAvatarPicked = viewModel::uploadAvatar,
        modifier = modifier
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    userProfile: UserProfile,
    onSignOut: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onWeekStartSelected: (WeekStart) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onAvatarPicked: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempDisplayName by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    onAvatarPicked(stream.readBytes())
                }
            }
        }
    )

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Đổi tên hiển thị") },
            text = {
                OutlinedTextField(
                    value = tempDisplayName,
                    onValueChange = { tempDisplayName = it },
                    label = { Text("Tên mới") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDisplayNameChange(tempDisplayName)
                    showEditNameDialog = false
                }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CapBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            ProfileHeader(
                userProfile = userProfile,
                onAvatarClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onEditNameClick = {
                    tempDisplayName = userProfile.displayName ?: ""
                    showEditNameDialog = true
                }
            )
        }
        if (uiState.isLoading) {
            item {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            uiState.errorMessage?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            uiState.infoMessage?.let { item { Text(it, color = CapIncomeMint) } }
            item { Text("Tổng quan", style = MaterialTheme.typography.titleLarge) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    OverviewTile("Giao dịch", uiState.transactionCount.toString(), Modifier.weight(1f))
                    OverviewTile("Thu nhập", uiState.income.formatVnd(), Modifier.weight(1f), CapIncomeMint)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    OverviewTile("Chi tiêu", uiState.expense.formatVnd(), Modifier.weight(1f), CapExpenseCoral)
                    OverviewTile("Số dư", uiState.balance.formatVnd(), Modifier.weight(1f), CapIncomeMint)
                }
            }
            item {
                PreferencesCard(
                    uiState = uiState,
                    onLanguageSelected = onLanguageSelected,
                    onThemeModeSelected = onThemeModeSelected,
                    onWeekStartSelected = onWeekStartSelected
                )
            }
            item {
                Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                    Text("Đăng xuất")
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun ProfileHeader(
    userProfile: UserProfile,
    onAvatarClick: () -> Unit,
    onEditNameClick: () -> Unit,
) {
    CapCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), containerColor = CapSurfaceHigh) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SnapAvatar(
                displayName = userProfile.displayName,
                email = userProfile.email,
                avatarUrl = userProfile.avatar,
                size = 112.dp,
                updatedAt = userProfile.updatedAt,
                modifier = Modifier.clickable(onClick = onAvatarClick)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = userProfile.displayName?.takeIf { it.isNotBlank() }
                        ?: userProfile.email?.substringBefore("@")
                        ?: "SnapChi User",
                    style = MaterialTheme.typography.headlineMedium,
                )
                IconButton(onClick = onEditNameClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Đổi tên",
                        tint = CapPrimaryBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferencesCard(
    uiState: ProfileUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onWeekStartSelected: (WeekStart) -> Unit,
) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cài đặt", style = MaterialTheme.typography.titleLarge)
            Text("Ngôn ngữ", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = uiState.preference?.language == language,
                        onClick = { onLanguageSelected(language) },
                        label = { Text(if (language == AppLanguage.VIETNAMESE) "Tiếng Việt" else "English") },
                    )
                }
            }
            Text("Giao diện", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { theme ->
                    FilterChip(
                        selected = uiState.preference?.themeMode == theme,
                        onClick = { onThemeModeSelected(theme) },
                        label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            Text("Tuần bắt đầu", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeekStart.entries.forEach { weekStart ->
                    FilterChip(
                        selected = uiState.preference?.weekStartsOn == weekStart,
                        onClick = { onWeekStartSelected(weekStart) },
                        label = { Text(if (weekStart == WeekStart.MONDAY) "Thứ 2" else "CN") },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    CapCard(modifier = modifier, containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = CapTextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent)
        }
    }
}

@Preview(showBackground = true, name = "Full Profile Screen")
@Composable
private fun ProfileScreenPreview() {
    APKBasicTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userProfile = UserProfile(id = "user-id", email = "viet.hoang@example.com", displayName = "Việt Hoàng"),
                isLoading = false,
                transactionCount = 24,
                income = Money.vnd(8000000),
                expense = Money.vnd(3500000),
                balance = Money.vnd(4500000),
            ),
            userProfile = UserProfile(id = "user-id", email = "viet.hoang@example.com", displayName = "Việt Hoàng"),
            onSignOut = {},
            onLanguageSelected = {},
            onThemeModeSelected = {},
            onWeekStartSelected = {},
            onDisplayNameChange = {},
            onAvatarPicked = {}
        )
    }
}
