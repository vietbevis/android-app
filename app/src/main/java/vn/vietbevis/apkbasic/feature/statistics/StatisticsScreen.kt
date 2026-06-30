package vn.vietbevis.apkbasic.feature.statistics

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.text.drawText
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.domain.reporting.FinanceSummary
import vn.vietbevis.apkbasic.ui.components.CapCard
import vn.vietbevis.apkbasic.ui.components.CapPillRow
import vn.vietbevis.apkbasic.ui.components.CapStatusPill
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme
import vn.vietbevis.apkbasic.ui.theme.SnapCream

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.ui.theme.SnapBlue
import vn.vietbevis.apkbasic.ui.theme.SnapYellow
import vn.vietbevis.apkbasic.ui.theme.SnapMint
import vn.vietbevis.apkbasic.ui.theme.SnapSlate
import vn.vietbevis.apkbasic.ui.theme.SnapNavy
import vn.vietbevis.apkbasic.ui.theme.SnapWhite
import vn.vietbevis.apkbasic.ui.theme.SnapCoral

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
) {
    val viewModel = remember(userProfile.id) {
        StatisticsViewModel(
            userProfile = userProfile,
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
            transactionRepository = appContainer.transactionRepository,
            budgetRepository = appContainer.budgetRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showExportMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CapBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(Modifier.padding(top = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thống kê", style = MaterialTheme.typography.headlineMedium)
                    Box {
                        Button(onClick = { showExportMenu = true }) {
                            Text("Xuất CSV")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Thu nhập") },
                                onClick = {
                                    showExportMenu = false
                                    performExport(context, viewModel, ExportType.INCOME, uiState.rangeLabel)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Chi tiêu") },
                                onClick = {
                                    showExportMenu = false
                                    performExport(context, viewModel, ExportType.EXPENSE, uiState.rangeLabel)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Tất cả") },
                                onClick = {
                                    showExportMenu = false
                                    performExport(context, viewModel, ExportType.ALL, uiState.rangeLabel)
                                }
                            )
                        }
                    }
                }
                
                CapPillRow(Modifier.padding(top = 16.dp)) {
                    CapStatusPill(
                        text = "Tháng", 
                        selected = uiState.rangeType == StatisticsRangeType.MONTH,
                        onClick = { viewModel.setRangeType(StatisticsRangeType.MONTH) }
                    )
                    CapStatusPill(
                        text = "Năm", 
                        selected = uiState.rangeType == StatisticsRangeType.YEAR,
                        onClick = { viewModel.setRangeType(StatisticsRangeType.YEAR) }
                    )
                }
                
                Row(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::navigatePrevious) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Trước")
                    }
                    Text(
                        text = uiState.rangeLabel,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = viewModel::navigateNext) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sau")
                    }
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
            uiState.summary != null -> item {
                StatisticsContent(
                    rangeType = uiState.rangeType,
                    summary = requireNotNull(uiState.summary),
                    monthlyTrends = uiState.monthlyTrends,
                    goalStatus = uiState.budgetGoalStatus,
                    yearlySummary = uiState.yearlyBudgetSummary
                )
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

private fun performExport(
    context: android.content.Context,
    viewModel: StatisticsViewModel,
    type: ExportType,
    label: String
) {
    val csv = viewModel.exportCsv(type)
    if (csv != null) {
        val typeLabel = when (type) {
            ExportType.INCOME -> "Thu nhập"
            ExportType.EXPENSE -> "Chi tiêu"
            ExportType.ALL -> "Tổng hợp"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Báo cáo $typeLabel - $label")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(intent, "Xuất dữ liệu CSV"))
    }
}

@Composable
private fun StatisticsContent(
    rangeType: StatisticsRangeType,
    summary: FinanceSummary,
    monthlyTrends: List<MonthlyTrend>,
    goalStatus: BudgetGoalStatus?,
    yearlySummary: YearlyBudgetSummary?
) {
    var selectedChartType by remember { mutableStateOf(TransactionType.EXPENSE) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Core Stats
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard(
                title = "Thu nhập", 
                value = summary.income.formatVnd(), 
                accent = CapIncomeMint, 
                modifier = Modifier.weight(1f).clickable { selectedChartType = TransactionType.INCOME }
            )
            StatCard(
                title = "Chi tiêu", 
                value = summary.expense.formatVnd(), 
                accent = CapExpenseCoral, 
                modifier = Modifier.weight(1f).clickable { selectedChartType = TransactionType.EXPENSE }
            )
        }

        // 1. Category Insights (Donut Chart)
        CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedChartType == TransactionType.EXPENSE) "Chi tiêu theo danh mục" else "Thu nhập theo danh mục",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    CapPillRow {
                        CapStatusPill(
                            text = "Chi", 
                            selected = selectedChartType == TransactionType.EXPENSE,
                            onClick = { selectedChartType = TransactionType.EXPENSE }
                        )
                        CapStatusPill(
                            text = "Thu", 
                            selected = selectedChartType == TransactionType.INCOME,
                            onClick = { selectedChartType = TransactionType.INCOME }
                        )
                    }
                }

                val data = if (selectedChartType == TransactionType.EXPENSE) summary.expenseByCategory else summary.incomeByCategory
                
                if (data.isEmpty()) {
                    Text(
                        if (selectedChartType == TransactionType.EXPENSE) "Chưa có dữ liệu chi tiêu." else "Chưa có dữ liệu thu nhập.",
                        color = CapTextSecondary
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DonutChart(
                            modifier = Modifier.size(140.dp),
                            data = data
                        )
                        val totalUnits = data.sumOf { it.amount.minorUnits }.coerceAtLeast(1L)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            data.take(4).forEachIndexed { index, item ->
                                val percent = (item.amount.minorUnits.toDouble() / totalUnits.toDouble() * 100).toInt()
                                ChartLegend(
                                    label = "${item.category?.name ?: "Khác"} ($percent%)",
                                    color = chartColors[index % chartColors.size],
                                    amount = item.amount.formatVnd()
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Goals (Mục tiêu)
        if (rangeType == StatisticsRangeType.MONTH && goalStatus != null) {
            CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mục tiêu tháng", style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (goalStatus.isTotalExceeded) "Vượt ngưỡng" else "Trong tầm kiểm soát",
                            color = if (goalStatus.isTotalExceeded) CapExpenseCoral else CapIncomeMint,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Total Budget Progress
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng ngân sách", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${goalStatus.totalSpent.formatVnd()} / ${goalStatus.monthlyBudget?.amount?.formatVnd()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CapTextSecondary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { goalStatus.totalProgress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = if (goalStatus.isTotalExceeded) CapExpenseCoral else CapPrimaryBlue,
                            trackColor = CapBackground,
                        )
                    }

                    // Category Specific Budgets
                    if (goalStatus.categoryStatuses.isNotEmpty()) {
                        Text("Hạn mức danh mục", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                        goalStatus.categoryStatuses.forEach { status ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(status.category.name, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${status.spentAmount.formatVnd()} / ${status.budgetAmount.formatVnd()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (status.isExceeded) CapExpenseCoral else CapTextSecondary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { status.progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (status.isExceeded) CapExpenseCoral else CapPrimaryBlue.copy(alpha = 0.7f),
                                    trackColor = CapBackground,
                                )
                            }
                        }
                    }

                    // Remaining / Other Budget
                    if (goalStatus.otherBudget.minorUnits > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ngân sách còn lại", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${goalStatus.otherSpent.formatVnd()} / ${goalStatus.otherBudget.formatVnd()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (goalStatus.isOtherExceeded) CapExpenseCoral else CapTextSecondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { goalStatus.otherProgress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = if (goalStatus.isOtherExceeded) CapExpenseCoral else CapPrimaryBlue.copy(alpha = 0.7f),
                                trackColor = CapBackground,
                            )
                        }
                    }
                }
            }
        } else if (rangeType == StatisticsRangeType.YEAR && yearlySummary != null) {
            CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Hiệu quả quản lý ngân sách", style = MaterialTheme.typography.titleLarge)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            CircularProgressIndicator(
                                progress = { yearlySummary.performancePercent / 100f },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 8.dp,
                                color = if (yearlySummary.performancePercent >= 80) CapIncomeMint else SnapYellow,
                                trackColor = CapBackground
                            )
                            Text("${yearlySummary.performancePercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Bạn đã đạt mục tiêu ${yearlySummary.monthsSuccessful}/${yearlySummary.monthsBudgeted} tháng",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Tổng chi tiêu: ${yearlySummary.totalSpent.formatVnd()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CapTextSecondary
                            )
                            Text(
                                "Tổng ngân sách: ${yearlySummary.totalBudgeted.formatVnd()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CapTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. Income vs. Expense Trends (Only in YEAR view)
        if (rangeType == StatisticsRangeType.YEAR) {
            CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Xu hướng thu chi", style = MaterialTheme.typography.titleLarge)
                    TrendChart(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        trends = monthlyTrends
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem("Thu", CapIncomeMint)
                        Spacer(Modifier.width(16.dp))
                        LegendItem("Chi", CapExpenseCoral)
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    modifier: Modifier = Modifier,
    data: List<vn.vietbevis.apkbasic.domain.reporting.CategoryTotal>
) {
    val total = data.sumOf { it.amount.minorUnits }.coerceAtLeast(1L).toFloat()
    
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val strokeWidth = 36f
        
        data.forEachIndexed { index, item ->
            val fraction = item.amount.minorUnits.toFloat() / total
            val sweepAngle = fraction * 360f
            
            if (sweepAngle > 0) {
                drawArc(
                    color = chartColors[index % chartColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun TrendChart(
    modifier: Modifier = Modifier,
    trends: List<MonthlyTrend>
) {
    if (trends.isEmpty()) return
    val maxAmount = trends.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1L).toFloat()
    
    val labelColor = CapTextSecondary
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
    val valueStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold)
    
    var selectedIndex by remember { mutableStateOf(-1) }

    Column(modifier = modifier) {
        // Selection Detail Row
        if (selectedIndex != -1) {
            val trend = trends[selectedIndex]
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tháng ${trend.label}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Thu: ${vn.vietbevis.apkbasic.domain.model.Money.vnd(trend.income).formatVnd()}", color = CapIncomeMint, style = MaterialTheme.typography.labelMedium)
                    Text("Chi: ${vn.vietbevis.apkbasic.domain.model.Money.vnd(trend.expense).formatVnd()}", color = CapExpenseCoral, style = MaterialTheme.typography.labelMedium)
                }
            }
        } else {
            Text(
                text = "Chạm vào cột để xem số liệu chi tiết",
                style = MaterialTheme.typography.labelSmall,
                color = CapTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(trends) {
                    detectTapGestures { offset ->
                        val leftPadding = 32.dp.toPx()
                        val chartWidth = size.width - leftPadding
                        val spacing = chartWidth / (trends.size + 1)
                        val index = ((offset.x - leftPadding + spacing / 2) / spacing).toInt() - 1
                        selectedIndex = if (index in trends.indices) index else -1
                    }
                }
        ) {
            val barWidth = if (trends.size > 6) 6.dp.toPx() else 12.dp.toPx()
            val bottomPadding = 24.dp.toPx()
            val leftPadding = 32.dp.toPx()
            val chartHeight = size.height - bottomPadding
            val chartWidth = size.width - leftPadding
            val spacing = chartWidth / (trends.size + 1)
            
            // Draw Grid & Y-Axis Labels
            val steps = 4
            for (i in 0..steps) {
                val y = chartHeight - (i * (chartHeight - 10.dp.toPx()) / steps)
                val valLabel = (maxAmount * i / steps).toLong().formatShortVnd()
                val labelLayout = textMeasurer.measure(valLabel, textStyle.copy(fontSize = 8.sp))
                
                drawText(
                    textLayoutResult = labelLayout,
                    color = labelColor.copy(alpha = 0.6f),
                    topLeft = Offset(leftPadding - labelLayout.size.width - 4.dp.toPx(), y - labelLayout.size.height / 2)
                )
                
                drawLine(
                    color = labelColor.copy(alpha = 0.1f),
                    start = Offset(leftPadding, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            
            trends.forEachIndexed { index, trend ->
                val x = leftPadding + spacing * (index + 1)
                val isSelected = index == selectedIndex
                
                // Income bar
                val incomeHeight = (trend.income.toFloat() / maxAmount) * (chartHeight - 10.dp.toPx())
                drawRoundRect(
                    color = if (isSelected) CapIncomeMint else CapIncomeMint.copy(alpha = 0.6f),
                    topLeft = Offset(x - barWidth - 2f, chartHeight - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )
                
                // Expense bar
                val expenseHeight = (trend.expense.toFloat() / maxAmount) * (chartHeight - 10.dp.toPx())
                drawRoundRect(
                    color = if (isSelected) CapExpenseCoral else CapExpenseCoral.copy(alpha = 0.6f),
                    topLeft = Offset(x + 2f, chartHeight - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )

                // Month Label
                val labelResult = textMeasurer.measure(trend.label, textStyle)
                drawText(
                    textLayoutResult = labelResult,
                    color = if (isSelected) SnapNavy else labelColor,
                    topLeft = Offset(x - labelResult.size.width / 2, chartHeight + 4.dp.toPx())
                )
                
                // If selected, show short values on top
                if (isSelected) {
                    val incVal = trend.income.formatShortVnd()
                    val expVal = trend.expense.formatShortVnd()
                    val incRes = textMeasurer.measure(incVal, valueStyle)
                    val expRes = textMeasurer.measure(expVal, valueStyle)
                    
                    if (trend.income > 0) {
                        drawText(
                            textLayoutResult = incRes,
                            color = CapIncomeMint,
                            topLeft = Offset(x - barWidth - 2f + (barWidth - incRes.size.width) / 2, chartHeight - incomeHeight - incRes.size.height - 2.dp.toPx())
                        )
                    }
                    if (trend.expense > 0) {
                        drawText(
                            textLayoutResult = expRes,
                            color = CapExpenseCoral,
                            topLeft = Offset(x + 2f + (barWidth - expRes.size.width) / 2, chartHeight - expenseHeight - expRes.size.height - 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

private fun Long.formatShortVnd(): String {
    return when {
        this >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fTr", this / 1_000_000.0)
        this >= 1_000 -> String.format(java.util.Locale.US, "%dk", this / 1_000)
        else -> this.toString()
    }
}

@Composable
private fun ChartLegend(label: String, color: Color, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(amount, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = CapTextSecondary)
    }
}

private val chartColors = listOf(
    SnapCoral, SnapBlue, SnapMint, SnapYellow, SnapSlate
)

@Composable
private fun StatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    CapCard(modifier = modifier, containerColor = CapSurfaceHigh) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(34.dp)
                    .background(accent, RoundedCornerShape(17.dp)),
            )
            Text(title, color = CapTextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent)
        }
    }
}

@Preview(showBackground = true, name = "Statistics Screen")
@Composable
private fun StatisticsScreenPreview() {
    APKBasicTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SnapCream)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Column(Modifier.padding(top = 20.dp)) {
                    Text("Thống kê", style = MaterialTheme.typography.headlineMedium)
                    CapPillRow(Modifier.padding(top = 16.dp)) {
                        CapStatusPill(text = "Tháng", selected = true)
                        CapStatusPill(text = "Năm", selected = false)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    StatCard("Thu nhập", "8.000.000 đ", CapIncomeMint, Modifier.weight(1f))
                    StatCard("Chi tiêu", "2.450.000 đ", CapExpenseCoral, Modifier.weight(1f))
                }
            }
            item {
                CapCard(modifier = Modifier.fillMaxWidth(), containerColor = CapSurfaceHigh) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Chi tiêu theo danh mục", style = MaterialTheme.typography.titleLarge)
                        listOf("Ăn uống" to 0.62f, "Di chuyển" to 0.36f, "Mua sắm" to 0.24f).forEach { (label, fraction) ->
                            Text(label)
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = CapPrimaryBlue,
                                trackColor = SnapCream,
                            )
                        }
                    }
                }
            }
        }
    }
}
