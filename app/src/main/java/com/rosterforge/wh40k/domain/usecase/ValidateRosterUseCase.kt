package com.rosterforge.wh40k.domain.usecase

import com.rosterforge.wh40k.domain.model.BattlefieldRole
import com.rosterforge.wh40k.domain.model.CatalogueSnapshot
import com.rosterforge.wh40k.domain.model.ModelScope
import com.rosterforge.wh40k.domain.model.Roster
import com.rosterforge.wh40k.domain.model.RosterUnit
import com.rosterforge.wh40k.domain.model.Severity
import com.rosterforge.wh40k.domain.model.ValidationCode
import com.rosterforge.wh40k.domain.model.ValidationIssue
import com.rosterforge.wh40k.domain.model.ValidationResult
import javax.inject.Inject

/**
 * Pure validation function. Runs every rule in [VALIDATION_LOGIC.md] against
 * a roster + catalogue snapshot and returns a [ValidationResult].
 *
 * No I/O, no side effects, fully testable.
 */
class ValidateRosterUseCase @Inject constructor() {

    operator fun invoke(roster: Roster, catalogue: CatalogueSnapshot): ValidationResult {
        val errors = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<ValidationIssue>()

        validateFactionAndDetachment(roster, catalogue, errors)
        validateModelCounts(roster, catalogue, errors)
        validateUnitLimits(roster, catalogue, errors)
        validateWargear(roster, catalogue, errors)
        validateLeaders(roster, catalogue, errors, warnings)
        validateEnhancements(roster, catalogue, errors, warnings)
        validatePoints(roster, errors)
        validateBattleline(roster, warnings)

        val unitErrorMap = (errors + warnings)
            .filter { it.affectedUnitId != null }
            .groupBy { it.affectedUnitId!! }

        return ValidationResult(
            isLegal = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            pointsUsed = roster.totalPoints,
            pointsLimit = roster.pointsLimit,
            unitErrorMap = unitErrorMap,
        )
    }

    // ─────────── Faction / Detachment ───────────

    private fun validateFactionAndDetachment(
        roster: Roster,
        catalogue: CatalogueSnapshot,
        errors: MutableList<ValidationIssue>,
    ) {
        if (catalogue.detachment.factionId != roster.factionId) {
            errors += ValidationIssue(
                code = ValidationCode.DETACHMENT_NOT_IN_FACTION,
                message = "Detachment '${catalogue.detachment.name}' does not belong to the selected faction.",
                severity = Severity.ERROR,
            )
        }
        for (rUnit in roster.units) {
            val cUnit = catalogue.unitsById[rUnit.unitId] ?: continue
            val factionMatch = cUnit.factionId == roster.factionId ||
                catalogue.faction.factionKeyword in cUnit.factionKeywords
            if (!factionMatch) {
                errors += ValidationIssue(
                    code = ValidationCode.FACTION_KEYWORD_MISMATCH,
                    message = "${cUnit.name} does not share a faction keyword with this army.",
                    affectedUnitId = rUnit.id,
                    severity = Severity.ERROR,
                )
            }
        }
    }

    // ─────────── Model counts ───────────

    private fun validateModelCounts(
        roster: Roster,
        catalogue: CatalogueSnapshot,
        errors: MutableList<ValidationIssue>,
    ) {
        for (rUnit in roster.units) {
            val cUnit = catalogue.unitsById[rUnit.unitId] ?: continue
            if (rUnit.modelCount < cUnit.minModels) {
                errors += ValidationIssue(
                    code = ValidationCode.MODEL_COUNT_BELOW_MINIMUM,
                    message = "${cUnit.name} requires a minimum of ${cUnit.minModels} models.",
                    affectedUnitId = rUnit.id,
                    severity = Severity.ERROR,
                )
            }
            if (cUnit.maxModels > 0 && rUnit.modelCount > cUnit.maxModels) {
                errors += ValidationIssue(
                    code = ValidationCode.MODEL_COUNT_ABOVE_MAXIMUM,
                    message = "${cUnit.name} may have a maximum of ${cUnit.maxModels} models.",
                    affectedUnitId = rUnit.id,
                    severity = Severity.ERROR,
                )
            }
        }
    }

    // ─────────── Per-roster unit limits ───────────

    private fun validateUnitLimits(
        roster: Roster,
        catalogue: CatalogueSnapshot,
        errors: MutableList<ValidationIssue>,
    ) {
        val countsByUnitId = roster.units.groupingBy { it.unitId }.eachCount()
        for ((unitId, count) in countsByUnitId) {
            val cUnit = catalogue.unitsById[unitId] ?: continue
            if (cUnit.isNamedCharacter && count > 1) {
                // Mark every duplicate beyond the first.
                roster.units.filter { it.unitId == unitId }.drop(1).forEach { dup ->
                    errors += ValidationIssue(
                        code = ValidationCode.NAMED_CHARACTER_DUPLICATE,
                        message = "${cUnit.name} is a unique named character and may only appear once.",
                        affectedUnitId = dup.id,
                        severity = Severity.ERROR,
                    )
                }
            }
            if (cUnit.maxPerRoster > 0 && count > cUnit.maxPerRoster) {
                errors += ValidationIssue(
                    code = ValidationCode.UNIT_LIMIT_EXCEEDED,
                    message = "${cUnit.name} may only appear ${cUnit.maxPerRoster} time(s) per roster (have $count).",
                    severity = Severity.ERROR,
                )
            }
        }
    }

