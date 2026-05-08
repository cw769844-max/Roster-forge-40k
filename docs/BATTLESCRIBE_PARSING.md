# Roster Forge 40K — BattleScribe Data Parsing Plan

## 1. Source Files

**Repository**: `https://github.com/BSData/wh40k-10e`

BSData publishes GitHub Releases. Each release contains a ZIP archive with two file types:

| Extension | Full Name | Content |
|---|---|---|
| `.gst` | Game System | Core game rules, shared entries (weapons, keywords, core stratagems) |
| `.cat` | Catalogue | One per faction/supplement; contains units, detachments, faction rules, enhancements |

Both formats are **ZIP-compressed XML**. Unzipped, each is a well-formed XML document conforming to the BattleScribe 2.x schema.

---

## 2. XML Schema Overview

### Root elements

```xml
<!-- Game System -->
<gameSystem id="..." name="Warhammer 40,000" revision="..." battleScribeVersion="...">
  <rules>...</rules>
  <sharedSelectionEntries>...</sharedSelectionEntries>
  <sharedProfiles>...</sharedProfiles>
  <profileTypes>...</profileTypes>
  <categoryEntries>...</categoryEntries>
</gameSystem>

<!-- Catalogue -->
<catalogue id="..." name="Space Marines" revision="..." 
           gameSystemId="..." gameSystemRevision="...">
  <rules>...</rules>
  <selectionEntries>...</selectionEntries>
  <sharedSelectionEntries>...</sharedSelectionEntries>
  <sharedProfiles>...</sharedProfiles>
  <entryLinks>...</entryLinks>
  <catalogueLinks>...</catalogueLinks>
</catalogue>
```

### Key XML element types

| XML Element | Maps To |
|---|---|
| `<selectionEntry type="unit">` | Unit |
| `<selectionEntry type="upgrade">` | Wargear option / model |
| `<selectionEntry type="model">` | Individual model in multi-model unit |
| `<selectionEntryGroup>` | Wargear option group (choice block) |
| `<entryLink>` | Reference to a shared entry (resolve at parse time) |
| `<profile type="Unit">` | Unit stat block |
| `<profile type="Weapon">` | Weapon profile |
| `<profile type="Abilities">` | Ability text |
| `<rule>` | Named rule / ability text |
| `<constraint>` | min/max/exactly limits on selections |
| `<condition>` | Conditional modifier trigger |
| `<modifier>` | Changes to entries based on conditions |
| `<cost type="pts">` | Points cost |
| `<categoryLink>` | Assigns battlefield role (CHARACTER, BATTLELINE, etc.) |
| `<keyword>` | Individual keyword tag |

---

## 3. Parsing Pipeline

### Step 1 — Fetch Latest Release

```
GET https://api.github.com/repos/BSData/wh40k-10e/releases/latest
→ Parse JSON: tag_name, assets[].browser_download_url, assets[].name
→ Find asset where name ends with ".zip"
→ Compare tag_name with installed version in DataStore
→ If newer: proceed to Step 2
```

### Step 2 — Download

```
GET <browser_download_url>  (streamed)
→ Save to app-internal storage as "wh40k-10e-<tag>.zip"
→ Use chunked download with progress callback (shown in Settings UI)
```

### Step 3 — Extract

```kotlin
ZipInputStream(FileInputStream(zipFile)).use { zis ->
    var entry = zis.nextEntry
    while (entry != null) {
        when {
            entry.name.endsWith(".gst") -> parseGameSystem(zis)
            entry.name.endsWith(".cat") -> parseCatalogue(zis)
        }
        entry = zis.nextEntry
    }
}
```

Each file is parsed in its own coroutine (launched with `Dispatchers.IO`).

### Step 4 — XML Parsing (XmlPullParser, SAX-style)

The parser is a streaming state machine. It never builds an in-memory DOM tree of the entire file, which keeps memory use low even for large catalogues.

```kotlin
class BsXmlParser {

    fun parseGameSystem(inputStream: InputStream): ParsedGameSystem {
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")
        return GameSystemParser(parser).parse()
    }

    fun parseCatalogue(inputStream: InputStream): ParsedCatalogue {
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")
        return CatalogueParser(parser).parse()
    }
}
```

#### State machine outline for `<selectionEntry type="unit">`:

```
START_TAG "selectionEntry" [type="unit"]
  → push UnitBuilder onto stack
  
  START_TAG "profiles"
    START_TAG "profile" [type="Unit"]
      → read characteristic tags into UnitStats
    START_TAG "profile" [type="Weapon"]
      → build WeaponProfile
    START_TAG "profile" [type="Abilities"]
      → build Ability
      
  START_TAG "rules"
    START_TAG "rule"
      → build Ability(type=UNIT)
      
  START_TAG "selectionEntries"       ← nested models
    START_TAG "selectionEntry" [type="model"]
      → read model constraints for ModelGroup
      
  START_TAG "selectionEntryGroups"   ← wargear option blocks
    START_TAG "selectionEntryGroup"
      → build WargearOption with constraints
      
  START_TAG "entryLinks"             ← references to shared entries
    START_TAG "entryLink"
      → record link (targetId, type) for post-pass resolution
      
  START_TAG "costs"
    START_TAG "cost" [name="pts"]
      → record PointsCost(modelCount, points)
      
  START_TAG "categoryLinks"
    START_TAG "categoryLink"
      → record BattlefieldRole from categoryEntry name
      
  START_TAG "keywords"
    START_TAG "keyword"
      → append to keyword list
      
END_TAG "selectionEntry"
  → pop UnitBuilder, add to result list
```

