# Roster Forge 40K — Screen-by-Screen UI Design

## Design Language

- **Theme**: Material 3, with a custom dark palette using deep crimson (`#8B0000`), charcoal (`#1C1C1E`), and gold accent (`#C9A84C`).
- **Light theme**: Off-white background, dark crimson primary, warm grey surfaces.
- **Typography**: `Rajdhani` (headings — gives a gothic sci-fi feel without being unreadable), `Inter` (body).
- **Layout**: Single-column cards on phone; adaptive two-column on tablet (future).
- **Corner radius**: 8dp for cards, 16dp for bottom sheets, 24dp for dialogs.
- **Spacing unit**: 8dp base grid.

---

## Screen 1 — Home / Roster List

**Route**: `home`

### Purpose
Entry point of the app. Shows all saved rosters and entry points to create a new one.

### Layout
```
┌───────────────────────────────┐
│ ≡  Roster Forge 40K     ⚙ 🔔 │  ← TopAppBar
│                               │
│  YOUR ROSTERS                 │  ← section header
│                               │
│ ┌───────────────────────────┐ │
│ │ Ultramarines — 2000 pts   │ │  ← RosterCard
│ │ Gladius Task Force        │ │
│ │ 1985 / 2000 ●●●●○  ⋮     │ │  ← points pip bar + overflow menu
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ Death Guard — 1000 pts    │ │
│ │ Plague Company            │ │
│ │  960 / 1000 ●●●●●  ⋮     │ │
│ └───────────────────────────┘ │
│                               │
│              [+ New Roster]   │  ← FAB (bottom-right)
└───────────────────────────────┘
```

### Components
- **TopAppBar**: App name, settings icon, update notification bell (badge if update available).
- **RosterCard**: Faction name, points limit, detachment name, points pip bar (5 pips = 5 × 400 pts for 2000 limit), overflow menu (Edit / Duplicate / Rename / Delete).
- **FAB**: Opens Create Roster flow.
- **Empty state**: Illustration of a Space Marine helmet + "No rosters yet. Build your first army."

### Interactions
- Tap RosterCard → Build Mode (BuildRosterScreen)
- Long-press RosterCard → select mode (multi-select for bulk delete)
- Overflow → Duplicate | Rename (inline text field) | Delete (confirm dialog)
- Bell icon → Settings > Data Update screen if update available

---

## Screen 2 — Create Roster

**Route**: `create_roster`

### Purpose
Gather the three required parameters before opening the build screen: name, points limit, faction.

### Layout
```
┌───────────────────────────────┐
│ ←  New Roster                 │
│                               │
│  Roster Name                  │
│ ┌───────────────────────────┐ │
│ │ My Ultramarines List    × │ │
│ └───────────────────────────┘ │
│                               │
│  Points Limit                 │
│ [500] [1000] [1500] [2000]    │  ← segmented button row
│  Custom: [____]               │
│                               │
│  Faction                      │
│ ┌───────────────────────────┐ │
│ │ 🔍 Search factions...     │ │
│ └───────────────────────────┘ │
│ ┌──────────┐ ┌──────────────┐ │
│ │ Space    │ │ Chaos Space  │ │  ← faction chips
│ │ Marines  │ │ Marines      │ │
│ └──────────┘ └──────────────┘ │
│                               │
│         [Continue →]          │
└───────────────────────────────┘
```

### Components
- **OutlinedTextField** for roster name.
- **SegmentedButtonRow** for preset point limits (Material 3).
- **Faction picker**: Search bar + scrollable chip grid grouped by allegiance (IMPERIUM / CHAOS / XENOS).

### Interactions
- Continue → DetachmentSelectScreen (name + points + faction carried as nav args).
- Validation: name required, faction required; continue button disabled until both set.

---

## Screen 3 — Faction Selection

**Route**: `faction_select`

(Used both from Create Roster flow and when changing faction on existing roster.)

