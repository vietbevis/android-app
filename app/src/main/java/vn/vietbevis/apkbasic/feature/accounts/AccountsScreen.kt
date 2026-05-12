package vn.vietbevis.apkbasic.feature.accounts

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
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.components.CapPillRow
import vn.vietbevis.apkbasic.ui.components.CapStatusPill
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary

@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
) {
    val viewModel = remember(userProfile.id) {
        AccountsViewModel(
            userProfile = userProfile,
            walletRepository = appContainer.walletRepository,
            transactionRepository = appContainer.transactionRepository,
            transferRepository = appContainer.transferRepository,
            loanRepository = appContainer.loanRepository,
            investmentRepository = appContainer.investmentRepository,
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
        item {
            Column(Modifier.padding(top = 20.dp)) {
                Text("Tài khoản", style = MaterialTheme.typography.headlineMedium)
                CapPillRow(Modifier.padding(top = 16.dp)) {
                    CapStatusPill(text = "Tài khoản", selected = true)
                    CapStatusPill(text = "Khoản vay", selected = false)
                    CapStatusPill(text = "Đầu tư", selected = false)
                }
            }
        }

        when {
            uiState.isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> item {
                Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            else -> {
                uiState.infoMessage?.let { message ->
                    item { Text(message, color = CapIncomeMint) }
                }
                item { AccountsSummary(uiState) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ActionTile("${uiState.transfers.size} chuyển tiền", Modifier.weight(1f))
                        ActionTile("${uiState.wallets.size} tài khoản", Modifier.weight(1f))
                    }
                }
                item { TransferForm(uiState, viewModel) }
                item { Text("Tài khoản", style = MaterialTheme.typography.titleLarge) }
                items(uiState.wallets, key = { it.wallet.id }) { account ->
                    AccountCard(account)
                }
                item { Text("Khoản vay", style = MaterialTheme.typography.titleLarge) }
                item { LoanForm(uiState, viewModel) }
                if (uiState.loans.isEmpty()) {
                    item { EmptyRealDataCard("Chưa có khoản vay trong Supabase.") }
                } else {
                    items(uiState.loans, key = { it.id }) { loan ->
                        CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(loan.name, style = MaterialTheme.typography.titleMedium)
                                    Text(loan.type.name.lowercase(), color = CapTextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(loan.principal.formatVnd(), color = MaterialTheme.colorScheme.error)
                                    TextButton(onClick = { viewModel.archiveLoan(loan.id) }) {
                                        Text("Lưu trữ")
                                    }
                                }
                            }
                        }
                    }
                }
                item { Text("Đầu tư", style = MaterialTheme.typography.titleLarge) }
                item { InvestmentForm(uiState, viewModel) }
                if (uiState.investments.isEmpty()) {
                    item { EmptyRealDataCard("Chưa có khoản đầu tư trong Supabase.") }
                } else {
                    items(uiState.investments, key = { it.id }) { investment ->
                        CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(investment.name, style = MaterialTheme.typography.titleMedium)
                                    Text(investment.type.name.lowercase(), color = CapTextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text((investment.currentValue ?: investment.principal).formatVnd(), color = CapIncomeMint)
                                    TextButton(onClick = { viewModel.archiveInvestment(investment.id) }) {
                                        Text("Lưu trữ")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun AccountsSummary(uiState: AccountsUiState) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tổng số dư", color = CapTextSecondary)
            Text(uiState.totalBalance.formatVnd(), style = MaterialTheme.typography.headlineLarge, color = CapIncomeMint)
            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                Text("Thu ${uiState.income.formatVnd()}", color = CapIncomeMint)
                Text("Chi ${uiState.expense.formatVnd()}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TransferForm(uiState: AccountsUiState, viewModel: AccountsViewModel) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Chuyển tiền", style = MaterialTheme.typography.titleLarge)
            Text("Từ", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.wallets.forEach { account ->
                    FilterChip(
                        selected = uiState.transferFromWalletId == account.wallet.id,
                        onClick = { viewModel.onTransferFromWalletSelected(account.wallet.id) },
                        label = { Text(account.wallet.name) },
                    )
                }
            }
            Text("Đến", color = CapTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.wallets.forEach { account ->
                    FilterChip(
                        selected = uiState.transferToWalletId == account.wallet.id,
                        onClick = { viewModel.onTransferToWalletSelected(account.wallet.id) },
                        label = { Text(account.wallet.name) },
                    )
                }
            }
            OutlinedTextField(
                value = uiState.transferAmountInput,
                onValueChange = viewModel::onTransferAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền chuyển") },
                singleLine = true,
            )
            Button(onClick = viewModel::createTransfer, modifier = Modifier.fillMaxWidth()) {
                Text("Lưu chuyển tiền")
            }
        }
    }
}

@Composable
private fun LoanForm(uiState: AccountsUiState, viewModel: AccountsViewModel) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Thêm khoản vay", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = uiState.loanNameInput,
                onValueChange = viewModel::onLoanNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên khoản vay") },
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.loanPrincipalInput,
                onValueChange = viewModel::onLoanPrincipalChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền gốc") },
                singleLine = true,
            )
            Button(onClick = viewModel::createLoan, modifier = Modifier.fillMaxWidth()) {
                Text("Lưu khoản vay")
            }
        }
    }
}

@Composable
private fun InvestmentForm(uiState: AccountsUiState, viewModel: AccountsViewModel) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Thêm đầu tư", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = uiState.investmentNameInput,
                onValueChange = viewModel::onInvestmentNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên khoản đầu tư") },
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.investmentPrincipalInput,
                onValueChange = viewModel::onInvestmentPrincipalChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Giá trị") },
                singleLine = true,
            )
            Button(onClick = viewModel::createInvestment, modifier = Modifier.fillMaxWidth()) {
                Text("Lưu đầu tư")
            }
        }
    }
}

@Composable
private fun AccountCard(account: AccountBalance) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(58.dp)
                    .weight(0.18f)
                    .background(CapPrimaryBlue, CircleShape),
            )
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(account.wallet.name, style = MaterialTheme.typography.titleLarge)
                Text(account.wallet.type.name.lowercase(), color = CapTextSecondary)
            }
            Text(account.balance.formatVnd(), color = CapIncomeMint, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun EmptyRealDataCard(text: String) {
    CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            color = CapTextSecondary,
        )
    }
}

@Composable
private fun ActionTile(
    text: String,
    modifier: Modifier = Modifier,
) {
    CapCard(modifier = modifier, containerColor = CapPrimaryBlue) {
        Box(Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
