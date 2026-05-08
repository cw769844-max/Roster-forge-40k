# Roster Forge 40K — App Architecture

## 1. Architectural Style

The app follows **Clean Architecture** with three distinct layers separated by dependency rules. Inner layers have zero knowledge of outer layers.

```
┌─────────────────────────────────────────┐
│           PRESENTATION LAYER            │  ← Jetpack Compose + ViewModels
│  (Screens, ViewModels, UI State)        │
├─────────────────────────────────────────┤
│             DOMAIN LAYER                │  ← Pure Kotlin, no Android deps
│  (Use Cases, Domain Models, Interfaces) │
├─────────────────────────────────────────┤
│              DATA LAYER                 │  ← Room, Retrofit, XML Parser
│  (Repositories, Local DB, Remote API)  │
└─────────────────────────────────────────┘
```

Dependency direction: Presentation → Domain ← Data

---

## 2. Layer Details

### 2.1 Presentation Layer

Each screen owns:
- A `@HiltViewModel` **ViewModel** exposing a `StateFlow<UiState>` and a `SharedFlow<UiEvent>` for one-shot effects.
- A root **Composable** (e.g., `BuildScreen`) that collects state and delegates interactions back to the ViewModel.
- Sub-composables for reusable UI elements (unit cards, wargear selectors, etc.).

```
Screen ──(user action)──▶ ViewModel ──(use case call)──▶ Domain
  ▲                           │
  └──(UiState / UiEvent)──────┘
```

**UiState pattern:**
```kotlin
sealed interface BuildUiState {
    object Loading : BuildUiState
    data class Success(
        val roster: RosterDomain,
        val availableUnits: List<UnitSummary>,
        val validationResult: ValidationResult,
        val pointsUsed: Int,
        val pointsLimit: Int,
    ) : BuildUiState
    data class Error(val message: String) : BuildUiState
}
```

**UiEvent pattern (one-shot):**
```kotlin
sealed interface BuildUiEvent {
    data class ShowSnackbar(val message: String) : BuildUiEvent
    object NavigateToUnitBrowser : BuildUiEvent
    data class NavigateToUnitCustomization(val rosterUnitId: String) : BuildUiEvent
}
```

---

### 2.2 Domain Layer

Contains pure business logic with **no Android framework dependencies**.

**Use Cases (one public function each):**

| Use Case | Responsibility |
|---|---|
| `CreateRosterUseCase` | Validate inputs and persist a new roster |
| `AddUnitToRosterUseCase` | Validate unit legality and append to roster |
| `RemoveUnitFromRosterUseCase` | Remove unit and cascade (detach leader, remove enhancement) |
| `UpdateWargearSelectionUseCase` | Validate and save wargear selection |
| `AssignEnhancementUseCase` | Validate enhancement eligibility and assign |
| `AttachLeaderUseCase` | Validate leader/bodyguard keyword match and attach |
| `ValidateRosterUseCase` | Run full validation suite; return `ValidationResult` |
| `GetAvailableUnitsUseCase` | Filter units for chosen faction and detachment |
| `GetStratagemsForRosterUseCase` | Return core + faction + detachment stratagems |
| `SyncDataUseCase` | Orchestrate data download, parse, and store |
| `CheckDataVersionUseCase` | Compare installed vs latest BSData release tag |

**Domain Models** are plain data classes with no Room annotations. See `DATA_MODEL.md`.

---

### 2.3 Data Layer

#### Repositories
Each repository interface is defined in the domain layer; implementations live in the data layer and are injected via Hilt.

| Repository | Primary Data Sources |
|---|---|
| `RosterRepository` | Room (rosters, roster_units, selections) |
| `CatalogueRepository` | Room (parsed faction / unit / wargear data) |
| `StratagemRepository` | Room (stratagems table) |
| `DataSyncRepository` | GitHub API (releases), FileSystem (cached XML), Room (write) |
| `AppSettingsRepository` | DataStore |

#### Data Flow — Roster Operations
```
ViewModel
  └─▶ AddUnitToRosterUseCase
        └─▶ CatalogueRepository.getUnit(id)         [Room read]
        └─▶ ValidateRosterUseCase                   [pure logic]
        └─▶ RosterRepository.addUnit(rosterUnit)    [Room write]
              └─▶ Flow<RosterDomain>                [re-emits to ViewModel]
```