### Layout
```
┌───────────────────────────────┐
│ ←  Choose Faction             │
│ 🔍  [Search factions...]      │
│                               │
│  IMPERIUM              ───    │  ← collapsible allegiance group
│                               │
│ ┌──────┐ ┌──────┐ ┌───────┐  │
│ │ SM   │ │ BA   │ │ DA    │  │  ← faction icon cards
│ │Space │ │Blood │ │Dark   │  │
│ │Marns │ │Angls │ │Angels │  │
│ └──────┘ └──────┘ └───────┘  │
│                               │
│  CHAOS                 ───    │
│ ┌──────┐ ┌──────┐            │
│ │ CSM  │ │ DG   │            │
│ │Chaos │ │Death │            │
│ │SpMrn │ │Guard │            │
│ └──────┘ └──────┘            │
└───────────────────────────────┘
```

### Notes
- Faction icons sourced from BSData / local drawable resources (placeholder silhouettes for MVP).
- Tapping a faction card shows a brief tooltip (faction keyword, allegiance) and selects it.

---

## Screen 4 — Detachment Selection

**Route**: `detachment_select/{rosterId}`

### Layout
```
┌───────────────────────────────┐
│ ←  Choose Detachment          │
│     Space Marines             │  ← faction name subheader
│                               │
│ ┌───────────────────────────┐ │
│ │ Gladius Task Force        │ │  ← DetachmentCard
│ │ ────────────────────────  │ │
│ │ DETACHMENT RULE           │ │
│ │ Oath of Moment: At the    │ │
│ │ start of your Command     │ │
│ │ phase, select one enemy   │ │
│ │ unit...           [more]  │ │
│ │                           │ │
│ │ ENHANCEMENTS (4)  ▼       │ │  ← collapsible
│ │ STRATAGEMS (6)    ▼       │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ Vanguard Spearhead        │ │
│ │ ...                       │ │
│ └───────────────────────────┘ │
└───────────────────────────────┘
```

### Components
- **DetachmentCard**: Name, detachment rule summary (truncated with expand), collapsed previews of enhancements and stratagems.
- Tapping card → selects detachment and navigates to BuildRosterScreen.
- Long-press → bottom sheet showing full detachment rules, all stratagems and enhancements.

---

## Screen 5 — Build Roster (Main Hub)

**Route**: `build/{rosterId}`

### Purpose
The central editing screen. Shows current roster state, points bar, and quick actions.

### Layout
```
┌───────────────────────────────┐
│ ←  My Ultramarines     ✓ ⋮   │  ← back, validate button, overflow
│                               │
│ ██████████████░░░  1760/2000  │  ← animated points bar
│ Gladius Task Force            │
│                               │
│ ▼ CHARACTERS (2)              │  ← collapsible role section
│                               │
│ ┌───────────────────────────┐ │
│ │ Captain in Phobos Armour  │ │  ← UnitCard (collapsed)
│ │ Warlord · Enhancement ★   │ │
│ │                   130 pts │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ Librarian                 │ │
│ │                   110 pts │ │
│ └───────────────────────────┘ │
│                               │
│ ▼ BATTLELINE (3)              │
│ ┌───────────────────────────┐ │
│ │ Intercessor Squad (10)    │ │
│ │ Bolt rifles               │ │
│ │                   210 pts │ │
│ └───────────────────────────┘ │
│                               │
│                        [+ ▼]  │  ← SpeedDial FAB
└───────────────────────────────┘
```

### SpeedDial FAB Options
```
         [Add Unit]
         [View Roster]
         [Stratagems]
[+]
```

### UnitCard (Expanded)
```
┌───────────────────────────────┐
│ Intercessor Squad (10)  ↑ ⋮   │  ← collapse, overflow (Edit/Duplicate/Delete)
│ ─────────────────────────── │
│ Wargear: Bolt rifles (10)     │
│          Power fist (Sgt)     │
│                               │
│ Leader: Lt. in Phobos [×]     │  ← attached leader chip with detach button
│                               │
│                       210 pts │
└───────────────────────────────┘
```

### Interactions
- Tap UnitCard → UnitCustomizeScreen
- Swipe left on UnitCard → delete (undo snackbar)
- ✓ button → ValidationScreen
- ⋮ overflow → Rename Roster / Change Detachment / Change Faction (with confirmation)
- Points bar turns red when over limit

---

## Screen 6 — Unit Browser (Add Unit)

**Route**: `unit_browser/{rosterId}`

