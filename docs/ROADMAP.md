# Roster Forge 40K — Roadmap

## MVP (v1.0)

### Goal
A fully functional offline army builder for Warhammer 40K 10th Edition with free stratagem access and clean mobile UI.

### Timeline: ~12 weeks

---

### Sprint 1 (Weeks 1–2): Foundation
**Theme: Data pipeline working end-to-end**

- [ ] Android project scaffolding (Kotlin, Compose, Hilt, Room)
- [ ] Gradle version catalogue (`libs.versions.toml`)
- [ ] GitHub Actions CI (build + lint)
- [ ] BattleScribe XML parser (`.gst` + `.cat`)
- [ ] Room schema: factions, units, detachments, enhancements, stratagems
- [ ] Seed database from bundled BSData snapshot
- [ ] CatalogueRepository with basic DAOs
- [ ] Unit test suite for parser edge cases

**Milestone**: Parser reads all factions and units from bundled data with no crashes.

---

### Sprint 2 (Weeks 3–4): Roster Core
**Theme: Create, save, and load a roster**

- [ ] Roster Room schema (rosters, roster_units)
- [ ] RosterRepository (CRUD)
- [ ] CreateRosterUseCase, AddUnitToRosterUseCase, RemoveUnitFromRosterUseCase
- [ ] DataStore for app settings
- [ ] HomeScreen (roster list, empty state)
- [ ] CreateRosterScreen (name, points, faction picker)
- [ ] DetachmentSelectScreen
- [ ] BuildRosterScreen (unit list, points bar)
- [ ] Navigation graph wired up

**Milestone**: User can create a roster, pick faction and detachment, add units, and return to home to see it saved.

---

### Sprint 3 (Weeks 5–6): Unit Customization
**Theme: Wargear, model counts, points**

- [ ] UnitBrowserScreen (search, role filter)
- [ ] UnitCustomizeScreen (model count slider, wargear option groups)
- [ ] WargearConstraints enforcement in UI
- [ ] UpdateWargearSelectionUseCase
- [ ] Live points recalculation
- [ ] PointsCost lookup (per model count tier)
- [ ] Unit ability and weapon profile display (collapsible panels)

**Milestone**: User can configure a unit's wargear and see accurate points reflected in real time.

---

### Sprint 4 (Weeks 7–8): Leaders, Enhancements, Validation
**Theme: Advanced roster rules**

- [ ] LeaderAttachScreen (eligible units, keyword display)
- [ ] AttachLeaderUseCase + keyword matching logic
- [ ] EnhancementScreen (eligible enhancements for selected unit)
- [ ] AssignEnhancementUseCase
- [ ] ValidateRosterUseCase (all rules from VALIDATION_LOGIC.md)
- [ ] ValidationScreen (errors + warnings display)
- [ ] Inline unit card error badges
- [ ] Points bar amber/red states

**Milestone**: Validation catches all error types from spec; users see clear, actionable error messages.

---

### Sprint 5 (Weeks 9–10): View Mode + Stratagems
**Theme: Game-table readiness**

- [ ] ViewRosterScreen (grouped units, collapsible cards)
- [ ] Faction rule + detachment rule panels
- [ ] Leader nested inside bodyguard card
- [ ] Enhancement display on character card
- [ ] StratagemReferenceScreen (army-scoped)
- [ ] Phase and type filters
- [ ] Full-text search
- [ ] Stratagem pinning
- [ ] Plain-text roster export (share intent)

**Milestone**: User can take their phone to a game table and reference their entire army — units, rules, and stratagems — without any editing UI getting in the way.

---

### Sprint 6 (Weeks 11–12): Data Updates, Polish, Release
**Theme: Production quality**

- [ ] SettingsScreen (version display, update check button)
- [ ] GitHub Releases API integration
- [ ] Download + re-parse on update
- [ ] Delta update indicator (badge on Home bell icon)
- [ ] WorkManager periodic check (weekly, unmetered network)
- [ ] Roster duplicate and rename
- [ ] Swipe-to-delete with undo
- [ ] Dark / light / system theme support
- [ ] Accessibility audit (content descriptions, minimum touch target size)
- [ ] R8 shrinking configured
- [ ] Crash-free rate target: >99.5% over 100 sessions of internal testing
- [ ] Google Play internal test track release

**Milestone**: App published to Google Play internal track; passes all automated tests; data update flow works end-to-end.

---

## MVP Feature Summary

| Feature | Included in MVP |
|---|---|
| All official factions | ✓ |
| All detachments | ✓ |
| Unit wargear options | ✓ |
| Leader attachment | ✓ |
| Enhancements | ✓ |
| Full validation | ✓ |
| View Mode | ✓ |
| All stratagems (free) | ✓ |
| Stratagem filter / search | ✓ |
| Plain-text export | ✓ |
| Data updates from BSData | ✓ |
| Offline operation | ✓ |
| Dark theme | ✓ |
| PDF export | ✗ (v1.1) |
| Tablet layout | ✗ (v1.1) |
| Cloud sync | ✗ (future) |
| iOS | ✗ (future) |

---

## Post-MVP Releases

### v1.1 — Polish and Tablets (Weeks 13–16)
- PDF export via print framework
- Two-column adaptive layout for tablets / landscape
- Weapon keyword glossary (in-line tooltips for [SUSTAINED HITS], [LETHAL HITS], etc.)
- Unit stat block visible in unit browser (expandable)
- Improved faction icons (custom SVG set)
- Points history chart (per-session, see how list evolved)

### v1.2 — Competitive Mode (Weeks 17–20)
- Pre-built "meta" list templates (community-contributed, read-only)
- Side-by-side list comparison view
- List legality check against specific event pack restrictions (custom points cap, detachment bans)
- QR code share (encode entire roster in a URL for opponent import)

### v1.3 — Game Companion (Weeks 21–26)
- In-game CP tracker (spend/refund stratagems)
- Command phase checklist (abilities that trigger each phase)
- Casualty tracker (track wounds remaining per unit)
- Round timer / game clock

### v2.0 — Community and Cloud (6+ months post-MVP)
- Optional user accounts (Google/GitHub sign-in)
- Cloud roster sync across devices
- Public roster sharing and forking
- Community stratagem ratings ("Most used in tournament play")
- Push notifications for new BSData releases

### Platform Expansion (Post-v2.0)
- iOS port (Kotlin Multiplatform Mobile — domain + data layers already platform-agnostic)
- Progressive Web App for desktop access
- Wear OS companion (CP tracker on watch)

---

## Open Source Strategy

The app will be open-sourced on GitHub under the MIT licence with the following considerations:

- **Data**: BSData data is CC-BY; attribution required in app credits.
- **Code**: MIT; contributions welcome.
- **Artwork**: All faction icons and UI art must be original or CC0; no GW IP.
- **Name**: "Roster Forge 40K" is a descriptive name; not a GW trademark.

Community contributions are encouraged for:
- Faction-specific validation edge cases
- Parser improvements for new BSData schema changes
- Translations (i18n in v1.2+)

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| BSData schema changes break parser | Medium | High | Version-check at parse time; skip unknown elements; community issues flag changes fast |
| GW cease-and-desist | Low | Critical | Do not reproduce GW text verbatim; use only openly published BSData; keep rules text minimal |
| BSData repository goes unmaintained | Low | High | Maintain a fork; data bundled in APK so existing users unaffected |
| Complex wargear constraint parsing fails | Medium | Medium | Comprehensive integration tests against real `.cat` files; log and skip unparseable options |
| Play Store rejection | Low | Medium | No ads, no user tracking in MVP; straightforward content |
