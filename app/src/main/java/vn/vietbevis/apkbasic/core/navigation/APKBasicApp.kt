package vn.vietbevis.apkbasic.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.feature.auth.AuthScreen
import vn.vietbevis.apkbasic.feature.auth.AuthViewModel
import vn.vietbevis.apkbasic.feature.budgets.BudgetsScreen
import vn.vietbevis.apkbasic.feature.capture.CaptureScreen
import vn.vietbevis.apkbasic.feature.home.HomeScreen
import vn.vietbevis.apkbasic.feature.profile.ProfileScreen
import vn.vietbevis.apkbasic.feature.statistics.StatisticsScreen
import vn.vietbevis.apkbasic.ui.components.SnapIconButton
import vn.vietbevis.apkbasic.ui.components.SnapTopBar
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme

@Composable
fun APKBasicApp() {
    val context = LocalContext.current
    val appContainer = remember { AppContainer(context) }
    val authViewModel = remember {
        AuthViewModel(
            authRepository = appContainer.authRepository,
            onboardingBootstrapper = appContainer.onboardingBootstrapper,
        )
    }
    val authState by authViewModel.uiState.collectAsState()
    val userPreference by appContainer.userPreferenceRepository.preferencesFlow.collectAsState(initial = null)

    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (userPreference?.themeMode) {
        vn.vietbevis.apkbasic.domain.model.ThemeMode.DARK -> true
        vn.vietbevis.apkbasic.domain.model.ThemeMode.LIGHT -> false
        else -> isSystemDark
    }

    val languageCode = if (userPreference?.language == vn.vietbevis.apkbasic.domain.model.AppLanguage.ENGLISH) "en" else "vi"
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = java.util.Locale(languageCode)
    if (configuration.locales.get(0)?.language != locale.language) {
        java.util.Locale.setDefault(locale)
        configuration.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    APKBasicTheme(darkTheme = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            when {
                authState.isLoading && authState.authenticatedProfile == null -> LoadingScreen()
                !authState.isAuthenticated -> AuthScreen(
                    uiState = authState,
                    onEmailChange = authViewModel::onEmailChange,
                    onPasswordChange = authViewModel::onPasswordChange,
                    onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
                    onToggleMode = authViewModel::toggleMode,
                    onSubmit = authViewModel::submit,
                )
                else -> MainAppShell(
                    appContainer = appContainer,
                    userProfile = requireNotNull(authState.authenticatedProfile),
                    onSignOut = authViewModel::signOut,
                    onProfileUpdated = authViewModel::updateProfile,
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun MainAppShell(
    appContainer: AppContainer,
    userProfile: UserProfile,
    onSignOut: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit,
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }
    var showCapture by rememberSaveable { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<vn.vietbevis.apkbasic.domain.model.Transaction?>(null) }

    if (showCapture || editingTransaction != null) {
        CaptureModalContent(
            appContainer = appContainer,
            userProfile = userProfile,
            initialTransaction = editingTransaction,
            onClose = { 
                showCapture = false
                editingTransaction = null
            },
        )
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        val contentModifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()

        when (currentDestination) {
            AppDestination.HOME -> HomeScreen(
                modifier = contentModifier,
                appContainer = appContainer,
                userProfile = userProfile,
                onOpenCapture = { showCapture = true },
                onOpenProfile = { currentDestination = AppDestination.PROFILE },
                onEditTransaction = { editingTransaction = it },
                onOpenBudgetDetail = { currentDestination = AppDestination.BUDGETS }
            )
            AppDestination.STATISTICS -> StatisticsScreen(
                modifier = contentModifier,
                appContainer = appContainer,
                userProfile = userProfile,
            )
            AppDestination.TRANSACTIONS -> vn.vietbevis.apkbasic.feature.transactions.TransactionsScreen(
                modifier = contentModifier,
                appContainer = appContainer,
                onEditTransaction = { editingTransaction = it }
            )
            AppDestination.BUDGETS -> BudgetsScreen(
                modifier = contentModifier,
                appContainer = appContainer,
                userProfile = userProfile,
            )
            AppDestination.PROFILE -> ProfileScreen(
                modifier = contentModifier,
                appContainer = appContainer,
                userProfile = userProfile,
                onSignOut = onSignOut,
                onProfileUpdated = onProfileUpdated
            )
        }

        SnapBottomBar(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun SnapBottomBar(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10000.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppDestination.entries.forEach { destination ->
                val selected = currentDestination == destination
                Surface(
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(destination.iconRes),
                            contentDescription = stringResource(destination.labelRes),
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureModalContent(
    appContainer: AppContainer,
    userProfile: UserProfile,
    initialTransaction: vn.vietbevis.apkbasic.domain.model.Transaction? = null,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        SnapTopBar(
            title = if (initialTransaction == null) stringResource(R.string.destination_capture) else stringResource(R.string.capture_edit_transaction),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            navigationIcon = {
                SnapIconButton(
                    iconRes = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.action_cancel),
                    onClick = onClose,
                )
            },
        )
        CaptureScreen(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            appContainer = appContainer,
            userProfile = userProfile,
            initialTransaction = initialTransaction,
            onFinish = onClose
        )
    }
}

@PreviewScreenSizes
@Composable
private fun APKBasicAppPreview() {
    APKBasicTheme {
        LoadingScreen()
    }
}