### Layout
```
┌───────────────────────────────┐
│ ←  Add Unit                   │
│ 🔍 [Search units...]          │
│                               │
│ [All] [Characters] [Infantry] │  ← role filter chips
│ [Vehicles] [Monsters]         │
│                               │
│ ▼ CHARACTERS                  │
│                               │
│ ┌───────────────────────────┐ │
│ │ Captain              95pts│ │  ← UnitListItem
│ │ INFANTRY · CHARACTER      │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ Librarian           110pts│ │
│ │ INFANTRY · PSYKER · CHAR  │ │
│ └───────────────────────────┘ │
│                               │
│ ▼ BATTLELINE                  │
│ ┌───────────────────────────┐ │
│ │ Intercessor Squad   100pts│ │
│ │ 5 models              ─── │ │
│ │                           │ │
│ │ MAX: 10   COST/5: 100pts  │ │
│ └───────────────────────────┘ │
└───────────────────────────────┘
```

### Interactions
- Tap UnitListItem → adds unit to roster immediately (at minimum model count) and navigates to UnitCustomizeScreen.
- Already-at-limit units shown greyed out with "LIMIT REACHED" badge.
- Named characters already in roster shown with "UNIQUE" badge and disabled.
- Points in item header reflect minimum cost.

---

## Screen 7 — Unit Customization

**Route**: `unit_customize/{rosterUnitId}`

### Layout
```
┌───────────────────────────────┐
│ ←  Intercessor Squad          │
│                               │
│  MODEL COUNT                  │
│ [−] ─────────── 10 ──────────[+] │  ← slider with bounds
│  MIN: 5    MAX: 10            │
│                               │
│  WARGEAR OPTIONS              │
│                               │
│ ┌───────────────────────────┐ │
│ │ Sergeant equipment        │ │
│ │  ● Bolt pistol + chainsword│ │  ← selected
│ │  ○ Bolt pistol + power fist│ │
│ │  ○ Hand flamer + chainsword│ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ Special weapons (1 per 5) │ │
│ │  □ Astartes grenade lnchr │ │
│ │  □ Auto bolt rifle        │ │
│ │  ■ Stalker bolt rifle     │ │  ← selected (■)
│ └───────────────────────────┘ │
│                               │
│  WEAPONS SUMMARY              │  ← collapsible
│                               │
│  ABILITIES                    │  ← collapsible
│                               │
│         ─── 210 pts ───       │  ← sticky bottom bar
└───────────────────────────────┘
```

### Components
- **Slider + counter** for model count (respects min/max).
- **WargearOptionGroup**: Radio group (single select) or checkbox group (multi-select) per wargear block.
- **WeaponSummaryPanel** (collapsible): table of all weapons with stats.
- **AbilitiesPanel** (collapsible): list of ability cards.
- **Sticky bottom bar**: current computed points and "Done" button.

---

## Screen 8 — Leader Attachment

**Route**: `leader_attach/{rosterUnitId}`

Used both from a leader's unit card ("Attach to unit") and from a bodyguard unit's card ("Attach a leader").

### Layout
```
┌───────────────────────────────┐
│ ←  Attach Leader              │
│                               │
│  Attaching: Librarian         │
│  Requires: ADEPTUS ASTARTES   │
│            INFANTRY           │
│                               │
│  ELIGIBLE UNITS IN ROSTER     │
│                               │
│ ┌───────────────────────────┐ │
│ │ ✓ Intercessor Squad (10)  │ │  ← eligible, currently unattached
│ │   BATTLELINE · INFANTRY   │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ ✗ Assault Intercessors    │ │  ← already has a leader
│ │   (Leader: Lt. Tacitus)   │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ – Outriders               │ │  ← ineligible (keyword mismatch)
│ │   CAVALRY · not INFANTRY  │ │
│ └───────────────────────────┘ │
│                               │
│         [Detach Current]      │  ← shown if already attached
└───────────────────────────────┘
```

---

## Screen 9 — Enhancement Selection

**Route**: `enhancement/{rosterId}/{rosterUnitId}`

### Layout
```
┌───────────────────────────────┐
│ ←  Choose Enhancement         │
│     For: Captain              │
│                               │
│ ┌───────────────────────────┐ │
│ │ ● Adept of the Hood   5pts│ │  ← selected
│ │  While this model leads    │ │
│ │  a unit, ranged attacks    │ │
│ │  targeting that unit...    │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ ○ Relic of the Chapter 0pt│ │
│ │  This model's bolt weapon  │ │
│ │  has the [DEVASTATING WOUNDS]│ │
│ │  keyword...               │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ – Shroud of Heroes    5pts│ │  ← greyed = already taken
│ │  Taken by: Librarian      │ │
│ └───────────────────────────┘ │
│                               │
│  [No Enhancement]             │  ← clear selection option
└───────────────────────────────┘
```

