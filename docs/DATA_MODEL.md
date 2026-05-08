# Roster Forge 40K — Data Model

## 1. Overview

There are two parallel model hierarchies:

| Tier | Purpose | Location |
|---|---|---|
| **Room Entities** | Persist data to SQLite | `data/local/entity/` |
| **Domain Models** | Business logic and UI state | `domain/model/` |

Room entities contain all columns needed for storage (including foreign keys and denormalised lookup fields). Domain models are clean Kotlin data classes that the UI and use cases operate on. Mappers in the repository layer translate between tiers.

---

## 2. Domain Models

### 2.1 Catalogue Models

```kotlin
data class Faction(
    val id: String,
    val name: String,
    val abbreviation: String,
    val factionKeyword: String,           // e.g. "ADEPTUS ASTARTES"
    val subFactions: List<String>,        // e.g. chapter keywords
    val allegiance: Allegiance,           // IMPERIUM, CHAOS, XENOS, UNALIGNED
)

data class Detachment(
    val id: String,
    val factionId: String,
    val name: String,
    val detachmentRule: DetachmentRule,
    val enhancements: List<Enhancement>,
    val stratagems: List<Stratagem>,
)

data class DetachmentRule(
    val name: String,
    val effect: String,
)

data class Unit(
    val id: String,
    val factionId: String,
    val name: String,
    val role: BattlefieldRole,
    val keywords: List<String>,
    val factionKeywords: List<String>,
    val stats: UnitStats,
    val modelGroups: List<ModelGroup>,    // supports multi-model units
    val weapons: List<WeaponProfile>,
    val abilities: List<Ability>,
    val wargearOptions: List<WargearOption>,
    val pointsCosts: List<PointsCost>,    // variable per model count
    val leaderAbility: LeaderAbility?,    // null if not a leader
    val attachmentTargets: List<String>,  // keyword constraints for bodyguard unit
    val isNamedCharacter: Boolean,
    val maxPerRoster: Int,                // 0 = no limit
    val minModels: Int,
    val maxModels: Int,
)

data class ModelGroup(
    val name: String,
    val minCount: Int,
    val maxCount: Int,
)

data class UnitStats(
    val movement: String,
    val toughness: Int,
    val save: String,
    val invulnerableSave: String?,
    val wounds: Int,
    val leadership: String,
    val objectiveControl: Int,
)

data class WeaponProfile(
    val id: String,
    val name: String,
    val type: WeaponType,               // RANGED / MELEE
    val range: String,
    val attacks: String,
    val skill: String,                  // BS or WS
    val strength: String,
    val ap: String,
    val damage: String,
    val keywords: List<String>,
    val abilities: List<String>,
)

enum class WeaponType { RANGED, MELEE }

data class WargearOption(
    val id: String,
    val description: String,
    val constraints: WargearConstraints,
    val replacements: List<WargearReplacement>,
    val additions: List<WargearAddition>,
)

data class WargearConstraints(
    val minSelections: Int = 0,
    val maxSelections: Int = 1,
    val modelScope: ModelScope,         // PER_MODEL, PER_UNIT, N_MODELS(n)
    val conditions: List<WargearCondition>,
)

data class PointsCost(
    val modelCount: Int,
    val points: Int,
)

data class Ability(
    val id: String,
    val name: String,
    val effect: String,
    val type: AbilityType,              // FACTION, DETACHMENT, UNIT, CORE, DAMAGED
    val phase: GamePhase?,
)

data class LeaderAbility(
    val effect: String,
    val attachKeywords: List<String>,   // keywords the bodyguard unit must have
)

data class Enhancement(
    val id: String,
    val detachmentId: String,
    val name: String,
    val points: Int,
    val effect: String,
    val eligibilityKeywords: List<String>,
    val restrictions: List<String>,
)

data class Stratagem(
    val id: String,
    val factionId: String?,             // null = core stratagem
    val detachmentId: String?,          // null = faction-wide
    val name: String,
    val cp: Int,
    val type: StratagemType,
    val phase: GamePhase,
    val target: String,
    val effect: String,
    val restrictions: String,
    val flavor: String?,
)

enum class StratagemType {
    BATTLE_TACTIC, STRATEGIC_PLOY, EPIC_DEED, WARGEAR, STRATEGIC
}

enum class GamePhase {
    ANY, COMMAND, MOVEMENT, SHOOTING, CHARGE, FIGHT, MORALE
}

enum class BattlefieldRole {
    CHARACTER, BATTLELINE, INFANTRY, MOUNTED, VEHICLE, MONSTER, FLY,
    DEDICATED_TRANSPORT, ALLIED_UNITS, FORTIFICATION
}

enum class Allegiance { IMPERIUM, CHAOS, XENOS, UNALIGNED }
```