#### Data Flow — Data Sync
```
WorkManager / User trigger
  └─▶ SyncDataUseCase
        └─▶ DataSyncRepository.fetchLatestRelease() [GitHub API]
        └─▶ DataSyncRepository.downloadZip(url)     [OkHttp]
        └─▶ BsXmlParser.parse(zipStream)            [XmlPullParser]
        └─▶ CatalogueRepository.replaceAll(data)    [Room bulk insert @Transaction]
        └─▶ AppSettingsRepository.setDataVersion(tag)
```

---

## 3. Navigation Architecture

Navigation is managed by **Navigation Compose** with a single `NavHost` and typed route objects.

```
NavGraph
├── HomeGraph
│   └── HomeScreen
├── BuildGraph
│   ├── CreateRosterScreen
│   ├── FactionSelectScreen
│   ├── DetachmentSelectScreen
│   ├── BuildRosterScreen          ← main editing hub
│   │   ├── UnitBrowserScreen
│   │   ├── UnitCustomizeScreen
│   │   ├── LeaderAttachScreen
│   │   └── EnhancementScreen
│   └── ValidationScreen
├── ViewGraph
│   ├── ViewRosterScreen
│   └── StratagemReferenceScreen
└── SettingsGraph
    └── SettingsScreen
```

Routes are sealed classes to prevent string-literal bugs:

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FactionSelect : Screen("faction_select/{rosterId}") {
        fun withArgs(rosterId: String) = "faction_select/$rosterId"
    }
    // ...
}
```

Back-stack behaviour:
- `BuildGraph` uses `saveState = true` / `restoreState = true` so unit browser filters survive navigation.
- `ViewGraph` is launched as a separate `NavHost` entry so the back button returns to the roster list, not the edit screen.

---

## 4. State Management Details

### Single source of truth
All persistent state lives in Room. `Flow` emissions drive the UI automatically when data changes. ViewModels **never** hold their own mutable copy of persisted data; they observe via `Flow`.

### Optimistic UI updates
For wargear toggles and unit selection (latency-sensitive), the ViewModel applies a local optimistic change immediately, then confirms or rolls back based on the Room write result. This prevents visible lag on mid-range devices.

### Roster validation is reactive
`ValidateRosterUseCase` is called inside a `combine()` of all roster-related flows. Any change to the roster re-triggers validation and emits a new `ValidationResult` to the UI within one compose frame cycle.

---

## 5. Offline-First Strategy

| Concern | Approach |
|---|---|
| Catalogue data | Stored in Room after first sync; app never requires network after that |
| Rosters | Room only; no network dependency |
| Data updates | Optional; triggered manually or by WorkManager (weekly) |
| Partial downloads | Catalogue files downloaded individually; partial set is still usable |
| Version mismatch | App warns if installed data is >30 days old |

---

## 6. Background Work

**WorkManager** handles the periodic data freshness check:

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.UNMETERED)
    .build()

val request = PeriodicWorkRequestBuilder<DataUpdateWorker>(7, TimeUnit.DAYS)
    .setConstraints(constraints)
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
    .build()
```

The worker only downloads when a newer GitHub release tag exists. The actual download is deferred to user-triggered action from the Settings notification badge.

---

## 7. Error Handling Strategy

| Layer | Approach |
|---|---|
| Domain | Use cases return `Result<T>` sealed class (`Success` / `Failure(cause)`) |
| Repository | `runCatching` wraps DB and network calls; re-throws as domain exceptions |
| ViewModel | Maps `Failure` to `UiState.Error` or `UiEvent.ShowSnackbar` |
| Parsing | Per-file try-catch; corrupt files are skipped with a logged warning, not crash |

---

## 8. Dependency Graph (Hilt)

```
@HiltAndroidApp Application
    ├── @Singleton DatabaseModule
    │       ├── AppDatabase (Room)
    │       ├── RosterDao
    │       ├── UnitDao
    │       ├── StratagemDao
    │       └── ...
    ├── @Singleton NetworkModule
    │       ├── OkHttpClient
    │       └── GithubApiService (Retrofit)
    ├── @Singleton RepositoryModule
    │       ├── RosterRepositoryImpl
    │       ├── CatalogueRepositoryImpl
    │       ├── StratagemRepositoryImpl
    │       └── DataSyncRepositoryImpl
    └── @ViewModelScoped (injected into ViewModels via @HiltViewModel)
```