### Rules Enforced
- Only one enhancement per character.
- Each enhancement unique per roster.
- Character must have eligible keywords.

---

## Screen 10 — Roster Validation

**Route**: `validation/{rosterId}`

### Layout
```
┌───────────────────────────────┐
│ ←  Roster Validation          │
│                               │
│ ╔═══════════════════════════╗ │
│ ║  ✓  ROSTER IS LEGAL       ║ │  ← green pass state
│ ╚═══════════════════════════╝ │
│                               │
│  1985 / 2000 pts  (15 free)   │
│                               │
│  ─── WARNINGS (1) ────────    │
│                               │
│ ┌───────────────────────────┐ │
│ │ ⚠  No Warlord designated  │ │
│ │    Consider assigning an  │ │
│ │    enhancement to a unit. │ │
│ └───────────────────────────┘ │
│                               │
│  ─── ARMY SUMMARY ────────    │
│  Faction: Space Marines       │
│  Detachment: Gladius Task Force│
│  Units: 12                    │
│  Enhancements: 2              │
│  Leaders: 2 attached          │
│                               │
│         [View Roster]         │
│       [Continue Editing]      │
└───────────────────────────────┘
```

### Error State Example
```
┌───────────────────────────────┐
│ ←  Roster Validation          │
│                               │
│ ╔═══════════════════════════╗ │
│ ║  ✕  ROSTER HAS ERRORS (2) ║ │  ← red fail state
│ ╚═══════════════════════════╝ │
│                               │
│  ─── ERRORS ───────────────   │
│                               │
│ ┌───────────────────────────┐ │
│ │ ✕  2050 / 2000 pts        │ │
│ │    Over points limit by   │ │
│ │    50 pts                 │ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ ✕  Marneus Calgar appears │ │
│ │    twice. Named characters│ │
│ │    must be unique.        │ │
│ │    [Go to unit →]         │ │
│ └───────────────────────────┘ │
└───────────────────────────────┘
```

---

## Screen 11 — View Roster

**Route**: `view/{rosterId}`

### Purpose
Game-table companion. Clean, readable, minimal chrome.

### Layout
```
┌───────────────────────────────┐
│ ←  My Ultramarines      [STR] │  ← back, stratagems tab shortcut
│     Gladius Task Force        │
│     1985 / 2000 pts           │
│                               │
│ ─── ARMY SPECIAL RULE ──────  │
│ ┌───────────────────────────┐ │
│ │ And They Shall Know No    │ │  ← faction rule card
│ │ Fear: ...                 │ │
│ └───────────────────────────┘ │
│                               │
│ ─── DETACHMENT RULE ────────  │
│ ┌───────────────────────────┐ │
│ │ Oath of Moment: ...       │ │
│ └───────────────────────────┘ │
│                               │
│ ─── CHARACTERS (2) ─────────  │
│                               │
│ ┌───────────────────────────┐ │
│ │ ▶ Captain                 │ │  ← collapsed
│ │   Enhancement: Hood   130p│ │
│ └───────────────────────────┘ │
│                               │
│ ┌───────────────────────────┐ │
│ │ ▼ Librarian           110p│ │  ← expanded
│ │ ─────────────────────── │ │
│ │ WEAPONS                   │ │
│ │  Force occulum  24" A3 4+ │ │
│ │                  S6 AP-1 D2│ │
│ │  Smite (psychic)          │ │
│ │                           │ │
│ │ ABILITIES                 │ │
│ │  Leader, Psychic          │ │
│ │  Attached to: Tacticals   │ │
│ └───────────────────────────┘ │
│                               │
│     ─── [Share / Export] ───  │
└───────────────────────────────┘
```

---

## Screen 12 — Stratagem Reference

**Route**: `stratagems/{rosterId}` (army-scoped) or `stratagems` (standalone)

