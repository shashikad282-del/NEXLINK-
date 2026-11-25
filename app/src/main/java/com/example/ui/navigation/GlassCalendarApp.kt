package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.signOut
import com.example.ui.components.GlassBackground
import com.example.ui.components.NexlinkPrismMark
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CalendarViewModel
import com.example.ui.screens.EventEditorSheet
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ArcticCyan
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistSilver
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SlateMuted
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

const val ROUTE_CALENDAR = "calendar"
const val ROUTE_AGENDA = "agenda"
const val ROUTE_ABOUT = "about"

private data class NavDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TopLevelDestinations = listOf(
    NavDestination(
        route = ROUTE_CALENDAR,
        labelRes = R.string.nav_calendar,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        testTag = "nav_tab_calendar"
    ),
    NavDestination(
        route = ROUTE_AGENDA,
        labelRes = R.string.nav_agenda,
        selectedIcon = Icons.Filled.ViewAgenda,
        unselectedIcon = Icons.Outlined.ViewAgenda,
        testTag = "nav_tab_agenda"
    ),
    NavDestination(
        route = ROUTE_ABOUT,
        labelRes = R.string.nav_about,
        selectedIcon = Icons.Filled.Info,
        unselectedIcon = Icons.Outlined.Info,
        testTag = "nav_tab_about"
    )
)

@Composable
fun GlassCalendarApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showSplash by remember { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = scope,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = { /* Handled gracefully by AuthScreen fallback */ }
        )
    }

    val activeStage = when {
        showSplash -> "splash"
        currentUser == null -> "auth"
        else -> "main_${currentUser!!.uid}"
    }

    AnimatedContent(
        targetState = activeStage,
        transitionSpec = {
            fadeIn(animationSpec = tween(260)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "root_stage_transition"
    ) { stage ->
        when {
            stage == "splash" -> {
                SplashScreen(onFinished = { showSplash = false })
            }
            stage == "auth" -> {
                AuthScreen(
                    onAuthSuccess = { currentUser = Firebase.auth.currentUser }
                )
            }
            else -> {
                val user = currentUser
                if (user != null) {
                    key(user.uid) {
                        AuthenticatedCalendarHost(
                            user = user,
                            onSignOut = {
                                signOut(
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onSignOutComplete = { currentUser = null }
                                )
                            }
                        )
                    }
                } else {
                    AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedCalendarHost(
    user: FirebaseUser,
    onSignOut: () -> Unit
) {
    val viewModel: CalendarViewModel = viewModel(
        key = user.uid,
        factory = CalendarViewModel.factory(user.uid)
    )

    val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val displayedMonth by viewModel.displayedMonth.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val editorRequest by viewModel.editorRequest.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_CALENDAR

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        val msg = statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val navigateTo: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    GlassBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 720.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = ObsidianElevated,
                            contentColor = IceWhite,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(
                                1.dp,
                                ArcticCyan.copy(alpha = 0.4f),
                                RoundedCornerShape(14.dp)
                            )
                        )
                    }
                },
                floatingActionButton = {
                    if (currentRoute != ROUTE_ABOUT) {
                        FloatingActionButton(
                            onClick = { viewModel.openNewEventEditor(selectedDate) },
                            containerColor = ArcticCyan,
                            contentColor = ObsidianVoid,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.testTag("fab_new_event")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.action_add_event)
                            )
                        }
                    }
                },
                bottomBar = {
                    if (!useRail) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(color = GlassBorderSubtle, thickness = 1.dp)
                            NavigationBar(
                                containerColor = ObsidianBase.copy(alpha = 0.94f),
                                contentColor = IceWhite,
                                windowInsets = NavigationBarDefaults.windowInsets
                            ) {
                                TopLevelDestinations.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    val label = stringResource(dest.labelRes)
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ArcticCyan,
                                            selectedTextColor = ArcticCyan,
                                            indicatorColor = ArcticCyan.copy(alpha = 0.16f),
                                            unselectedIconColor = SlateMuted,
                                            unselectedTextColor = SlateMuted
                                        ),
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = ObsidianBase.copy(alpha = 0.92f),
                            contentColor = IceWhite,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            NexlinkPrismMark(size = 24.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            TopLevelDestinations.forEach { dest ->
                                val selected = currentRoute == dest.route
                                val label = stringResource(dest.labelRes)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ArcticCyan,
                                        selectedTextColor = ArcticCyan,
                                        indicatorColor = ArcticCyan.copy(alpha = 0.16f),
                                        unselectedIconColor = SlateMuted,
                                        unselectedTextColor = SlateMuted
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        MinimalTopBrandBar()

                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_CALENDAR,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(ROUTE_CALENDAR) {
                                CalendarScreen(
                                    eventsState = eventsState,
                                    selectedDate = selectedDate,
                                    displayedMonth = displayedMonth,
                                    preferences = preferences,
                                    onSelectDate = viewModel::selectDate,
                                    onStepMonth = viewModel::stepMonth,
                                    onJumpToToday = viewModel::jumpToToday,
                                    onAddEvent = { date -> viewModel.openNewEventEditor(date) },
                                    onEditEvent = { event -> viewModel.openEditEventEditor(event) }
                                )
                            }

                            composable(ROUTE_AGENDA) {
                                AgendaScreen(
                                    eventsState = eventsState,
                                    searchQuery = searchQuery,
                                    selectedCategory = categoryFilter,
                                    use24Hour = preferences.use24HourFormat,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    onSelectCategory = viewModel::selectCategoryFilter,
                                    onEditEvent = { event -> viewModel.openEditEventEditor(event) }
                                )
                            }

                            composable(ROUTE_ABOUT) {
                                AboutScreen(
                                    userDisplayName = user.displayName,
                                    userEmail = user.email,
                                    preferences = preferences,
                                    onToggle24Hour = viewModel::setUse24HourFormat,
                                    onToggleSundayStart = viewModel::setWeekStartsOnSunday,
                                    onSignOut = onSignOut
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val activeEditor = editorRequest
    if (activeEditor != null) {
        EventEditorSheet(
            request = activeEditor,
            use24Hour = preferences.use24HourFormat,
            isSaving = isSaving,
            onDismiss = viewModel::closeEditor,
            onSave = viewModel::saveEvent,
            onDelete = viewModel::deleteEvent
        )
    }
}

@Composable
private fun MinimalTopBrandBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NexlinkPrismMark(size = 18.dp)
            Text(
                text = stringResource(R.string.brand_app_display),
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 2.6.sp,
                color = MistSilver
            )
        }

        Text(
            text = stringResource(R.string.brand_developer),
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 2.2.sp,
            color = SlateMuted
        )
    }
}