    // ─────────── Wargear ───────────

    private fun validateWargear(
        roster: Roster,
        catalogue: CatalogueSnapshot,
        errors: MutableList<ValidationIssue>,
    ) {
        for (rUnit in roster.units) {
            val cUnit = catalogue.unitsById[rUnit.unitId] ?: continue
            for (option in cUnit.wargearOptions) {
                val selected = rUnit.selectedWargear.filter { it.optionId == option.id }
                val totalCount = selected.sumOf { it.count }
                if (totalCount < option.constraints.minSelections) {
                    errors += ValidationIssue(
                        code = ValidationCode.WARGEAR_BELOW_MIN_SELECTIONS,
                        message = "${cUnit.name}: '${option.description}' requires at least ${option.constraints.minSelections} selection(s).",
                        affectedUnitId = rUnit.id,
                        severity = Severity.ERROR,
                    )
                }
                if (option.constraints.maxSelections > 0 && totalCount > option.constraints.maxSelections) {
                    errors += ValidationIssue(
                        code = ValidationCode.WARGEAR_EXCEEDS_MAX_SELECTIONS,
                        message = "${cUnit.name}: too many selections in '${option.description}' (max ${option.constraints.maxSelections}).",
                        affectedUnitId = rUnit.id,
                        severity = Severity.ERROR,
                    )
                }
                if (option.constraints.modelScope == ModelScope.PER_MODEL &&
                    totalCount > rUnit.modelCount
                ) {
                    errors += ValidationIssue(
                        code = ValidationCode.WARGEAR_PER_MODEL_EXCEEDS_COUNT,
                        message = "${cUnit.name}: more selections in '${option.description}' than models in unit.",
                        affectedUnitId = rUnit.id,
                        severity = Severity.ERROR,
                    )
                }
                // Mutually-exclusive choice checks.
                val selectedIds = selected.map { it.choiceId }.toSet()
                option.choices.forEach { choice ->
                    if (choice.id in selectedIds) {
                        choice.mutuallyExclusiveWith.forEach { excluded ->
                            if (excluded in selectedIds) {
                                errors += ValidationIssue(
                                    code = ValidationCode.MUTUALLY_EXCLUSIVE_WARGEAR,
                                    message = "${cUnit.name}: '${choice.name}' cannot be combined with '${excluded}'.",
                                    affectedUnitId = rUnit.id,
                                    severity = Severity.ERROR,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ─────────── Leaders ───────────

    private fun validateLeaders(
        roster: Roster,
        catalogue: CatalogueSnapshot,
        errors: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationIssue>,
    ) {
        val rosterUnitsById = roster.units.associateBy { it.id }
        // leaderRosterUnitId -> list of bodyguard rosterUnitIds it is attached to
        val attachedLeaderIds = mutableMapOf<String, MutableList<String>>()

        for (bodyguard in roster.units) {
            val leaderId = bodyguard.attachedLeaderRosterUnitId ?: continue
            val leaderRosterUnit = rosterUnitsById[leaderId]
            if (leaderRosterUnit == null) continue
            attachedLeaderIds.getOrPut(leaderId) { mutableListOf() }.add(bodyguard.id)

            if (leaderId == bodyguard.id) {
                errors += ValidationIssue(
                    code = ValidationCode.LEADER_SELF_ATTACHMENT,
                    message = "${bodyguard.unitName} cannot be attached to itself.",
                    affectedUnitId = bodyguard.id,
                    severity = Severity.ERROR,
                )
                continue
            }
            val leaderUnit = catalogue.unitsById[leaderRosterUnit.unitId]
            val bodyguardUnit = catalogue.unitsById[bodyguard.unitId]
            if (leaderUnit == null || bodyguardUnit == null) continue

            val attachKeywords = leaderUnit.leaderAbility?.attachKeywords ?: leaderUnit.attachmentTargets
            if (attachKeywords.isNotEmpty()) {
                val bodyguardKeywords =
                    (bodyguardUnit.keywords + bodyguardUnit.factionKeywords).toSet()
                val missing = attachKeywords.filter { it !in bodyguardKeywords }
                if (missing.isNotEmpty()) {
                    errors += ValidationIssue(
                        code = ValidationCode.LEADER_KEYWORD_MISMATCH,
                        message = "${leaderUnit.name} cannot lead ${bodyguardUnit.name}. Required keyword(s): ${missing.joinToString()}.",
                        affectedUnitId = bodyguard.id,
                        severity = Severity.ERROR,
                    )
                }
            }
        }

        // Same leader RosterUnit attached to multiple bodyguards.
        attachedLeaderIds.forEach { (leaderId, bodyguards) ->
            if (bodyguards.size > 1) {
                val name = rosterUnitsById[leaderId]?.unitName ?: "Leader"
                bodyguards.forEach { bg ->
                    errors += ValidationIssue(
                        code = ValidationCode.LEADER_ALREADY_ATTACHED_ELSEWHERE,
                        message = "$name is attached to more than one unit.",
                        affectedUnitId = bg,
                        severity = Severity.ERROR,
                    )
                }
            }
        }

        // Bodyguard with multiple distinct leaders attached.
        // Each bodyguard already only points at one leader via attachedLeaderRosterUnitId,
        // so multi-leader bodyguard is not currently representable - covered by data shape.

        // Leaders that are not attached anywhere.
        for (rUnit in roster.units) {
            val cUnit = catalogue.unitsById[rUnit.unitId] ?: continue
            if (cUnit.leaderAbility != null) {
                val isAttached = roster.units.any { it.attachedLeaderRosterUnitId == rUnit.id }
                if (!isAttached) {
                    warnings += ValidationIssue(
                        code = ValidationCode.LEADER_WITHOUT_BODYGUARD,
                        message = "${cUnit.name} is a Leader but is not attached to a unit.",
                        affectedUnitId = rUnit.id,
                        severity = Severity.WARNING,
                    )
                }
            }
        }
    }

    // ─────────── Enhancements ───────────

    private fun validateEnhancements(
        roster: Roster,
        catalogue: CatalogueSnapshot,
        errors: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationIssue>,
    ) {
        val unitsById = roster.units.associateBy { it.id }

        // Eligibility + wrong-detachment.
        for (rUnit in roster.units) {
            val enhancementId = rUnit.selectedEnhancementId ?: continue
            val enhancement = catalogue.enhancementsById[enhancementId]
            val cUnit = catalogue.unitsById[rUnit.unitId]
            if (enhancement == null) {
                errors += ValidationIssue(
                    code = ValidationCode.ENHANCEMENT_INELIGIBLE,
                    message = "${rUnit.unitName} has an unknown enhancement assigned.",
                    affectedUnitId = rUnit.id,
                    severity = Severity.ERROR,
                )
                continue
            }
            if (enhancement.detachmentId != catalogue.detachment.id) {
                errors += ValidationIssue(
                    code = ValidationCode.ENHANCEMENT_WRONG_DETACHMENT,
                    message = "Enhancement '${enhancement.name}' is from a different detachment.",
                    affectedUnitId = rUnit.id,
                    severity = Severity.ERROR,
                )
            }
            if (cUnit != null && enhancement.eligibilityKeywords.isNotEmpty()) {
                val unitKw = (cUnit.keywords + cUnit.factionKeywords).toSet()
                val missing = enhancement.eligibilityKeywords.filter { it !in unitKw }
                if (missing.isNotEmpty()) {
                    errors += ValidationIssue(
                        code = ValidationCode.ENHANCEMENT_INELIGIBLE,
                        message = "${cUnit.name} cannot take '${enhancement.name}'. Requires: ${missing.joinToString()}.",
                        affectedUnitId = rUnit.id,
                        severity = Severity.ERROR,
                    )
                }
            }
        }

        // Duplicate enhancement: same id assigned twice.
        val assigned = roster.units.mapNotNull { ru -> ru.selectedEnhancementId?.let { it to ru } }
        val grouped = assigned.groupBy({ it.first }, { it.second })
        grouped.forEach { (eId, units) ->
            if (units.size > 1) {
                val name = catalogue.enhancementsById[eId]?.name ?: "Enhancement"
                units.forEach { u ->
                    errors += ValidationIssue(
                        code = ValidationCode.ENHANCEMENT_DUPLICATE,
                        message = "'$name' may only be taken once per army.",
                        affectedUnitId = u.id,
                        severity = Severity.ERROR,
                    )
                }
            }
        }

        // Standard 10e cap of 3 enhancements per army.
        val totalEnhancements = assigned.size
        if (totalEnhancements > 3) {
            warnings += ValidationIssue(
                code = ValidationCode.ENHANCEMENT_LIMIT_EXCEEDED,
                message = "More than 3 enhancements selected ($totalEnhancements). Confirm the event pack allows this.",
                severity = Severity.WARNING,
            )
        }
    }

    // ─────────── Points ───────────

    private fun validatePoints(roster: Roster, errors: MutableList<ValidationIssue>) {
        if (roster.totalPoints > roster.pointsLimit) {
            errors += ValidationIssue(
                code = ValidationCode.POINTS_EXCEEDED,
                message = "Army is ${roster.totalPoints - roster.pointsLimit} pts over the ${roster.pointsLimit} pt limit.",
                severity = Severity.ERROR,
            )
        }
    }

    // ─────────── Battleline (warning-level) ───────────

    private fun validateBattleline(roster: Roster, warnings: MutableList<ValidationIssue>) {
        if (roster.units.none { it.role == BattlefieldRole.BATTLELINE }) {
            warnings += ValidationIssue(
                code = ValidationCode.BATTLELINE_MINIMUM,
                message = "No Battleline units in this army.",
                severity = Severity.WARNING,
            )
        }
    }
}

