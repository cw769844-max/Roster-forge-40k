# Roster Forge 40K — Product Specification

## 1. Overview

**Roster Forge 40K** is a free, mobile-first Warhammer 40,000 10th Edition army list builder for Android. It is designed around three core principles:

1. **Speed** — players can build or modify a list in minutes, not hours.
2. **Clarity** — the UI prioritises legibility during an actual game, not just during list building.
3. **Free** — all gameplay-critical features (stratagems, abilities, rules) are free. No paywalls on core functionality.

It is inspired by BattleScribe and New Recruit but is an independent, original implementation with a modern Jetpack Compose interface and a clean, normalised internal data model.

---

## 2. Goals and Non-Goals

### Goals
- Build, validate, view, and manage 10th Edition rosters on a phone.
- Parse and stay current with BattleScribe wh40k-10e data files.
- Surface stratagems, abilities, detachment rules, and faction rules for free.
- Work fully offline after initial data download.
- Support all official factions and detachments in the BSData 10e dataset.

### Non-Goals (MVP)
- Web or iOS versions (deferred to post-MVP).
- Painting trackers, game result logging, or community features.
- Custom data entry / homebrew rules.
- Multiplayer or real-time sync.

---

## 3. App Modes

### 3.1 Build Mode
The primary editing environment for constructing and modifying rosters.

**Entry points:**
- New roster (from Home screen)
- Edit existing roster (from Home screen)

**Capabilities:**
| Feature | Details |
|---|---|
| Points limit | Choose from presets (500 / 1000 / 1500 / 2000 pts) or enter custom |
| Faction selection | Browse all factions with icons and keyword filters |
| Detachment selection | Choose from detachments legal for the selected faction |
| Add units | Search/browse units available in the chosen faction |
| Unit wargear | Select from legal wargear options per unit profile |
| Enhancements | Assign one enhancement from the detachment list to eligible characters |
| Leader attachment | Attach eligible leaders to compatible bodyguard units |
| Points tracking | Live points bar: used / limit, with colour coding |
| Duplicate units | Add multiple copies where rules allow |
| Named characters | Enforced uniqueness and faction restrictions |
| Validation | Inline warnings and errors; full validation panel |
| Roster management | Save, duplicate, rename, delete rosters |

### 3.2 View Mode
A read-only, game-ready layout of a completed roster.

**Capabilities:**
| Feature | Details |
|---|---|
| Full roster overview | Scrollable summary of all units |
| Grouped units | By battlefield role (Leader, Battleline, etc.) |
| Unit cards | Collapsed (name + pts) / expanded (wargear, abilities, keywords) |
| Leaders | Shown nested inside their attached unit's card |
| Detachment rule | Pinned at top of view |
| Faction rule | Shown in army details |
| Enhancements | Listed on the enhanced character's card |
| Stratagems | Tab showing detachment + core stratagems for this army |
| Export / share | Plain-text export, PDF share intent |

### 3.3 Stratagem Reference
A filterable, searchable reference for all stratagems relevant to the loaded army.

**Capabilities:**
| Feature | Details |
|---|---|
| Scope | Core stratagems + faction stratagems + detachment stratagems |
| Stratagem card | Name, CP cost, type, phase, target, effect, restrictions, source |
| Filters | Phase, type (offensive/defensive/battle tactic/epic deed/strategic), CP cost |
| Search | Full-text search on name and effect text |
| In-game pinning | Pin frequently-used stratagems to the top |

### 3.4 Data Management
Background capability for keeping the app's rules data current.

**Capabilities:**
| Feature | Details |
|---|---|
| Initial download | Fetch `.cat` / `.gst` files from BSData GitHub on first launch |
| Manual update | Check for updates and download new releases |
| Version display | Show which BSData release tag is installed |
| Offline operation | Full functionality after initial download |
| Delta updates | Download only changed catalogues |

---

## 4. Feature List

### Roster Features
- [ ] Create roster with name, points limit, faction, detachment
- [ ] Add / remove units
- [ ] Add multiple copies of a unit (where allowed)
- [ ] Set wargear selections per unit
- [ ] Assign detachment enhancements to eligible characters
- [ ] Attach leaders to bodyguard units (validate keyword match)
- [ ] Rename roster
- [ ] Duplicate roster (fork an existing list)
- [ ] Delete roster
- [ ] Roster list sorted by most-recently modified

### Unit Features
- [ ] Per-unit points display
- [ ] Wargear options with legal restrictions enforced
- [ ] Ability text accessible in-line
- [ ] Weapon profiles accessible in-line
- [ ] Keyword list per unit

### Validation Features
- [ ] Points over limit warning
- [ ] Duplicate named character error
- [ ] Exceeding per-roster unit limits
- [ ] Leader keyword mismatch error
- [ ] Enhancement on ineligible unit error
- [ ] Wargear constraint violations
- [ ] Faction / detachment mismatch
- [ ] Missing mandatory selections

### Data Features
- [ ] Parse BSData .gst and .cat XML files
- [ ] Normalise into local Room database
- [ ] Version-aware update mechanism
- [ ] Data version display in Settings

### Reference Features
- [ ] Free stratagem access (all tiers)
- [ ] Stratagem filter by phase / type / CP
- [ ] Unit ability quick-reference
- [ ] Faction rule and detachment rule display

### UX Features
- [ ] Dark and light theme
- [ ] Persistent roster state (survives app kill)
- [ ] Quick-access floating action button in Build Mode
- [ ] Swipe-to-delete for units in roster
- [ ] Undo last deletion (snackbar)
- [ ] Search in unit browser
- [ ] Sort units by role / points / name

---

## 5. Constraints and Assumptions

- Data is sourced exclusively from the BSData `wh40k-10e` repository. No scraping of Games Workshop or other sites.
- Points and rules data may change between BSData releases; the app must handle schema evolution.
- The app does not reproduce copyrighted GW text verbatim beyond what BSData already publishes openly.
- Initial target: Android 8.0 (API 26) and above.
- No user accounts required for MVP.
- No cloud sync in MVP.
