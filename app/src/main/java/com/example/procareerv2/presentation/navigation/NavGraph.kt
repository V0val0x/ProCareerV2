package com.example.procareerv2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.procareerv2.presentation.auth.AuthViewModel
import com.example.procareerv2.presentation.auth.login.LoginScreen
import com.example.procareerv2.presentation.auth.register.RegisterScreen
import com.example.procareerv2.presentation.home.HomeScreen
import com.example.procareerv2.presentation.onboarding.OnboardingScreen
import com.example.procareerv2.presentation.profile.ProfileScreen
//import com.example.procareerv2.presentation.roadmap.RoadmapScreen
import com.example.procareerv2.presentation.splash.SplashScreen
import com.example.procareerv2.presentation.test.TestDetailScreen
import com.example.procareerv2.presentation.test.TestListScreen
import com.example.procareerv2.presentation.test.TestQuestionScreen
import com.example.procareerv2.presentation.vacancy.VacancyDetailScreen
import com.example.procareerv2.presentation.vacancy.VacancyListScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object VacancyList : Screen("vacancy_list")
    object VacancyDetail : Screen("vacancy_detail/{vacancyId}")
    object TestList : Screen("test_list")
    object TestDetail : Screen("test_detail/{testId}")
    object TestQuestion : Screen("test_question/{testId}")
    object Roadmap : Screen("roadmap")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val userState by authViewModel.user.collectAsState()
    val startDestination = Screen.Splash.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToVacancies = {
                    navController.navigate(Screen.VacancyList.route)
                },
                onNavigateToTests = {
                    navController.navigate(Screen.TestList.route)
                },
                onNavigateToRoadmap = {
                    navController.navigate(Screen.Roadmap.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.VacancyList.route) {
            VacancyListScreen(
                onNavigateToVacancyDetail = { vacancyId ->
                    navController.navigate("vacancy_detail/$vacancyId")
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.VacancyList.route) { inclusive = true }
                    }
                },
                onNavigateToTests = {
                    navController.navigate(Screen.TestList.route) {
                        popUpTo(Screen.VacancyList.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.VacancyList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.VacancyDetail.route,
            arguments = listOf(
                navArgument("vacancyId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val vacancyId = backStackEntry.arguments?.getInt("vacancyId") ?: -1
            VacancyDetailScreen(
                vacancyId = vacancyId,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.VacancyList.route) { inclusive = true }
                    }
                },
                onNavigateToVacancies = {
                    navController.navigate(Screen.VacancyList.route) {
                        popUpTo(Screen.VacancyDetail.route) { inclusive = true }
                    }
                },
                onNavigateToTests = {
                    navController.navigate(Screen.TestList.route) {
                        popUpTo(Screen.VacancyDetail.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.VacancyDetail.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TestList.route) {
            TestListScreen(
                onNavigateToTestDetail = { testId ->
                    navController.navigate("test_detail/$testId")
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.TestList.route) { inclusive = true }
                    }
                },
                onNavigateToVacancies = {
                    navController.navigate(Screen.VacancyList.route) {
                        popUpTo(Screen.TestList.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.TestList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TestDetail.route,
            arguments = listOf(
                navArgument("testId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getInt("testId") ?: -1
            TestDetailScreen(
                testId = testId,
                onStartTest = {
                    navController.navigate("test_question/$testId")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = Screen.TestQuestion.route,
            arguments = listOf(
                navArgument("testId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getInt("testId") ?: -1
            TestQuestionScreen(
                testId = testId,
                onFinishTest = {
                    navController.navigate(Screen.TestList.route) {
                        popUpTo(Screen.TestList.route) { inclusive = true }
                    }
                }
            )
        }
        /*
                composable(Screen.Roadmap.route) {
                    RoadmapScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )
                }
        */
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                },
                onNavigateToVacancies = {
                    navController.navigate(Screen.VacancyList.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                },
                onNavigateToTests = {
                    navController.navigate(Screen.TestList.route) {
                        popUpTo(Screen.Profile.route) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}