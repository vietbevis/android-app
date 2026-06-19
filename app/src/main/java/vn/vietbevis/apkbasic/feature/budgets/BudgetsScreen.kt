package vn.vietbevis.apkbasic.feature.budgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.components.MonthYearPicker
import vn.vietbevis.apkbasic.ui.components.SnapIconButton
import vn.vietbevis.apkbasic.ui.components.SnapTopBar
import vn.vietbevis.apkbasic.feature.home.homeDateLabel
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary
import vn.vietbevis.apkbasic.ui.theme.SnapBlue
import vn.vietbevis.apkbasic.ui.theme.SnapCoral
import vn.vietbevis.apkbasic.ui.theme.SnapWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
    initialMonth: Int? = null,
    initialYear: Int? = null,
    initialTab: Int = 0,
) {
    val viewModel = remember(userProfile.id) {
        BudgetsViewModel(
            userProfile = userProfile,
            budgetRepository = appContainer.budgetRepository,
            categoryRepository = appContainer.categoryRepository,
            transactionRepository = appContainer.transactionRepository,
        )
    }

    LaunchedEffect(initialMonth, initialYear) {
        if (initialMonth != null && initialYear != null) {
            viewModel.onMonthYearSelected(initialMonth, initialYear)
        }
    }
    
    LaunchedEffect(initialTab) {
        viewModel.selectTab(initialTab)
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CapBackground)
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text("Ngân sách", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Quản lý hạn mức và tiến độ chi tiêu", color = CapTextSecondary)
            Spacer(Modifier.height(16.dp))

            MonthYearPicker(
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onMonthSelected = viewModel::onMonthYearSelected,
                monthsWithBudget = uiState.monthsWithBudget,
                modifier = Modifier.height(280.dp)
            )

            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.Transparent,
                contentColor = SnapCoral,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = SnapCoral
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Chi tiết", fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Cập nhật", fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SnapCoral)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                
                if (uiState.selectedTab == 0) {
                    // TAB CHI TIẾT
                    item { SummaryInsightCard(uiState = uiState) }
                    
                    val filteredItems = uiState.categoryProgressItems.filter { it.budget != null }
                    
                    if (filteredItems.isEmpty() && uiState.currentMonthlyBudget != null && uiState.otherBudgetLimit.minorUnits <= 0) {
                        item {
                            Text(
                                "Chưa có danh mục nào được đặt hạn mức riêng trong tháng này.",
                                color = CapTextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }

                    items(filteredItems, key = { it.category.id }) { item ->
                        BudgetProgressCard(
                            title = item.category.name,
                            spent = item.spent,
                            limit = item.budget?.amount,
                            percentSpent = item.percentSpent,
                            isExceeded = item.isExceeded,
                            isSelected = uiState.selectedCategoryId == item.category.id,
                            onClick = { 
                                viewModel.selectCategory(if (uiState.selectedCategoryId == item.category.id) null else item.category.id)
                            },
                            transactions = if (uiState.selectedCategoryId == item.category.id) uiState.selectedCategoryTransactions else emptyList(),
                            allCategories = uiState.categories
                        )
                    }

                    // Ngân sách còn lại (Khác)
                    if (uiState.currentMonthlyBudget != null && uiState.otherBudgetLimit.minorUnits > 0) {
                        item {
                            BudgetProgressCard(
                                title = "Ngân sách còn lại",
                                spent = uiState.otherSpent,
                                limit = uiState.otherBudgetLimit,
                                percentSpent = uiState.otherPercentSpent,
                                isExceeded = uiState.isOtherExceeded,
                                isSelected = uiState.selectedCategoryId == "OTHER",
                                onClick = { 
                                    viewModel.selectCategory(if (uiState.selectedCategoryId == "OTHER") null else "OTHER")
                                },
                                transactions = if (uiState.selectedCategoryId == "OTHER") uiState.selectedCategoryTransactions else emptyList(),
                                allCategories = uiState.categories
                            )
                        }
                    }
                } else {
                    // TAB THIẾT LẬP
                    item {
                        CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    "Hạn mức tháng ${uiState.selectedMonth + 1}/${uiState.selectedYear}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = uiState.totalAmountInput,
                                    onValueChange = viewModel::onTotalAmountChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Hạn mức tổng (VND)") },
                                    suffix = { Text("đ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CapBackground)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Hạn mức danh mục", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = { viewModel.showCategoryPicker(true) },
                                        modifier = Modifier.size(32.dp).background(SnapCoral, CircleShape)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_plus),
                                            contentDescription = "Thêm danh mục",
                                            tint = SnapWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (uiState.isTotalExceededInSetup) {
                                    Text(
                                        "⚠️ Tổng hạn mức danh mục (${Money.vnd(uiState.totalCategoryAmount).formatVnd()}) đang lớn hơn hạn mức chung",
                                        color = CapExpenseCoral,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                uiState.categoryAmountInputs.forEach { (categoryId, amount) ->
                                    val category = uiState.categories.find { it.id == categoryId }
                                    if (category != null) {
                                        CategoryBudgetInput(
                                            name = category.name,
                                            iconRes = R.drawable.ic_budget,
                                            value = amount,
                                            onValueChange = { viewModel.onCategoryAmountChange(categoryId, it) },
                                            onRemove = { viewModel.removeCategoryBudget(categoryId) }
                                        )
                                    }
                                }
                                
                                if (uiState.categoryAmountInputs.isEmpty()) {
                                    Text(
                                        "Nhấn nút + để thêm hạn mức cho từng danh mục cụ thể.",
                                        color = CapTextSecondary,
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = viewModel::saveBudget,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isSaving && uiState.totalAmountInput.isNotBlank()
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Text("Lưu cấu hình")
                                    }
                                }

                                uiState.errorMessage?.let { Text(it, color = CapExpenseCoral, fontSize = 13.sp) }
                                uiState.infoMessage?.let { Text(it, color = CapIncomeMint, fontSize = 13.sp) }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    if (uiState.isShowingCategoryPicker) {
        CategoryPickerDialog(
            categories = uiState.availableCategoriesForPicker,
            onCategorySelected = viewModel::addCategoryToBudget,
            onDismiss = { viewModel.showCategoryPicker(false) }
        )
    }
}

@Composable
private fun SummaryInsightCard(
    uiState: BudgetsUiState
) {
    CapCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = CapSurfaceHigh
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tổng chi tiêu", color = CapTextSecondary, fontSize = 12.sp)
                    Text(
                        uiState.totalSpent.formatVnd(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isTotalExceeded) CapExpenseCoral else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (uiState.currentMonthlyBudget != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Còn lại", color = CapTextSecondary, fontSize = 12.sp)
                        Text(
                            uiState.remainingAmount.formatVnd(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.remainingAmount.minorUnits < 0) CapExpenseCoral else CapIncomeMint
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Hạn mức", color = CapTextSecondary, fontSize = 12.sp)
                    Text(
                        uiState.currentMonthlyBudget?.amount?.formatVnd() ?: "Chưa đặt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (uiState.currentMonthlyBudget != null) {
                val totalLimit = uiState.currentMonthlyBudget.amount.minorUnits
                if (totalLimit > 0) {
                    val progress = (uiState.totalSpent.minorUnits.toFloat() / totalLimit).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        color = if (uiState.isTotalExceeded) CapExpenseCoral else CapPrimaryBlue,
                        trackColor = CapBackground,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.safeSpendToday?.let {
                        InsightItem(
                            modifier = Modifier.weight(1f),
                            label = "Hôm nay nên tiêu",
                            amount = it,
                            isWarning = uiState.isTotalExceeded
                        )
                    }
                    uiState.safeSpendThisWeek?.let {
                        InsightItem(
                            modifier = Modifier.weight(1f),
                            label = "Tuần này nên tiêu",
                            amount = it,
                            isWarning = uiState.isTotalExceeded
                        )
                    }
                }
            } else {
                Text(
                    "Bạn chưa thiết lập ngân sách cho tháng này. Chuyển sang tab Thiết lập để đặt hạn mức.",
                    color = CapTextSecondary,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun InsightItem(
    modifier: Modifier = Modifier,
    label: String,
    amount: Money,
    isWarning: Boolean
) {
    Column(
        modifier = modifier
            .background(CapBackground, MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        Text(label, color = CapTextSecondary, fontSize = 10.sp)
        Text(
            amount.formatVnd(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) CapExpenseCoral else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BudgetProgressCard(
    title: String,
    spent: Money,
    limit: Money?,
    percentSpent: Int,
    isExceeded: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    transactions: List<vn.vietbevis.apkbasic.domain.model.Transaction>,
    allCategories: List<vn.vietbevis.apkbasic.domain.model.Category>
) {
    val dateTimeFormatter = remember { java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.forLanguageTag("vi-VN")) }

    CapCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = CapSurfaceHigh
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ... (phần tiêu đề và thanh tiến độ giữ nguyên)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CapTextSecondary
                )
            }

            LinearProgressIndicator(
                progress = { (percentSpent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (isExceeded) CapExpenseCoral else CapPrimaryBlue,
                trackColor = CapBackground,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$percentSpent% " + if (isExceeded) "(Vượt ngưỡng!)" else "",
                    color = if (isExceeded) CapExpenseCoral else CapTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${spent.formatVnd()} / ${limit?.formatVnd() ?: "Không giới hạn"}",
                    color = CapTextSecondary,
                    fontSize = 12.sp
                )
            }

            AnimatedVisibility(visible = isSelected) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (transactions.isEmpty()) {
                        Text("Không có giao dịch nào trong tháng này", color = CapTextSecondary, fontSize = 12.sp)
                    } else {
                        transactions.forEach { tx ->
                            val categoryName = allCategories.find { it.id == tx.categoryId }?.name ?: "Khác"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        dateTimeFormatter.format(java.util.Date(tx.occurredAtEpochMillis)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CapTextSecondary
                                    )
                                }
                                Text(
                                    tx.amount.formatVnd(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == vn.vietbevis.apkbasic.domain.model.TransactionType.EXPENSE) CapExpenseCoral else CapIncomeMint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerDialog(
    categories: List<vn.vietbevis.apkbasic.domain.model.Category>,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = SnapWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Chọn danh mục để thêm hạn mức",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CapTextSecondary
            )
            
            if (categories.isEmpty()) {
                Text("Tất cả danh mục đã được thêm.", color = CapTextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CapCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(category.id) },
                            containerColor = CapSurfaceHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_budget),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = SnapBlue
                                )
                                Text(category.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategoryBudgetInput(
    name: String,
    iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = CapTextSecondary
        )
        Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(120.dp),
            placeholder = { Text("0", color = CapTextSecondary) },
            suffix = { Text("đ", fontSize = 12.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "Xóa",
                tint = CapExpenseCoral,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
