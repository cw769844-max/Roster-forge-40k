# Roster Forge 40K

A free, mobile-first Warhammer 40,000 10th Edition army list builder for Android.

**Design goals:** Build, validate, and reference armies faster than BattleScribe with a cleaner phone interface and all gameplay features (stratagems, abilities, rules) free.

**Data source:** [BSData/wh40k-10e](https://github.com/BSData/wh40k-10e) — open data published under CC-BY.

---

## Documentation

| Document | Contents |
|---|---|
| [Product Specification](docs/PRODUCT_SPEC.md) | Goals, app modes, full feature list |
| [Architecture](docs/ARCHITECTURE.md) | Clean Architecture layers, state management, navigation, offline strategy |
| [Data Model](docs/DATA_MODEL.md) | Domain models, Room schema, DAO interfaces, points calculation |
| [UI Screens](docs/UI_SCREENS.md) | Screen-by-screen wireframes, component library, navigation flow |
| [Validation Logic](docs/VALIDATION_LOGIC.md) | Every validation rule with pseudocode and test strategy |
| [BattleScribe Parsing](docs/BATTLESCRIBE_PARSING.md) | XML parsing pipeline, link resolution, error handling |
| [Technology Stack](docs/TECH_STACK.md) | Android libraries, module structure, tooling |
| [Roadmap](docs/ROADMAP.md) | 12-week MVP sprints, post-MVP releases, risk register |

---

## Quick Stack Summary

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture (MVVM + Repository)
- **DI**: Hilt
- **Database**: Room
- **Async**: Coroutines + Flow
- **Networking**: Retrofit + OkHttp
- **Background**: WorkManager
- **Min SDK**: Android 8.0 (API 26)

---

## Licence

Code: MIT  
Data: [BSData CC-BY](https://github.com/BSData/wh40k-10e/blob/master/LICENSE)  
Roster Forge 40K is not affiliated with or endorsed by Games Workshop.
