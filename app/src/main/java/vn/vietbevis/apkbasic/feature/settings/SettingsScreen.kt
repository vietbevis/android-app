package vn.vietbevis.apkbasic.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.ui.components.SnapMessageCard
import vn.vietbevis.apkbasic.ui.components.SnapPrimaryButton
import vn.vietbevis.apkbasic.ui.components.SnapSectionHeader
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme
import vn.vietbevis.apkbasic.ui.theme.SnapCream

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnapCream)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SnapSectionHeader(title = stringResource(R.string.settings_title))
        SnapMessageCard(
            title = "Tài khoản",
            body = "Dữ liệu ví, danh mục, giao dịch và ảnh hóa đơn đang được đồng bộ qua Supabase.",
        )
        SnapPrimaryButton(
            text = "Đăng xuất",
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    APKBasicTheme {
        SettingsScreen()
    }
}