---

### 2.2 Roster Models

```kotlin
data class Roster(
    val id: String,
    val name: String,
    val factionId: String,
    val factionName: String,
    val detachmentId: String,
    val detachmentName: String,
    val pointsLimit: Int,
    val units: List<RosterUnit>,
    val createdAt: Long,
    val updatedAt: Long,
)

data class RosterUnit(
    val id: String,                     // UUID, unique per roster slot
    val rosterId: String,
    val unitId: String,                 // references Unit.id
    val unitName: String,               // denormalised for display
    val role: BattlefieldRole,
    val modelCount: Int,
    val selectedWargear: List<SelectedWargear>,
    val attachedLeaderRosterUnitId: String?,   // ID of the leader RosterUnit
    val selectedEnhancementId: String?,
    val computedPoints: Int,            // recalculated on every change
    val customNotes: String?,
)

data class SelectedWargear(
    val optionId: String,
    val choiceId: String,
    val modelScope: String,
    val count: Int,
)

data class ValidationResult(
    val isLegal: Boolean,
    val errors: List<ValidationIssue>,
    val warnings: List<ValidationIssue>,
)

data class ValidationIssue(
    val code: ValidationCode,
    val message: String,
    val affectedUnitId: String?,        // null = roster-level issue
    val severity: Severity,
)

enum class Severity { ERROR, WARNING, INFO }

enum class ValidationCode {
    POINTS_EXCEEDED,
    NAMED_CHARACTER_DUPLICATE,
    UNIT_LIMIT_EXCEEDED,
    LEADER_KEYWORD_MISMATCH,
    LEADER_ALREADY_ATTACHED,
    ENHANCEMENT_INELIGIBLE,
    ENHANCEMENT_DUPLICATE,
    WARGEAR_CONSTRAINT_VIOLATED,
    FACTION_KEYWORD_MISMATCH,
    DETACHMENT_UNIT_NOT_ALLOWED,
    MISSING_REQUIRED_SELECTION,
    MANDATORY_WARGEAR_MISSING,
}
```

---

## 3. Room Entities and Schema

### Database: `RosterForgeDatabase` (version 1)

```kotlin
// ─────────────── CATALOGUE TABLES ───────────────

@Entity(tableName = "factions")
data class FactionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val abbreviation: String,
    val factionKeyword: String,
    val allegiance: String,
    val subFactionsJson: String,        // JSON array
)

@Entity(tableName = "detachments",
        foreignKeys = [ForeignKey(entity = FactionEntity::class,
            parentColumns = ["id"], childColumns = ["factionId"],
            onDelete = CASCADE)])
@Index("factionId")
data class DetachmentEntity(
    @PrimaryKey val id: String,
    val factionId: String,
    val name: String,
    val ruleName: String,
    val ruleEffect: String,
)

@Entity(tableName = "units",
        foreignKeys = [ForeignKey(entity = FactionEntity::class,
            parentColumns = ["id"], childColumns = ["factionId"],
            onDelete = CASCADE)])
@Index("factionId")
data class UnitEntity(
    @PrimaryKey val id: String,
    val factionId: String,
    val name: String,
    val role: String,
    val keywordsJson: String,
    val factionKeywordsJson: String,
    val statsJson: String,
    val modelGroupsJson: String,
    val weaponsJson: String,
    val abilitiesJson: String,
    val wargearOptionsJson: String,
    val pointsCostsJson: String,
    val leaderAbilityJson: String?,
    val attachmentTargetsJson: String,
    val isNamedCharacter: Boolean,
    val maxPerRoster: Int,
    val minModels: Int,
    val maxModels: Int,
)

@Entity(tableName = "enhancements",
        foreignKeys = [ForeignKey(entity = DetachmentEntity::class,
            parentColumns = ["id"], childColumns = ["detachmentId"],
            onDelete = CASCADE)])
@Index("detachmentId")
data class EnhancementEntity(
    @PrimaryKey val id: String,
    val detachmentId: String,
    val factionId: String,
    val name: String,
    val points: Int,
    val effect: String,
    val eligibilityKeywordsJson: String,
    val restrictionsJson: String,
)

@Entity(tableName = "stratagems")
@Index("factionId", "detachmentId")
data class StratagemEntity(
    @PrimaryKey val id: String,
    val factionId: String?,
    val detachmentId: String?,
    val name: String,
    val cp: Int,
    val type: String,
    val phase: String,
    val target: String,
    val effect: String,
    val restrictions: String,
    val flavor: String?,
)

// ─────────────── ROSTER TABLES ───────────────

@Entity(tableName = "rosters")
data class RosterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val factionId: String,
    val factionName: String,
    val detachmentId: String,
    val detachmentName: String,
    val pointsLimit: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "roster_units",
        foreignKeys = [ForeignKey(entity = RosterEntity::class,
            parentColumns = ["id"], childColumns = ["rosterId"],
            onDelete = CASCADE)])
@Index("rosterId")
data class RosterUnitEntity(
    @PrimaryKey val id: String,
    val rosterId: String,
    val unitId: String,
    val unitName: String,
    val role: String,
    val modelCount: Int,
    val selectedWargearJson: String,
    val attachedLeaderRosterUnitId: String?,
    val selectedEnhancementId: String?,
    val computedPoints: Int,
    val customNotes: String?,
    val sortOrder: Int,
)

// ─────────────── METADATA ───────────────

@Entity(tableName = "data_metadata")
data class DataMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)
// Keys: "installed_version", "installed_date", "game_system_name"
```

