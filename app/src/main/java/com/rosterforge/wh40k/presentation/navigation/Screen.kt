package com.rosterforge.wh40k.presentation.navigation

/**
 * Route registry for all top-level destinations. Centralised here so navigation
 * arguments are typo-safe across the app.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateRoster : Screen("create_roster")
    object Settings : Screen("settings")
    object StratagemReference : Screen("stratagems")

    object FactionSelect : Screen("faction_select?draftId={draftId}") {
        const val ARG_DRAFT_ID = "draftId"
        fun route(draftId: String) = "faction_select?draftId=$draftId"
    }

    object DetachmentSelect : Screen("detachment_select/{factionId}?draftId={draftId}") {
        const val ARG_FACTION_ID = "factionId"
        const val ARG_DRAFT_ID = "draftId"
        fun route(factionId: String, draftId: String) =
            "detachment_select/$factionId?draftId=$draftId"
    }

    object BuildRoster : Screen("build/{rosterId}") {
        const val ARG_ROSTER_ID = "rosterId"
        fun route(rosterId: String) = "build/$rosterId"
    }

    object UnitBrowser : Screen("unit_browser/{rosterId}") {
        const val ARG_ROSTER_ID = "rosterId"
        fun route(rosterId: String) = "unit_browser/$rosterId"
    }

    object UnitCustomize : Screen("unit_customize/{rosterId}/{rosterUnitId}") {
        const val ARG_ROSTER_ID = "rosterId"
        const val ARG_ROSTER_UNIT_ID = "rosterUnitId"
        fun route(rosterId: String, rosterUnitId: String) =
            "unit_customize/$rosterId/$rosterUnitId"
    }

    object LeaderAttach : Screen("leader_attach/{rosterId}/{rosterUnitId}") {
        const val ARG_ROSTER_ID = "rosterId"
        const val ARG_ROSTER_UNIT_ID = "rosterUnitId"
        fun route(rosterId: String, rosterUnitId: String) =
            "leader_attach/$rosterId/$rosterUnitId"
    }

    object Enhancement : Screen("enhancement/{rosterId}/{rosterUnitId}") {
        const val ARG_ROSTER_ID = "rosterId"
        const val ARG_ROSTER_UNIT_ID = "rosterUnitId"
        fun route(rosterId: String, rosterUnitId: String) =
            "enhancement/$rosterId/$rosterUnitId"
    }

    object Validation : Screen("validation/{rosterId}") {
        const val ARG_ROSTER_ID = "rosterId"
        fun route(rosterId: String) = "validation/$rosterId"
    }

    object ViewRoster : Screen("view/{rosterId}") {
        const val ARG_ROSTER_ID = "rosterId"
        fun route(rosterId: String) = "view/$rosterId"
    }

    object StratagemReferenceForArmy : Screen("stratagems/{rosterId}") {
        const val ARG_ROSTER_ID = "rosterId"
        fun route(rosterId: String) = "stratagems/$rosterId"
    }
}
