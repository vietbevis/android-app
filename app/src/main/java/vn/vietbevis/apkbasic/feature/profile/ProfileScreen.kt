package vn.vietbevis.apkbasic.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.AppLanguage
import vn.vietbevis.apkbasic.domain.model.ThemeMode
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.model.WeekStart
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme
import vn.vietbevis.apkbasic.ui.theme.SnapCream

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
    onSignOut: () -> Unit,
) {
    val viewModel = remember(userProfile.id) {
        ProfileViewModel(
            userProfile = userProfile,
            transactionRepository = appContainer.transactionRepository,
            recurringTransactionRepository = appContainer.recurringTransactionRepository,
            userPreferenceRepository = appContainer.userPreferenceRepository,
            sharingRepository = appContainer.sharingRepository,
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CapBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ProfileHeader(userProfile) }
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
            item { PreferencesCard(uiState, viewModel) }
            item { RecurringForm(uiState, viewModel) }
            if (uiState.recurringTransactions.isEmpty()) {
                item {
                    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                        Text("Chưa có giao dịch định kỳ trong Supabase.", modifier = Modifier.padding(18.dp), color = CapTextSecondary)
                    }
                }
            } else {
                items(uiState.recurringTransactions, key = { it.id }) { recurring ->
                    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                        Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(recurring.note ?: "Giao dịch định kỳ", style = MaterialTheme.typography.titleMedium)
                                Text("${recurring.schedule.frequency.name.lowercase()} · ${recurring.amount.formatVnd()}", color = CapTextSecondary)
                            }
                            TextButton(onClick = { viewModel.archiveRecurringTransaction(recurring.id) }) {
                                Text("Lưu trữ")
                            }
                        }
                    }
                }
            }
            item { SocialSharingCard(uiState, viewModel) }
            item { SettingsMenu() }
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
private fun ProfileHeader(userProfile: UserProfile) {
    CapCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), containerColor = CapSurfaceHigh) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.height(112.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.height(104.dp).fillMaxWidth(0.32f).background(CapPrimaryBlue, CircleShape))
            }
            Text(
                text = userProfile.displayName?.takeIf { it.isNotBlank() } ?: "CapMoney",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text("tháng này", color = CapTextSecondary)
        }
    }
}

@Composable
private fun PreferencesCard(uiState: ProfileUiState, viewModel: ProfileViewModel) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cài đặt dữ liệu thật", style = MaterialTheme.typography.titleLarge)
            Text("Ngôn ngữ", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = uiState.preference?.language == language,
                        onClick = { viewModel.setLanguage(language) },
                        label = { Text(if (language == AppLanguage.VIETNAMESE) "Tiếng Việt" else "English") },
                    )
                }
            }
            Text("Giao diện", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { theme ->
                    FilterChip(
                        selected = uiState.preference?.themeMode == theme,
                        onClick = { viewModel.setThemeMode(theme) },
                        label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            Text("Tuần bắt đầu", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeekStart.entries.forEach { weekStart ->
                    FilterChip(
                        selected = uiState.preference?.weekStartsOn == weekStart,
                        onClick = { viewModel.setWeekStart(weekStart) },
                        label = { Text(if (weekStart == WeekStart.MONDAY) "Thứ 2" else "CN") },
                    )
                }
            }
            Text("Tiền tệ · ${uiState.preference?.currency ?: "VND"}", color = CapTextSecondary)
        }
    }
}

@Composable
private fun RecurringForm(uiState: ProfileUiState, viewModel: ProfileViewModel) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Giao dịch định kỳ", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.wallets.forEach { wallet ->
                    FilterChip(
                        selected = uiState.selectedWalletId == wallet.id,
                        onClick = { viewModel.onWalletSelected(wallet.id) },
                        label = { Text(wallet.name) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { viewModel.onCategorySelected(category.id) },
                        label = { Text(category.name) },
                    )
                }
            }
            OutlinedTextField(
                value = uiState.recurringAmountInput,
                onValueChange = viewModel::onRecurringAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền định kỳ") },
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.recurringNoteInput,
                onValueChange = viewModel::onRecurringNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ghi chú") },
                singleLine = true,
            )
            Button(onClick = viewModel::createRecurringTransaction, modifier = Modifier.fillMaxWidth()) {
                Text("Lưu định kỳ hằng tháng")
            }
        }
    }
}

@Composable
private fun SocialSharingCard(uiState: ProfileUiState, viewModel: ProfileViewModel) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Bạn bè & nhóm", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = uiState.friendUserIdInput,
                onValueChange = viewModel::onFriendUserIdChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("User id bạn bè") },
                singleLine = true,
            )
            Button(onClick = viewModel::createFriendRequest, modifier = Modifier.fillMaxWidth()) {
                Text("Gửi lời mời")
            }
            OutlinedTextField(
                value = uiState.groupNameInput,
                onValueChange = viewModel::onGroupNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên nhóm") },
                singleLine = true,
            )
            Button(onClick = viewModel::createGroup, modifier = Modifier.fillMaxWidth()) {
                Text("Tạo nhóm")
            }
            if (uiState.groups.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.groups.forEach { group ->
                        FilterChip(
                            selected = uiState.selectedShareGroupId == group.id,
                            onClick = { viewModel.onShareGroupSelected(group.id) },
                            label = { Text(group.name) },
                        )
                    }
                }
                Button(onClick = viewModel::shareLatestTransactionToGroup, modifier = Modifier.fillMaxWidth()) {
                    Text("Chia sẻ giao dịch gần nhất")
                }
            }
            Text("Bạn bè: ${uiState.friends.size} · Nhóm: ${uiState.groups.size} · Chia sẻ: ${uiState.sharedTransactions.size}", color = CapTextSecondary)
            uiState.sharedTransactions.take(3).forEach { shared ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(shared.groupId?.let { "Nhóm $it" } ?: "Bạn bè", color = CapTextSecondary, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.deleteSharedTransaction(shared.id) }) {
                        Text("Gỡ")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenu() {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column {
            listOf(
                "Danh mục",
                "Góp ý",
                "Chia sẻ ứng dụng",
            ).forEach { label ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(label, style = MaterialTheme.typography.titleMedium)
                    Text("›", color = CapTextSecondary)
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

@Preview(showBackground = true, name = "Profile Screen")
@Composable
private fun ProfileScreenPreview() {
    APKBasicTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SnapCream)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                CapCard(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), containerColor = CapSurfaceHigh) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("SnapChi", style = MaterialTheme.typography.headlineMedium)
                        Text("Hồ sơ cá nhân", color = CapTextSecondary)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    OverviewTile("Giao dịch", "24", Modifier.weight(1f))
                    OverviewTile("Thu nhập", "8.000.000 đ", Modifier.weight(1f), CapIncomeMint)
                }
            }
            item {
                CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Cài đặt dữ liệu", style = MaterialTheme.typography.titleLarge)
                        Text("Ngôn ngữ · Tiếng Việt", color = CapTextSecondary)
                        Text("Giao diện · Sáng", color = CapTextSecondary)
                        Text("Tiền tệ · VND", color = CapTextSecondary)
                    }
                }
            }
        }
    }
}
