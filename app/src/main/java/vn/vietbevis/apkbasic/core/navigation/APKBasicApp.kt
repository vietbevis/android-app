package vn.vietbevis.apkbasic.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
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
import vn.vietbevis.apkbasic.feature.accounts.AccountsScreen
import vn.vietbevis.apkbasic.ui.theme.CapBackground
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme

@Composable
fun APKBasicApp(appContainer: AppContainer = remember { AppContainer() }) {
    val authViewModel = remember {
        AuthViewModel(
            authRepository = appContainer.authRepository,
            onboardingBootstrapper = appContainer.onboardingBootstrapper,
        )
    }
    val authState by authViewModel.uiState.collectAsState()

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
        else -> MainAppScaffold(
            appContainer = appContainer,
            userProfile = requireNotNull(authState.authenticatedProfile),
            onSignOut = authViewModel::signOut,
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MainAppScaffold(
    appContainer: AppContainer,
    userProfile: UserProfile,
    onSignOut: () -> Unit,
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }
    var showCapture by rememberSaveable { mutableStateOf(false) }

    if (showCapture) {
        CaptureModalContent(
            appContainer = appContainer,
            userProfile = userProfile,
            onClose = { showCapture = false },
        )
        return
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestination.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(destination.iconRes),
                            contentDescription = stringResource(destination.labelRes),
                        )
                    },
                    label = { Text(stringResource(destination.labelRes)) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination },
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CapBackground,
            floatingActionButton = {
                FloatingActionButton(onClick = { showCapture = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = stringResource(R.string.action_add_transaction),
                    )
                }
            },
        ) { innerPadding ->
            val modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            when (currentDestination) {
                AppDestination.HOME -> HomeScreen(
                    modifier = modifier,
                    appContainer = appContainer,
                    userProfile = userProfile,
                    onOpenCapture = { showCapture = true },
                )
                AppDestination.STATISTICS -> StatisticsScreen(
                    modifier = modifier,
                    appContainer = appContainer,
                )
                AppDestination.ACCOUNTS -> AccountsScreen(
                    modifier = modifier,
                    appContainer = appContainer,
                    userProfile = userProfile,
                )
                AppDestination.BUDGETS -> BudgetsScreen(
                    modifier = modifier,
                    appContainer = appContainer,
                    userProfile = userProfile,
                )
                AppDestination.PROFILE -> ProfileScreen(
                    modifier = modifier,
                    appContainer = appContainer,
                    userProfile = userProfile,
                    onSignOut = onSignOut,
                )
            }
        }
    }
}

@Composable
private fun CaptureModalContent(
    appContainer: AppContainer,
    userProfile: UserProfile,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CapBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.action_cancel))
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.destination_capture))
        }
        Spacer(Modifier.height(4.dp))
        CaptureScreen(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            appContainer = appContainer,
            userProfile = userProfile,
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
