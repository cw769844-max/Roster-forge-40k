# Roster Forge 40K — Validation Logic Plan

## 1. Overview

Validation runs reactively: any change to the roster triggers a full re-validation via `ValidateRosterUseCase`. The result is a `ValidationResult` containing ordered lists of `ERROR` and `WARNING` issues.

Validation is **pure** — it takes a `Roster` domain object and a `CatalogueSnapshot` and returns a `ValidationResult` with no side effects. This makes it trivially unit-testable.

```kotlin
class ValidateRosterUseCase @Inject constructor() {
    operator fun invoke(roster: Roster, catalogue: CatalogueSnapshot): ValidationResult
}
```

---

## 2. Validation Rules Catalogue

Rules are grouped by severity and category. All rules run on every validation pass; there is no early exit.

---

### 2.1 Points Rules

#### POINTS_EXCEEDED (ERROR)
```
if (roster.totalPoints > roster.pointsLimit) → ERROR
  message: "Army is ${over} pts over the ${limit} pt limit."
  affectedUnit: null (roster-level)
```

---

### 2.2 Unit Count Rules

#### NAMED_CHARACTER_DUPLICATE (ERROR)
```
for each unit in roster where unit.isNamedCharacter:
    if count(unit.unitId in roster) > 1 → ERROR per extra copy
    message: "${unit.name} is a unique named character and may only appear once."
    affectedUnit: the duplicate entry
```

#### UNIT_LIMIT_EXCEEDED (ERROR)
```
for each unit in roster where unit.maxPerRoster > 0:
    if count(unit.unitId in roster) > unit.maxPerRoster → ERROR
    message: "${unit.name} may only appear ${maxPerRoster} time(s) per roster."
```

#### BATTLELINE_MINIMUM (WARNING)
```
// Not a hard 10e rule by default, but warn if army has 0 battleline units
if roster.units.none { it.role == BATTLELINE } → WARNING
  message: "No Battleline units in this army."
```

---

### 2.3 Leader Attachment Rules

#### LEADER_KEYWORD_MISMATCH (ERROR)
```
for each rosterUnit that has an attachedLeaderId:
    leader = getUnit(attachedLeaderId)
    bodyguard = rosterUnit
    requiredKeywords = leader.leaderAbility.attachKeywords
    bodyguardKeywords = bodyguard.keywords + bodyguard.factionKeywords
    if requiredKeywords.any { it !in bodyguardKeywords } → ERROR
    message: "${leader.name} cannot lead ${bodyguard.name}. 
              Required keywords: ${requiredKeywords.joinToString()}."
```

#### LEADER_SELF_ATTACHMENT (ERROR)
```
if leader.rosterUnitId == bodyguard.rosterUnitId → ERROR
  message: "A unit cannot be attached to itself."
```

#### LEADER_ALREADY_ATTACHED_ELSEWHERE (ERROR)
```
// A given leader RosterUnit may only appear once as an attached leader
val allAttachedLeaderIds = roster.units.mapNotNull { it.attachedLeaderRosterUnitId }
if allAttachedLeaderIds.size != allAttachedLeaderIds.toSet().size → ERROR
  for each duplicate:
    message: "${leader.name} is attached to more than one unit."
```

#### BODYGUARD_HAS_MULTIPLE_LEADERS (ERROR)
```
// In 10e, one leader per bodyguard unit (standard rule)
for each rosterUnit:
    if count of leaders attached to this unit > 1 → ERROR
    message: "${unit.name} has more than one leader attached."
```

#### LEADER_WITHOUT_BODYGUARD (WARNING)
```
for each rosterUnit where unit.leaderAbility != null:
    if unit.attachedLeaderRosterUnitId == null 
        AND unit is not itself the target of another unit's attachment → WARNING
    message: "${unit.name} is a Leader but is not attached to a unit."
```

---

### 2.4 Enhancement Rules

#### ENHANCEMENT_INELIGIBLE (ERROR)
```
for each rosterUnit where selectedEnhancementId != null:
    enhancement = getEnhancement(selectedEnhancementId)
    unit = getUnit(rosterUnit.unitId)
    eligibilityKeywords = enhancement.eligibilityKeywords
    if eligibilityKeywords.any { it !in unit.keywords + unit.factionKeywords } → ERROR
    message: "${unit.name} does not meet the requirements for ${enhancement.name}.
              Requires: ${eligibilityKeywords.joinToString()}."
```

#### ENHANCEMENT_DUPLICATE (ERROR)
```
val enhancementIds = roster.units.mapNotNull { it.selectedEnhancementId }
if enhancementIds.size != enhancementIds.toSet().size → ERROR
  for each duplicate:
    message: "${enhancement.name} can only be taken once per army."
```

