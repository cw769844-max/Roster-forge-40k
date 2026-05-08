package com.rosterforge.wh40k.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.rosterforge.wh40k.domain.model.BattlefieldRole
import com.rosterforge.wh40k.domain.model.Enhancement
import com.rosterforge.wh40k.domain.model.LeaderAbility
import com.rosterforge.wh40k.domain.model.ValidationCode
import com.rosterforge.wh40k.domain.usecase.RosterTestFixtures.rosterUnit
import com.rosterforge.wh40k.domain.usecase.RosterTestFixtures.snapshot
import com.rosterforge.wh40k.domain.usecase.RosterTestFixtures.unit
import org.junit.jupiter.api.Test

class ValidateRosterUseCaseTest {

    private val sut = ValidateRosterUseCase()

    @Test
    fun `legal roster with battleline reports no issues`() {
        val intercessors = unit(
            id = "intercessors",
            name = "Intercessor Squad",
            role = BattlefieldRole.BATTLELINE,
            points = 100,
        )
        val roster = RosterTestFixtures.roster(
            units = listOf(rosterUnit(intercessors).copy(computedPoints = 100)),
        )
        val result = sut(roster, snapshot(units = listOf(intercessors)))

        assertThat(result.isLegal).isTrue()
        assertThat(result.errors).isEmpty()
    }

    @Test
    fun `points over limit produces error`() {
        val expensive = unit(id = "u1", name = "Mega Knight", points = 3000)
        val roster = RosterTestFixtures.roster(
            units = listOf(rosterUnit(expensive).copy(computedPoints = 3000)),
            pointsLimit = 2000,
        )
        val result = sut(roster, snapshot(listOf(expensive)))

        assertThat(result.isLegal).isFalse()
        assertThat(result.errors.map { it.code })
            .contains(ValidationCode.POINTS_EXCEEDED)
    }

    @Test
    fun `named character duplicate produces error per duplicate`() {
        val calgar = unit(id = "calgar", name = "Marneus Calgar", isNamed = true)
        val roster = RosterTestFixtures.roster(
            units = listOf(
                rosterUnit(calgar, idSuffix = "a").copy(computedPoints = 100),
                rosterUnit(calgar, idSuffix = "b").copy(computedPoints = 100),
                rosterUnit(calgar, idSuffix = "c").copy(computedPoints = 100),
            ),
        )
        val result = sut(roster, snapshot(listOf(calgar)))

        assertThat(result.errors.count { it.code == ValidationCode.NAMED_CHARACTER_DUPLICATE })
            .isEqualTo(2)
    }

    @Test
    fun `unit limit per roster produces error when exceeded`() {
        val rare = unit(id = "rare", name = "Rare Beast", maxPerRoster = 1)
        val roster = RosterTestFixtures.roster(
            units = listOf(
                rosterUnit(rare, idSuffix = "a"),
                rosterUnit(rare, idSuffix = "b"),
            ),
        )
        val result = sut(roster, snapshot(listOf(rare)))
        assertThat(result.errors.map { it.code })
            .contains(ValidationCode.UNIT_LIMIT_EXCEEDED)
    }

    @Test
    fun `model count below minimum produces error`() {
        val intercessors = unit(id = "u", name = "Intercessor Squad", minModels = 5, maxModels = 10)
        val roster = RosterTestFixtures.roster(
            units = listOf(rosterUnit(intercessors).copy(modelCount = 3)),
        )
        val result = sut(roster, snapshot(listOf(intercessors)))
        assertThat(result.errors.map { it.code })
            .contains(ValidationCode.MODEL_COUNT_BELOW_MINIMUM)
    }

    @Test
    fun `leader keyword mismatch produces error`() {
        val cavLeader = unit(
            id = "leader",
            name = "Mounted Hero",
            role = BattlefieldRole.CHARACTER,
            keywords = listOf("CHARACTER", "MOUNTED"),
            leaderAbility = LeaderAbility(
                effect = "...",
                attachKeywords = listOf("MOUNTED"),
            ),
        )
        val infBodyguard = unit(
            id = "bg",
            name = "Foot Squad",
            keywords = listOf("INFANTRY"),
        )
        val leaderUnit = rosterUnit(cavLeader, idSuffix = "leader")
        val bgUnit = rosterUnit(infBodyguard, idSuffix = "bg").copy(
            attachedLeaderRosterUnitId = leaderUnit.id,
        )
        val roster = RosterTestFixtures.roster(units = listOf(leaderUnit, bgUnit))
        val result = sut(roster, snapshot(listOf(cavLeader, infBodyguard)))

        assertThat(result.errors.map { it.code })
            .contains(ValidationCode.LEADER_KEYWORD_MISMATCH)
    }