### Layout
```
┌───────────────────────────────┐
│ ←  Stratagems           🔍 ⊞ │  ← search, filter
│     Gladius Task Force        │
│                               │
│ [All] [Command] [Movement]    │  ← phase filter chips
│ [Shooting] [Fight]            │
│                               │
│ [All] [Battle Tactic] [Ploy]  │  ← type filter chips
│ [Epic Deed] [Wargear]         │
│                               │
│ ─── DETACHMENT (6) ─────────  │
│                               │
│ ┌───────────────────────────┐ │
│ │ Rapid Assault    1CP  [📌]│ │  ← pin button
│ │ BATTLE TACTIC · SHOOTING  │ │
│ │ ─────────────────────── │ │
│ │ Target: ADEPTUS ASTARTES  │ │
│ │ unit from your army       │ │
│ │                           │ │
│ │ Effect: Until end of phase│ │
│ │ each time a model in this │ │
│ │ unit makes a ranged attack│ │
│ │ it can target a unit it   │ │
│ │ Advanced towards...       │ │
│ └───────────────────────────┘ │
│                               │
│ ─── CORE (8) ───────────────  │
│                               │
│ ┌───────────────────────────┐ │
│ │ Command Re-roll   1CP [📌]│ │
│ │ STRATEGIC PLOY · ANY      │ │
│ └───────────────────────────┘ │
└───────────────────────────────┘
```

### Pinned Stratagems
Pinned stratagems appear in a horizontal scrollable chip row at the top of the screen, above the filters, for instant game-table access.

---

## Screen 13 — Settings / Data Update

**Route**: `settings`

### Layout
```
┌───────────────────────────────┐
│ ←  Settings                   │
│                               │
│  DATA                         │
│ ┌───────────────────────────┐ │
│ │ Installed Version         │ │
│ │ wh40k-10e v2.1.4          │ │
│ │ Updated: 3 days ago       │ │
│ └───────────────────────────┘ │
│ ┌───────────────────────────┐ │
│ │ [Check for Updates]       │ │
│ │ Latest: v2.1.4 ✓ Up to   │ │
│ │ date                      │ │
│ └───────────────────────────┘ │
│                               │
│  APPEARANCE                   │
│ ┌───────────────────────────┐ │
│ │ Theme   [System] [Light]  │ │
│ │         [Dark]            │ │
│ └───────────────────────────┘ │
│                               │
│  ABOUT                        │
│ ┌───────────────────────────┐ │
│ │ App Version: 1.0.0        │ │
│ │ Data Source: BSData/wh40k │ │
│ │ -10e (open data, CC BY)   │ │
│ └───────────────────────────┘ │
│ ┌───────────────────────────┐ │
│ │ Open Source Licences      │ │
│ │ Privacy Policy            │ │
│ └───────────────────────────┘ │
└───────────────────────────────┘
```

---

## Navigation Flow Diagram

```
Home
 ├── [+ New Roster] ──→ CreateRoster ──→ FactionSelect ──→ DetachmentSelect
 │                                                               └──→ BuildRoster ◄─────────────────┐
 │                                                                      ├── UnitBrowser ─── [tap] ──┤
 │                                                                      ├── UnitCustomize            │
 │                                                                      ├── LeaderAttach             │
 │                                                                      ├── Enhancement              │
 │                                                                      └── Validation               │
 │                                                                             └── ViewRoster         │
 └── [tap roster] ────────────────────────────────────────────────────→ BuildRoster ◄───────────────┘
                                                                               └── ViewRoster
                                                                                      └── Stratagems
Settings (accessible from Home TopAppBar)
```

---

## Component Library (Shared)

| Component | Description |
|---|---|
| `PointsBar` | Animated fill bar with numeric label; turns red at >100% |
| `UnitCard` | Collapsible card with name, pts, wargear, leader badge, error indicator |
| `WargearOptionGroup` | Radio/checkbox group for a single wargear block |
| `WeaponTable` | Compact table row per weapon profile |
| `AbilityCard` | Name + effect text, collapsible |
| `StratagemCard` | CP chip, phase chip, full effect text, pin button |
| `ValidationBanner` | Red/yellow/green top banner in BuildRoster |
| `FactionChip` | Rounded chip with faction icon |
| `EnhancementCard` | Name, pts, effect, eligibility indicator |
| `BottomSheetRulesPanel` | Full-screen-height sheet for detachment/faction rules |
