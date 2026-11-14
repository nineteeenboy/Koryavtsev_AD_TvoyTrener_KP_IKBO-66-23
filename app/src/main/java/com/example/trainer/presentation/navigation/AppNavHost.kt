package com.example.trainer.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.trainer.presentation.auth.*
import com.example.trainer.presentation.booking.*
import com.example.trainer.presentation.profile.*
import com.example.trainer.presentation.review.*
import com.example.trainer.presentation.trainers.*
import com.example.trainer.presentation.landing.LandingScreen
import com.example.trainer.presentation.application.TrainerApplicationScreen
import com.example.trainer.presentation.application.TrainerApplicationPendingScreen
import com.example.trainer.presentation.application.*
import com.example.trainer.data.remote.dto.TrainerApplicationRequest
import com.example.trainer.presentation.components.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import com.example.trainer.util.UiState

/** Complete Compose navigation graph; authentication clears the previous back stack. */
@Composable
fun AppNavHost(dark: Boolean?, onDark: (Boolean) -> Unit) {
    val nav = rememberNavController()
    val auth: AuthViewModel = hiltViewModel()
    val authState by auth.state.collectAsStateWithLifecycle()
    val start = "landing"
    fun enterCatalog() {
        nav.navigate("trainers") { popUpTo(nav.graph.id) { inclusive = true }; launchSingleTop = true }
        auth.reset()
    }
    NavHost(nav, startDestination = start) {
        composable("landing") {
            val vm: TrainersViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.load() }
            LandingScreen(state, auth.signedIn,
                { nav.navigate(if (auth.signedIn) "trainers" else "login") },
                { nav.navigate("trainer/$it") }, { vm.load() })
        }
        composable("login") {
            LaunchedEffect(authState) { if (authState is UiState.Success) enterCatalog() }
            LoginScreen(authState, auth::login, onRegister = { auth.reset(); nav.navigate("register") })
        }
        composable("register") {
            LaunchedEffect(authState) { if (authState is UiState.Success) enterCatalog() }
            RegisterScreen(authState, auth::register, onBack = { auth.reset(); nav.popBackStack() })
        }
        composable("trainers") {
            val vm: TrainersViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.load() }
            TrainerListScreen(state, vm::load, { nav.navigate("trainer/$it") },
                { nav.navigate("bookings") }, { nav.navigate("profile") },
                onBecomeTrainer = { nav.navigate(if (auth.signedIn) "trainerApplication" else "login") })
        }
        composable("trainer/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = requireNotNull(entry.arguments).getInt("id")
            val vm: TrainersViewModel = hiltViewModel()
            val state by vm.detailState.collectAsStateWithLifecycle()
            val reviews by vm.reviewsState.collectAsStateWithLifecycle()
            LaunchedEffect(id) { vm.loadDetail(id) }
            TrainerDetailScreen(state, reviews, { vm.loadDetail(id) },
                { nav.navigate(if (auth.signedIn) "booking/$id" else "login") },
                { nav.navigate(if (auth.signedIn) "review/$id" else "login") }, { nav.popBackStack() })
        }
        composable("booking/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = requireNotNull(entry.arguments).getInt("id")
            val vm: BookingViewModel = hiltViewModel()
            val slots by vm.slots.collectAsStateWithLifecycle()
            val action by vm.action.collectAsStateWithLifecycle()
            LaunchedEffect(action) {
                if (action is UiState.Success) nav.navigate("bookings") { popUpTo("booking/$id") { inclusive = true } }
            }
            BookingScreen(slots, action, { vm.loadSlots(id, it) }, { vm.reserve(id, it) }, { nav.popBackStack() })
        }
        composable("bookings") {
            val vm: BookingViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val cancel by vm.cancelState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.load() }
            MyBookingsScreen(state, cancel, vm::load, vm::cancel, { nav.navigate("review/$it") }, { nav.popBackStack() })
        }
        composable("review/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = requireNotNull(entry.arguments).getInt("id")
            val vm: ReviewViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            LaunchedEffect(state) { if (state is UiState.Success) nav.popBackStack() }
            ReviewScreen(state, { rating, text -> vm.submit(id, rating, text) }, { nav.popBackStack() })
        }
        composable("profile") {
            val vm: ProfileViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val reviews by vm.reviewState.collectAsStateWithLifecycle()
            val loggedOut by vm.loggedOut.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.load() }
            LaunchedEffect(loggedOut) {
                if (loggedOut) {
                    auth.reset()
                    nav.navigate("landing") { popUpTo(nav.graph.id) { inclusive = true }; launchSingleTop = true }
                }
            }
            ProfileScreen(state, reviews, dark, onDark, vm::load, vm::logout, { nav.popBackStack() },
                onBecomeTrainer = { nav.navigate("trainerApplication") },
                onAdmin = { nav.navigate("adminApplications") }, onDiary = { nav.navigate("trainingDiary") })
        }
        composable("trainerApplication") {
            val vm: TrainerWorkspaceViewModel = hiltViewModel()
            val state by vm.applications.collectAsStateWithLifecycle()
            val action by vm.action.collectAsStateWithLifecycle()
            var edit by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { vm.loadApplications() }
            when (val current = state) {
                is UiState.Success -> {
                    val application = current.data.firstOrNull()
                    if (application == null || (edit && application.status == "REJECTED")) {
                        TrainerApplicationScreen(vm::submit, { nav.popBackStack() }, action,
                            application?.let { TrainerApplicationRequest(it.email, it.phone, it.experience,
                                it.education, it.certificates, it.specialization, it.experienceYears, it.price) })
                    } else ApplicationStatusScreen(application, onEdit = { edit = true; vm.resetAction() },
                        onDiary = { nav.navigate("trainingDiary") }, onRefresh = { vm.loadApplications() },
                        onBack = { nav.popBackStack() })
                }
                else -> ScreenFrame("Моя заявка", { nav.popBackStack() }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        StateContent(state, { vm.loadApplications() }) {}
                    }
                }
            }
        }
        composable("adminApplications") {
            val vm: TrainerWorkspaceViewModel = hiltViewModel()
            val state by vm.applications.collectAsStateWithLifecycle()
            val action by vm.action.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.loadApplications(admin = true) }
            AdminApplicationsScreen(state, action, vm::decide, vm::resetAction,
                { vm.loadApplications(admin = true) }, { nav.popBackStack() })
        }
        composable("trainingDiary") {
            val vm: TrainerWorkspaceViewModel = hiltViewModel()
            val state by vm.sessions.collectAsStateWithLifecycle()
            val action by vm.action.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { vm.loadSessions() }
            TrainingDiaryScreen(state, action, vm::saveNote, vm::resetAction,
                vm::loadSessions, { nav.popBackStack() })
        }
    }
}

