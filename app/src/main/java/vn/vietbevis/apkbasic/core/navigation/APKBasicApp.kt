package vn.vietbevis.apkbasic.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import vn.vietbevis.apkbasic.R
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.feature.accounts.AccountsScreen
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
import vn.vietbevis.apkbasic.ui.theme.SnapCoral
import vn.vietbevis.apkbasic.ui.theme.SnapCream
import vn.vietbevis.apkbasic.ui.theme.SnapNavy
import vn.vietbevis.apkbasic.ui.theme.SnapWhite

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
        else -> MainAppShell(
            appContainer = appContainer,
            userProfile = requireNotNull(authState.authenticatedProfile),
            onSignOut = authViewModel::signOut,
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapCream),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SnapCoral)
    }
}

@Composable
private fun MainAppShell(
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapCream),
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
            )
            AppDestination.STATISTICS -> StatisticsScreen(
                modifier = contentModifier,
                appContainer = appContainer,
            )
            AppDestination.ACCOUNTS -> AccountsScreen(
                modifier = contentModifier,
                appContainer = appContainer,
                userProfile = userProfile,
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
        color = SnapNavy,
        contentColor = SnapWhite,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 34.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppDestination.entries.forEach { destination ->
                val selected = currentDestination == destination
                Surface(
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = if (selected) SnapCoral else SnapNavy,
                    contentColor = SnapWhite,
                    border = if (selected) null else BorderStroke(1.dp, SnapNavy),
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
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapCream)
            .statusBarsPadding(),
    ) {
        SnapTopBar(
            title = stringResource(R.string.destination_capture),
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