---

## 4. DAO Interfaces

```kotlin
@Dao
interface RosterDao {
    @Query("SELECT * FROM rosters ORDER BY updatedAt DESC")
    fun getAllRosters(): Flow<List<RosterEntity>>

    @Query("SELECT * FROM rosters WHERE id = :id")
    fun getRosterById(id: String): Flow<RosterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoster(roster: RosterEntity)

    @Delete
    suspend fun deleteRoster(roster: RosterEntity)

    @Transaction
    suspend fun duplicateRoster(sourceId: String, newId: String, newName: String)
}

@Dao
interface RosterUnitDao {
    @Query("SELECT * FROM roster_units WHERE rosterId = :rosterId ORDER BY sortOrder ASC")
    fun getUnitsForRoster(rosterId: String): Flow<List<RosterUnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRosterUnit(unit: RosterUnitEntity)

    @Delete
    suspend fun deleteRosterUnit(unit: RosterUnitEntity)

    @Query("UPDATE roster_units SET computedPoints = :points WHERE id = :id")
    suspend fun updatePoints(id: String, points: Int)
}

@Dao
interface CatalogueDao {
    @Query("SELECT * FROM factions ORDER BY name ASC")
    suspend fun getAllFactions(): List<FactionEntity>

    @Query("SELECT * FROM detachments WHERE factionId = :factionId")
    suspend fun getDetachmentsForFaction(factionId: String): List<DetachmentEntity>

    @Query("SELECT * FROM units WHERE factionId = :factionId")
    suspend fun getUnitsForFaction(factionId: String): List<UnitEntity>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getUnitById(id: String): UnitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun replaceAllUnits(units: List<UnitEntity>)
}

@Dao
interface StratagemDao {
    @Query("""
        SELECT * FROM stratagems 
        WHERE factionId IS NULL 
           OR factionId = :factionId 
           OR detachmentId = :detachmentId
        ORDER BY cp ASC, name ASC
    """)
    fun getStrategems(factionId: String, detachmentId: String): Flow<List<StratagemEntity>>

    @Query("SELECT * FROM stratagems WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<StratagemEntity>
}
```

---

## 5. DataStore Schema (Proto)

```protobuf
// app_settings.proto
message AppSettings {
    string installed_data_version = 1;
    int64  installed_data_date    = 2;
    string theme                  = 3;  // "system" | "light" | "dark"
    bool   show_points_bar        = 4;
    bool   auto_check_updates     = 5;
    string last_used_faction_id   = 6;
}
```

---

## 6. Key Relationships

```
Faction 1──* Detachment
Faction 1──* Unit
Detachment 1──* Enhancement
Detachment 1──* Stratagem
Faction 1──* Stratagem       (faction-wide stratagems, detachmentId = null)
Stratagem (core)             (factionId = null, detachmentId = null)

Roster 1──* RosterUnit
RosterUnit 0..1──1 RosterUnit  (leader attachment: leader → bodyguard)
RosterUnit 0..1──1 Enhancement
RosterUnit 1──* SelectedWargear
```

---

## 7. Points Calculation

Points are recalculated eagerly whenever a unit's model count or wargear changes:

```
1. Find matching PointsCost for current modelCount.
2. Add enhancement.points if one is selected.
3. Add any per-model wargear costs (rare in 10e, handled as points modifier).
4. Store result in RosterUnitEntity.computedPoints.
5. Roster total = SUM(computedPoints) across all RosterUnits.
```

In 10th edition, wargear is generally free and does not affect points. The model only handles wargear points if the parsed data includes a cost entry (future-proofing).