### Step 5 — Link Resolution

After all files are parsed, a post-pass resolves `<entryLink>` references:

```kotlin
fun resolveLinks(catalogues: List<ParsedCatalogue>, gameSystem: ParsedGameSystem) {
    val sharedEntries: Map<String, SharedEntry> = buildSharedEntryMap(catalogues, gameSystem)
    catalogues.forEach { catalogue ->
        catalogue.units.forEach { unit ->
            unit.unresolvedLinks.forEach { link ->
                val shared = sharedEntries[link.targetId]
                if (shared != null) {
                    unit.mergeSharedEntry(shared, link)
                }
            }
        }
    }
}
```

### Step 6 — Domain Mapping

```kotlin
fun ParsedUnit.toDomain(factionId: String): Unit = Unit(
    id = this.id,
    factionId = factionId,
    name = this.name,
    role = this.categoryLinks.toBattlefieldRole(),
    keywords = this.keywords,
    factionKeywords = this.factionKeywords,
    stats = this.unitProfile.toUnitStats(),
    modelGroups = this.models.toModelGroups(),
    weapons = this.weaponProfiles.map { it.toWeaponProfile() },
    abilities = this.abilities.map { it.toAbility() },
    wargearOptions = this.wargearGroups.map { it.toWargearOption() },
    pointsCosts = this.costs.toPointsCosts(),
    leaderAbility = this.leaderRule?.toLeaderAbility(),
    attachmentTargets = this.attachKeywords,
    isNamedCharacter = NAMED_CHARACTER_CATEGORY_ID in this.categoryLinkIds,
    maxPerRoster = this.constraints.maxPerRoster(),
    minModels = this.minModelConstraint(),
    maxModels = this.maxModelConstraint(),
)
```

### Step 7 — Bulk Insert into Room

```kotlin
@Transaction
suspend fun replaceAllCatalogueData(
    factions: List<FactionEntity>,
    detachments: List<DetachmentEntity>,
    units: List<UnitEntity>,
    enhancements: List<EnhancementEntity>,
    stratagems: List<StratagemEntity>,
) {
    // Delete all existing catalogue data (cascade deletes children)
    factionDao.deleteAll()
    // Re-insert everything
    factionDao.insertAll(factions)
    detachmentDao.insertAll(detachments)
    unitDao.insertAll(units)
    enhancementDao.insertAll(enhancements)
    stratagemDao.insertAll(stratagems)
}
```

This runs in a single transaction so the app is never in a partial-data state.

---

## 4. Detachment and Enhancement Parsing

Detachments in 10e are typically represented as a top-level `<selectionEntry type="upgrade">` with a specific category (`DETACHMENT`) or as a named entry in the catalogue root.

The parser identifies detachments by:
1. Looking for entries with a `<categoryLink>` pointing to a category named "Detachment" or "Detachments".
2. Alternatively, by convention in BSData 10e, detachment entries have a recognisable naming pattern.

Enhancements are nested inside the detachment entry as child `<selectionEntry type="upgrade">` elements under a group named "Enhancements" or similar.

Stratagems are stored as `<rule>` or `<selectionEntry>` entries with specific profile type `"Stratagem"` containing characteristics: CP, Type, Timing, Target, Effect, Restrictions.

---

## 5. Catalogue Identification

The catalogue file name encodes the faction. Mapping example:

| Filename | Faction |
|---|---|
| `Space Marines.cat` | Space Marines (`SM`) |
| `Chaos Space Marines.cat` | Chaos Space Marines (`CSM`) |
| `Aeldari.cat` | Aeldari |
| `Tyranids.cat` | Tyranids |

The catalogue's `name` attribute is used as the display name. The `id` attribute (a UUID-like string) is used as the stable internal `factionId`.

---

## 6. Error Handling During Parse

| Failure | Behaviour |
|---|---|
| Corrupt ZIP | Abort entire sync; report error to user; keep existing data |
| Malformed XML in one `.cat` | Skip that catalogue; log warning; continue with others |
| Missing required attribute | Use default value where safe; log warning |
| Unknown element | Skip (forward-compatible) |
| Schema version mismatch | Warn user; attempt parse anyway |

The parser never crashes the app. All exceptions are caught per-file and collected into a `ParseResult.warnings` list that is shown on the Settings screen after sync.

---

## 7. Version Tracking

After a successful sync:

```kotlin
appSettingsRepository.setInstalledVersion(releaseTag)        // e.g. "v2.1.4"
appSettingsRepository.setInstalledDate(System.currentTimeMillis())
```

The Settings screen displays both values. A badge on the Home screen TopAppBar appears when the installed version is older than the latest available release.

---

## 8. Incremental Updates (Post-MVP)

The BSData GitHub API supports querying releases since a given date. A future optimisation will:

1. Fetch the list of changed files between installed tag and latest tag using `GET /repos/BSData/wh40k-10e/compare/{old}...{new}`.
2. Download only the changed `.cat` files.
3. Parse and re-insert only those catalogues.

This reduces data usage for minor patch updates.

---

## 9. Offline Data Bundle (First Launch)

To avoid requiring network access on first launch, a snapshot of the BSData files at release time will be bundled in `assets/catalogue_snapshot/` inside the APK. The app will:

1. Detect that no data is installed.
2. Copy and parse the bundled snapshot immediately (no network).
3. Show a "Check for updates" prompt in Settings, non-blocking.

The bundled snapshot will be updated with each app release.
