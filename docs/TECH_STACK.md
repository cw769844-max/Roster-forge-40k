# Roster Forge 40K — Android Technology Stack

## Language and Core Runtime

| Component | Choice | Rationale |
|---|---|---|
| Language | **Kotlin** | Idiomatic Android, concise, null-safe |
| Min SDK | **API 26 (Android 8.0)** | ~95% device coverage; required for several Jetpack APIs |
| Target SDK | **API 35** | Latest stable |
| Build system | **Gradle (KTS)** | Type-safe build scripts, version catalogue |

---

## UI Layer

| Component | Library | Version |
|---|---|---|
| UI toolkit | **Jetpack Compose** (Material 3) | BOM `2024.x` |
| Navigation | **Navigation Compose** | `2.8.x` |
| Icons | **Material Symbols** extended icon set | bundled with M3 |
| Animations | Compose animation APIs (built-in) | — |
| Theming | `MaterialTheme` with custom colour schemes | — |
| Window size classes | `WindowSizeClass` (adaptive layouts) | `androidx.window:window` |
| Image loading | **Coil** (Compose integration) | `2.x` |

### Why Jetpack Compose over XML Views?

- Eliminates the `ViewBinding` / `Adapter` boilerplate that makes BattleScribe-style nested list UIs painful.
- State-driven rendering maps cleanly onto the `ViewModel → StateFlow → UI` pipeline.
- Easier to implement the collapsible card pattern for unit display.
- Modern skill — easier to hire / contribute for open source.

---

## State Management and Architecture

| Component | Choice |
|---|---|
| Architecture pattern | **Clean Architecture** (Presentation / Domain / Data) |
| UI state | `ViewModel` + `StateFlow` / `SharedFlow` |
| UI state holder | Sealed `UiState` classes per screen |
| Side effects | `Channel` → `receiveAsFlow()` for one-shot events (snackbars, navigation) |
| Lifecycle | `collectAsStateWithLifecycle()` in Composables |

---

## Dependency Injection

| Component | Choice | Rationale |
|---|---|---|
| DI framework | **Hilt** | First-party, integrates with ViewModel and WorkManager |

Module structure:
- `DatabaseModule` — Room DAOs and database instance
- `RepositoryModule` — Repository bindings
- `NetworkModule` — OkHttp + Retrofit clients
- `ParserModule` — BattleScribe XML parser dependencies

---

## Local Data Persistence

| Component | Choice |
|---|---|
| Database | **Room** (SQLite) |
| Preferences | **DataStore (Proto)** for app settings and data version metadata |
| File storage | `Context.filesDir` for cached `.cat` / `.gst` files |

Room is used for all structured data (factions, units, rosters). DataStore holds simple key-value config (current data version, theme preference, etc.).

---

## Networking

| Component | Choice |
|---|---|
| HTTP client | **OkHttp 4** |
| REST abstraction | **Retrofit 2** |
| Serialization | **Kotlin Serialization** (`kotlinx.serialization`) |
| GitHub API usage | `api.github.com/repos/BSData/wh40k-10e/releases` for version checks; raw file downloads for `.zip` archives |

---

## Async / Concurrency

| Component | Choice |
|---|---|
| Async model | **Kotlin Coroutines** + **Flow** |
| IO dispatcher | `Dispatchers.IO` for DB and network |
| Background work | **WorkManager** for scheduled data update checks |
| Structured concurrency | `viewModelScope` / `lifecycleScope` |

---

## Data Parsing

| Component | Choice |
|---|---|
| XML parser | **Android `XmlPullParser`** (built-in, zero dependency) |
| ZIP extraction | `java.util.zip.ZipInputStream` (built-in) |
| Fallback | `org.xmlpull.v1.XmlPullParser` for unit-test environments |

BSData `.cat` and `.gst` files are ZIP-compressed XML. The parser pipeline:
1. Download release `.zip` from GitHub.
2. Extract member files.
3. Stream-parse each file with `XmlPullParser` (SAX-style, low memory).
4. Map parsed elements to domain objects.
5. Bulk-insert into Room via `@Transaction`.

---

## Testing

| Layer | Framework |
|---|---|
| Unit tests | **JUnit 5** + **Mockk** |
| Coroutine tests | `kotlinx-coroutines-test` |
| Room tests | `androidx.room:room-testing` (in-memory DB) |
| Compose UI tests | `androidx.compose.ui:ui-test-junit4` |
| Integration | **Hilt testing** support |
| Coverage | JaCoCo |

---

## Build and Tooling

| Tool | Purpose |
|---|---|
| **ktlint** | Kotlin code style enforcement |
| **detekt** | Static analysis |
| **GitHub Actions** | CI: build, lint, test on every push |
| **R8 / ProGuard** | Release build shrinking and obfuscation |
| Gradle Version Catalogue (`libs.versions.toml`) | Single source of dependency versions |

---

## Third-Party Library Summary

```toml
# libs.versions.toml (abbreviated)
[versions]
compose-bom          = "2024.09.00"
navigation-compose   = "2.8.0"
hilt                 = "2.52"
room                 = "2.6.1"
retrofit             = "2.11.0"
okhttp               = "4.12.0"
coil                 = "2.7.0"
kotlinx-serialization = "1.7.3"
workmanager          = "2.9.1"
datastore            = "1.1.1"

[libraries]
compose-bom             = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui              = { group = "androidx.compose.ui", name = "ui" }
compose-material3       = { group = "androidx.compose.material3", name = "material3" }
compose-navigation      = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }
hilt-android            = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler           = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
room-runtime            = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx                = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler           = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
retrofit                = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
okhttp-logging          = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
coil-compose            = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
kotlinx-serialization   = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
workmanager-ktx         = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workmanager" }
datastore-proto         = { group = "androidx.datastore", name = "datastore", version.ref = "datastore" }
```

---

## Project Module Structure

```
app/
├── src/main/
│   ├── java/com/rosterforge/wh40k/
│   │   ├── di/                     # Hilt modules
│   │   ├── data/
│   │   │   ├── local/              # Room DB, DAOs, entities
│   │   │   ├── remote/             # Retrofit services, DTOs
│   │   │   ├── parser/             # BattleScribe XML parser
│   │   │   └── repository/         # Repository implementations
│   │   ├── domain/
│   │   │   ├── model/              # Domain data classes
│   │   │   ├── repository/         # Repository interfaces
│   │   │   └── usecase/            # Use case classes
│   │   └── presentation/
│   │       ├── home/
│   │       ├── build/
│   │       ├── view/
│   │       ├── stratagems/
│   │       ├── settings/
│   │       ├── common/             # Shared Composables
│   │       └── navigation/         # NavGraph setup
│   └── res/
└── build.gradle.kts
```
