package com.rosterforge.wh40k.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rosterforge.wh40k.presentation.build.BuildRosterScreen
import com.rosterforge.wh40k.presentation.create.CreateRosterScreen
import com.rosterforge.wh40k.presentation.create.DetachmentSelectScreen
import com.rosterforge.wh40k.presentation.create.FactionSelectScreen
import com.rosterforge.wh40k.presentation.enhancement.EnhancementScreen
import com.rosterforge.wh40k.presentation.home.HomeScreen
import com.rosterforge.wh40k.presentation.leader.LeaderAttachScreen
import com.rosterforge.wh40k.presentation.settings.SettingsScreen
import com.rosterforge.wh40k.presentation.stratagems.StratagemReferenceScreen
import com.rosterforge.wh40k.presentation.unit.UnitBrowserScreen
import com.rosterforge.wh40k.presentation.unit.UnitCustomizeScreen
import com.rosterforge.wh40k.presentation.validation.ValidationScreen
import com.rosterforge.wh40k.presentation.view.ViewRosterScreen

@Composable
fun RosterForgeNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onOpenRoster = { id -> navController.navigate(Screen.BuildRoster.route(id)) },
                onCreateRoster = { navController.navigate(Screen.CreateRoster.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.CreateRoster.route) {
            CreateRosterScreen(
                onContinue = { draftId ->
                    navController.navigate(Screen.FactionSelect.route(draftId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.FactionSelect.route,
            arguments = listOf(navArgument(Screen.FactionSelect.ARG_DRAFT_ID) {
                type = NavType.StringType; defaultValue = ""
            }),
        ) {
            FactionSelectScreen(
                onFactionSelected = { factionId, draftId ->
                    navController.navigate(Screen.DetachmentSelect.route(factionId, draftId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.DetachmentSelect.route,
            arguments = listOf(
                navArgument(Screen.DetachmentSelect.ARG_FACTION_ID) { type = NavType.StringType },
                navArgument(Screen.DetachmentSelect.ARG_DRAFT_ID) {
                    type = NavType.StringType; defaultValue = ""
                },
            ),
        ) {
            DetachmentSelectScreen(
                onRosterCreated = { rosterId ->
                    navController.navigate(Screen.BuildRoster.route(rosterId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.BuildRoster.route,
            arguments = listOf(navArgument(Screen.BuildRoster.ARG_ROSTER_ID) {
                type = NavType.StringType
            }),
        ) {
            BuildRosterScreen(
                onAddUnit = { rosterId ->
                    navController.navigate(Screen.UnitBrowser.route(rosterId))
                },
                onCustomizeUnit = { rosterId, rosterUnitId ->
                    navController.navigate(Screen.UnitCustomize.route(rosterId, rosterUnitId))
                },
                onAttachLeader = { rosterId, rosterUnitId ->
                    navController.navigate(Screen.LeaderAttach.route(rosterId, rosterUnitId))
                },
                onAssignEnhancement = { rosterId, rosterUnitId ->
                    navController.navigate(Screen.Enhancement.route(rosterId, rosterUnitId))
                },
                onValidate = { rosterId ->
                    navController.navigate(Screen.Validation.route(rosterId))
                },
                onViewRoster = { rosterId ->
                    navController.navigate(Screen.ViewRoster.route(rosterId))
                },
                onOpenStratagems = { rosterId ->
                    navController.navigate(Screen.StratagemReferenceForArmy.route(rosterId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.UnitBrowser.route,
            arguments = listOf(navArgument(Screen.UnitBrowser.ARG_ROSTER_ID) {
                type = NavType.StringType
            }),
        ) {
            UnitBrowserScreen(
                onUnitAdded = { rosterId, rosterUnitId ->
                    navController.navigate(Screen.UnitCustomize.route(rosterId, rosterUnitId)) {
                        popUpTo(Screen.BuildRoster.route(rosterId))
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.UnitCustomize.route,
            arguments = listOf(
                navArgument(Screen.UnitCustomize.ARG_ROSTER_ID) { type = NavType.StringType },
                navArgument(Screen.UnitCustomize.ARG_ROSTER_UNIT_ID) { type = NavType.StringType },
            ),
        ) {
            UnitCustomizeScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.LeaderAttach.route,
            arguments = listOf(
                navArgument(Screen.LeaderAttach.ARG_ROSTER_ID) { type = NavType.StringType },
                navArgument(Screen.LeaderAttach.ARG_ROSTER_UNIT_ID) { type = NavType.StringType },
            ),
        ) {
            LeaderAttachScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Enhancement.route,
            arguments = listOf(
                navArgument(Screen.Enhancement.ARG_ROSTER_ID) { type = NavType.StringType },
                navArgument(Screen.Enhancement.ARG_ROSTER_UNIT_ID) { type = NavType.StringType },
            ),
        ) {
            EnhancementScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Validation.route,
            arguments = listOf(navArgument(Screen.Validation.ARG_ROSTER_ID) {
                type = NavType.StringType
            }),
        ) {
            ValidationScreen(
                onBack = { navController.popBackStack() },
                onViewRoster = { rosterId ->
                    navController.navigate(Screen.ViewRoster.route(rosterId))
                },
            )
        }

        composable(
            route = Screen.ViewRoster.route,
            arguments = listOf(navArgument(Screen.ViewRoster.ARG_ROSTER_ID) {
                type = NavType.StringType
            }),
        ) {
            ViewRosterScreen(
                onBack = { navController.popBackStack() },
                onOpenStratagems = { rosterId ->
                    navController.navigate(Screen.StratagemReferenceForArmy.route(rosterId))
                },
            )
        }

        composable(
            route = Screen.StratagemReferenceForArmy.route,
            arguments = listOf(navArgument(Screen.StratagemReferenceForArmy.ARG_ROSTER_ID) {
                type = NavType.StringType
            }),
        ) {
            StratagemReferenceScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