#### ENHANCEMENT_WRONG_DETACHMENT (ERROR)
```
for each rosterUnit where selectedEnhancementId != null:
    enhancement = getEnhancement(selectedEnhancementId)
    if enhancement.detachmentId != roster.detachmentId → ERROR
    message: "${enhancement.name} belongs to a different detachment."
```

#### ENHANCEMENT_LIMIT (WARNING)
```
// Standard 10e: max 3 enhancements per army
if roster.units.count { it.selectedEnhancementId != null } > 3 → WARNING
  message: "More than 3 enhancements selected. Verify with your opponent."
```

---

### 2.5 Wargear Rules

#### WARGEAR_EXCEEDS_MAX_SELECTIONS (ERROR)
```
for each rosterUnit:
    for each wargearOptionGroup in unit.wargearOptions:
        selectedCount = rosterUnit.selectedWargear
                            .count { it.optionId == group.id }
        if selectedCount > group.constraints.maxSelections → ERROR
        message: "${unit.name}: Too many selections in '${group.description}'. 
                  Max: ${group.constraints.maxSelections}."
```

#### WARGEAR_BELOW_MIN_SELECTIONS (ERROR)
```
        if selectedCount < group.constraints.minSelections → ERROR
        message: "${unit.name}: '${group.description}' requires at least 
                  ${group.constraints.minSelections} selection(s)."
```

#### WARGEAR_PER_MODEL_EXCEEDS_COUNT (ERROR)
```
for wargear options with PER_MODEL scope:
    if selectedCount > rosterUnit.modelCount → ERROR
    message: "${unit.name}: More '${wargear.name}' selected than there are models."
```

#### MUTUALLY_EXCLUSIVE_WARGEAR (ERROR)
```
// Detected via WargearCondition.EXCLUDES entries
for each selected wargear item:
    if any other selected wargear in same unit is in its exclusion list → ERROR
    message: "${wargear1.name} and ${wargear2.name} cannot be taken together."
```

---

### 2.6 Faction / Detachment Rules

#### FACTION_KEYWORD_MISMATCH (ERROR)
```
for each rosterUnit:
    unit = getUnit(rosterUnit.unitId)
    if unit.factionId != roster.factionId 
        AND roster.factionKeyword !in unit.factionKeywords → ERROR
    message: "${unit.name} does not share a faction keyword with this army."
```

#### DETACHMENT_NOT_IN_FACTION (ERROR)
```
detachment = getDetachment(roster.detachmentId)
if detachment.factionId != roster.factionId → ERROR
  message: "Detachment '${detachment.name}' does not belong to the selected faction."
```

---

### 2.7 Model Count Rules

#### MODEL_COUNT_BELOW_MINIMUM (ERROR)
```
for each rosterUnit:
    if rosterUnit.modelCount < unit.minModels → ERROR
    message: "${unit.name} requires a minimum of ${unit.minModels} models."
```

#### MODEL_COUNT_ABOVE_MAXIMUM (ERROR)
```
for each rosterUnit:
    if rosterUnit.modelCount > unit.maxModels → ERROR
    message: "${unit.name} may have a maximum of ${unit.maxModels} models."
```

---

## 3. Validation Execution Order

Rules run in this order to produce the most useful error ordering:

1. Faction/detachment consistency (structural errors first)
2. Model count limits
3. Unit count / named character limits
4. Wargear constraints
5. Leader attachment
6. Enhancement validity
7. Points total

---

## 4. Inline vs Full Validation

**Inline** (in BuildRosterScreen): Each UnitCard shows a red border and a small error count badge when it contributes to any error. Tapping the badge shows a tooltip with the specific issues.

**Full** (ValidationScreen): Complete sorted list of all errors and warnings with navigation links to the affected unit.

**Points bar**: Turns amber at 95–100% of limit, red when over. This is live — not a separate validation pass.

---

## 5. Validation Result Structure

```kotlin
data class ValidationResult(
    val isLegal: Boolean,             // true iff errors list is empty
    val errors: List<ValidationIssue>,
    val warnings: List<ValidationIssue>,
    val pointsUsed: Int,
    val pointsLimit: Int,
    val unitErrorMap: Map<String, List<ValidationIssue>>,  // rosterUnitId → issues
)
```

`unitErrorMap` is pre-built so the UI can look up issues for a specific unit card in O(1).

---

## 6. Testing Strategy

Every validation rule has its own unit test file in `domain/usecase/validation/`. Tests use builders to construct minimal `Roster` objects that isolate exactly one rule.

```kotlin
@Test
fun `named character appearing twice produces error`() {
    val roster = rosterBuilder()
        .withUnit(MARNEUS_CALGAR)
        .withUnit(MARNEUS_CALGAR)   // duplicate
        .build()
    val result = ValidateRosterUseCase()(roster, catalogue)
    assertThat(result.errors).hasSize(1)
    assertThat(result.errors[0].code).isEqualTo(NAMED_CHARACTER_DUPLICATE)
}
```

Target: **100% rule coverage** before MVP release.
