package vn.vietbevis.apkbasic.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SnapLightColorScheme = lightColorScheme(
    primary = SnapCoral,
    onPrimary = SnapWhite,
    primaryContainer = SnapCoralSoft,
    onPrimaryContainer = SnapNavy,
    secondary = SnapMint,
    onSecondary = SnapNavy,
    secondaryContainer = SnapSoftYellow,
    onSecondaryContainer = SnapNavy,
    tertiary = SnapYellow,
    onTertiary = SnapNavy,
    tertiaryContainer = SnapSoftYellow,
    onTertiaryContainer = SnapNavy,
    background = SnapCream,
    onBackground = SnapNavy,
    surface = SnapCream,
    onSurface = SnapNavy,
    surfaceVariant = SnapCreamSurface,
    onSurfaceVariant = SnapSlate,
    surfaceContainer = SnapCreamSurface,
    surfaceContainerHigh = SnapSoftYellow,
    outline = SnapBorder,
    outlineVariant = SnapBorderSoft,
    error = SnapError,
    onError = SnapWhite,
    errorContainer = SnapError.copy(alpha = 0.14f),
    onErrorContainer = SnapError,
)

private val SnapDarkColorScheme = darkColorScheme(
    primary = SnapCoral,
    onPrimary = SnapWhite,
    primaryContainer = SnapNavy,
    onPrimaryContainer = SnapWhite,
    secondary = SnapMint,
    onSecondary = SnapNavy,
    tertiary = SnapYellow,
    onTertiary = SnapNavy,
    background = SnapNavy,
    onBackground = SnapWhite,
    surface = SnapNavy,
    onSurface = SnapWhite,
    surfaceVariant = SnapSlate,
    onSurfaceVariant = SnapCoralSoft,
    surfaceContainer = SnapNavy,
    surfaceContainerHigh = SnapSlate,
    outline = SnapBorder,
    error = SnapError,
)

@Composable
fun APKBasicTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isSystemInDarkTheme() -> {
            dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> SnapDarkColorScheme
        else -> SnapLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
