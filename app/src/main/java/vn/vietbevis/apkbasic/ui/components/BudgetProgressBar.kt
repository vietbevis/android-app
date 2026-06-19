package vn.vietbevis.apkbasic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.domain.model.Money
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint

@Composable
fun BudgetProgressBar(
    budgetName: String,
    spent: Money,
    limit: Money,
    modifier: Modifier = Modifier,
) {
    val progress = if (limit.minorUnits > 0) {
        (spent.minorUnits.toFloat() / limit.minorUnits.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }
    val isExceeded = spent.minorUnits > limit.minorUnits
    val color = if (isExceeded) CapExpenseCoral else CapIncomeMint

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = budgetName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${spent.formatVnd()} / ${limit.formatVnd()}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isExceeded) CapExpenseCoral else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
        if (isExceeded) {
            Text(
                text = "Đã vượt ngưỡng ${(spent.minorUnits - limit.minorUnits).let { Money.vnd(it).formatVnd() }}",
                color = CapExpenseCoral,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