    @Test
    fun `leader keyword match passes`() {
        val infLeader = unit(
            id = "leader",
            name = "Captain",
            role = BattlefieldRole.CHARACTER,
            keywords = listOf("CHARACTER", "INFANTRY"),
            leaderAbility = LeaderAbility(
                effect = "...",
                attachKeywords = listOf("INFANTRY"),
            ),
        )
        val intercessors = unit(
            id = "bg",
            name = "Intercessor Squad",
            role = BattlefieldRole.BATTLELINE,
            keywords = listOf("INFANTRY"),
        )
        val leaderUnit = rosterUnit(infLeader, idSuffix = "l")
        val bgUnit = rosterUnit(intercessors, idSuffix = "b").copy(
            attachedLeaderRosterUnitId = leaderUnit.id,
        )
        val roster = RosterTestFixtures.roster(units = listOf(leaderUnit, bgUnit))
        val result = sut(roster, snapshot(listOf(infLeader, intercessors)))

        assertThat(result.errors.filter {
            it.code == ValidationCode.LEADER_KEYWORD_MISMATCH
        }).isEmpty()
    }

    @Test
    fun `leader self attachment produces error`() {
        val infLeader = unit(
            id = "leader",
            name = "Captain",
            keywords = listOf("CHARACTER", "INFANTRY"),
            leaderAbility = LeaderAbility(effect = "...", attachKeywords = listOf("INFANTRY")),
        )
        val leaderUnit = rosterUnit(infLeader, idSuffix = "l")
        val selfAttached = leaderUnit.copy(attachedLeaderRosterUnitId = leaderUnit.id)
        val roster = RosterTestFixtures.roster(units = listOf(selfAttached))
        val result = sut(roster, snapshot(listOf(infLeader)))

        assertThat(result.errors.map { it.code })
            .contains(ValidationCode.LEADER_SELF_ATTACHMENT)
    }

    @Test
    fun `enhancement on ineligible unit produces error`() {
        val intercessors = unit(
            id = "u",
            name = "Intercessors",
            keywords = listOf("INFANTRY"),
        )
        val enhancement = Enhancement(
            id = "e1",
            detachmentId = RosterTestFixtures.detachment.id,
            factionId = RosterTestFixtures.faction.id,
            name = "Mantle of Heroes",
            points = 25,
            effect = "...",
            eligibilityKeywords = listOf("CHARACTER"),
            restrictions = emptyList(),
        )
        val rUnit = rosterUnit(intercessors).copy(selectedEnhancementId = enhancement.id)
        val roster = RosterTestFixtures.roster(units = listOf(rUnit))
        val result = sut(roster, snapshot(listOf(intercessors), enhancements = listOf(enhancement)))

        assertThat(result.errors.map { it.code })
            .contains(ValidationCode.ENHANCEMENT_INELIGIBLE)
    }

    @Test
    fun `duplicate enhancement assignment produces error`() {
        val captainA = unit(id = "u1", name = "Captain A", keywords = listOf("CHARACTER", "INFANTRY"))
        val captainB = unit(id = "u2", name = "Captain B", keywords = listOf("CHARACTER", "INFANTRY"))
        val enhancement = Enhancement(
            id = "e1",
            detachmentId = RosterTestFixtures.detachment.id,
            factionId = RosterTestFixtures.faction.id,
            name = "Adept of the Hood",
            points = 25,
            effect = "...",
            eligibilityKeywords = emptyList(),
            restrictions = emptyList(),
        )
        val a = rosterUnit(captainA, idSuffix = "a").copy(selectedEnhancementId = enhancement.id)
        val b = rosterUnit(captainB, idSuffix = "b").copy(selectedEnhancementId = enhancement.id)
        val roster = RosterTestFixtures.roster(units = listOf(a, b))
        val result = sut(
            roster,
            snapshot(listOf(captainA, captainB), enhancements = listOf(enhancement)),
        )
        assertThat(result.errors.count { it.code == ValidationCode.ENHANCEMENT_DUPLICATE })
            .isEqualTo(2)
    }

    @Test
    fun `no battleline produces warning`() {
        val character = unit(id = "u", name = "Captain", role = BattlefieldRole.CHARACTER)
        val roster = RosterTestFixtures.roster(units = listOf(rosterUnit(character)))
        val result = sut(roster, snapshot(listOf(character)))
        assertThat(result.warnings.map { it.code })
            .contains(ValidationCode.BATTLELINE_MINIMUM)
    }
}
