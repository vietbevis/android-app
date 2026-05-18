package vn.vietbevis.apkbasic.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme
import vn.vietbevis.apkbasic.ui.theme.CapDivider
import vn.vietbevis.apkbasic.ui.theme.CapPrimaryBlue
import vn.vietbevis.apkbasic.ui.theme.CapSurface
import vn.vietbevis.apkbasic.ui.theme.CapSurfaceHigh
import vn.vietbevis.apkbasic.ui.theme.SnapBlue
import vn.vietbevis.apkbasic.ui.theme.SnapBorder
import vn.vietbevis.apkbasic.ui.theme.SnapBorderSoft
import vn.vietbevis.apkbasic.ui.theme.SnapCoral
import vn.vietbevis.apkbasic.ui.theme.SnapCream
import vn.vietbevis.apkbasic.ui.theme.SnapCreamSurface
import vn.vietbevis.apkbasic.ui.theme.SnapMint
import vn.vietbevis.apkbasic.ui.theme.SnapNavy
import vn.vietbevis.apkbasic.ui.theme.SnapSlate
import vn.vietbevis.apkbasic.ui.theme.SnapSoftYellow
import vn.vietbevis.apkbasic.ui.theme.SnapWhite
import vn.vietbevis.apkbasic.ui.theme.SnapYellow

object SnapSpacing {
    val screen = 16.dp
    val section = 32.dp
    val item = 18.dp
    val card = 20.dp
}

object SnapRadius {
    val card = 16.dp
    val banner = 20.dp
    val input = 14.dp
    val iconTile = 10.dp
    val pill = 40.dp
}

val SnapBannerShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = SnapRadius.banner,
    bottomEnd = 0.dp,
    bottomStart = SnapRadius.banner,
)

@Composable
fun SnapScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = SnapSpacing.screen),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(SnapCream)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(SnapSpacing.item),
        content = content,
    )
}

@Composable
fun SnapTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
            navigationIcon?.invoke()
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = SnapNavy,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
            actionIcon?.invoke()
        }
    }
}

@Composable
fun SnapIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = SnapNavy,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(50.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, SnapBorder),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun SnapPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 54.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(SnapRadius.input),
        colors = ButtonDefaults.buttonColors(
            containerColor = SnapCoral,
            contentColor = SnapWhite,
            disabledContainerColor = SnapBorderSoft,
            disabledContentColor = SnapSlate,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = SnapWhite,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SnapSecondaryPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(SnapRadius.pill),
        color = if (selected) SnapCoral else Color.Transparent,
        contentColor = if (selected) SnapWhite else SnapNavy,
        border = BorderStroke(1.dp, if (selected) SnapCoral else SnapBorder),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

@Composable
fun SnapSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = SnapNavy)
        actionText?.let {
            SnapSecondaryPill(text = it, selected = false, onClick = onAction)
        }
    }
}

@Composable
fun SnapCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    borderColor: Color = SnapBorder,
    shape: RoundedCornerShape = RoundedCornerShape(SnapRadius.card),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { Surface(color = Color.Transparent, content = content) },
    )
}

@Composable
fun SnapColoredBanner(
    modifier: Modifier = Modifier,
    containerColor: Color,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = SnapBannerShape,
        color = containerColor,
        contentColor = SnapNavy,
    ) {
        Box(Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun SnapSummaryBanner(
    label: String,
    amount: String,
    meta: String,
    modifier: Modifier = Modifier,
) {
    SnapColoredBanner(modifier = modifier.fillMaxWidth(), containerColor = SnapCoral) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(label, style = MaterialTheme.typography.titleLarge, color = SnapWhite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(amount, style = MaterialTheme.typography.headlineLarge, color = SnapWhite)
                Text(meta, style = MaterialTheme.typography.titleLarge, color = SnapWhite)
            }
        }
    }
}

@Composable
fun SnapIconTile(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SnapSoftYellow,
    size: Dp = 56.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(SnapRadius.iconTile))
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = SnapNavy,
        )
    }
}

@Composable
fun SnapListItem(
    title: String,
    subtitle: String,
    trailingTitle: String,
    trailingSubtitle: String,
    modifier: Modifier = Modifier,
    iconText: String = title,
    iconContainerColor: Color = SnapSoftYellow,
    onClick: (() -> Unit)? = null,
) {
    val itemModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    SnapCard(modifier = itemModifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SnapIconTile(text = iconText, containerColor = iconContainerColor)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = SnapNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = SnapSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(trailingTitle, style = MaterialTheme.typography.titleMedium, color = SnapNavy, maxLines = 1)
                Text(trailingSubtitle, style = MaterialTheme.typography.labelMedium, color = SnapSlate, maxLines = 1)
            }
        }
    }
}

@Composable
fun SnapTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    minLines: Int = 1,
    singleLine: Boolean = minLines == 1,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = SnapNavy)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = SnapSlate) },
            enabled = enabled,
            minLines = minLines,
            singleLine = singleLine,
            shape = RoundedCornerShape(SnapRadius.input),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SnapCream,
                unfocusedContainerColor = SnapCream,
                disabledContainerColor = SnapCreamSurface,
                focusedBorderColor = SnapBorder,
                unfocusedBorderColor = SnapBorder,
                focusedTextColor = SnapNavy,
                unfocusedTextColor = SnapNavy,
            ),
        )
    }
}

@Composable
fun SnapMessageCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    SnapCard(modifier = modifier.fillMaxWidth(), containerColor = SnapCreamSurface, borderColor = SnapBorderSoft) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = SnapNavy)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = SnapSlate)
            actionText?.let {
                SnapSecondaryPill(
                    text = it,
                    selected = false,
                    modifier = Modifier.align(Alignment.End),
                    onClick = onAction,
                )
            }
        }
    }
}

@Composable
fun CapCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CapSurface,
    content: @Composable () -> Unit,
) {
    SnapCard(modifier = modifier, containerColor = containerColor, content = content)
}

@Composable
fun CapSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = SnapNavy,
    )
}

@Composable
fun CapPillRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = { content() },
    )
}

@Composable
fun CapStatusPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    SnapSecondaryPill(
        text = text,
        selected = selected,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun SnapComponentsPreview() {
    APKBasicTheme {
        Column(
            modifier = Modifier
                .background(SnapCream)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SnapTopBar(
                title = "Trang chủ",
                actionIcon = {
                    SnapIconButton(
                        iconRes = R.drawable.ic_plus,
                        contentDescription = "Thêm giao dịch",
                        onClick = {},
                    )
                },
            )
            SnapSummaryBanner(label = "Số dư", amount = "12.450.000 đ", meta = "05/26")
            SnapSectionHeader(title = "Giao dịch", actionText = "Xem tất cả")
            SnapListItem(
                title = "Ăn uống",
                subtitle = "Ví tiền mặt · Hôm nay",
                trailingTitle = "-120.000 đ",
                trailingSubtitle = "Chi tiêu",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(SnapBlue, SnapMint, SnapYellow).forEach { color ->
                    Box(Modifier.size(36.dp).clip(CircleShape).background(color))
                }
            }
        }
    }
}
