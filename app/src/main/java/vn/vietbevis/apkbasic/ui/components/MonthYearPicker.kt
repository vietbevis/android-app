package vn.vietbevis.apkbasic.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import vn.vietbevis.apkbasic.R

@Composable
fun MonthYearPicker(
    selectedMonth: Int, // 0-11
    selectedYear: Int,
    onMonthSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    monthsWithBudget: Set<String> = emptySet(), // Format: YYYY-MM
) {
    val currentCalendar = Calendar.getInstance()
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentYear = currentCalendar.get(Calendar.YEAR)

    val months = listOf(
        stringResource(R.string.month_short_1), stringResource(R.string.month_short_2), stringResource(R.string.month_short_3), stringResource(R.string.month_short_4),
        stringResource(R.string.month_short_5), stringResource(R.string.month_short_6), stringResource(R.string.month_short_7), stringResource(R.string.month_short_8),
        stringResource(R.string.month_short_9), stringResource(R.string.month_short_10), stringResource(R.string.month_short_11), stringResource(R.string.month_short_12)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthSelected(selectedMonth, selectedYear - 1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.month_picker_prev_year))
            }
            Text(
                text = stringResource(R.string.month_picker_year, selectedYear),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthSelected(selectedMonth, selectedYear + 1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.month_picker_next_year))
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(months.indices.toList()) { index ->
                val isSelected = index == selectedMonth
                val isCurrent = index == currentMonth && selectedYear == currentYear
                val hasBudget = monthsWithBudget.contains(String.format(Locale.US, "%04d-%02d", selectedYear, index + 1))

                MonthItem(
                    monthName = months[index],
                    isSelected = isSelected,
                    isCurrent = isCurrent,
                    hasBudget = hasBudget,
                    onClick = { onMonthSelected(index, selectedYear) }
                )
            }
        }
    }
}

@Composable
private fun MonthItem(
    monthName: String,
    isSelected: Boolean,
    isCurrent: Boolean,
    hasBudget: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .then(
                if (isCurrent && !isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = monthName,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            )
            if (hasBudget) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                )
            }
        }
        if (isCurrent && !isSelected) {
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.background)
            ) {
                Box(modifier = Modifier.size(6.dp))
            }
        }
    }
}
