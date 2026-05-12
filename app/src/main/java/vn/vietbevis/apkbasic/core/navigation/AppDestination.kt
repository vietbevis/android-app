package vn.vietbevis.apkbasic.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import vn.vietbevis.apkbasic.R

enum class AppDestination(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    HOME(R.string.destination_home, R.drawable.ic_home),
    STATISTICS(R.string.destination_statistics, R.drawable.ic_chart),
    ACCOUNTS(R.string.destination_accounts, R.drawable.ic_wallet),
    BUDGETS(R.string.destination_budgets, R.drawable.ic_budget),
    PROFILE(R.string.destination_profile, R.drawable.ic_profile),
}
