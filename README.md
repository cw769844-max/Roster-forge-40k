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

## Building & Installing the App

### Option A — GitHub Actions (recommended)

Every push to `main` or a `claude/**` branch automatically builds a debug APK and
uploads it as a workflow artifact.

1. Open the **Actions** tab in this repository.
2. Click the latest **Build Debug APK** run.
3. Download the **roster-forge-40k-debug** artifact (a `.zip` containing `app-debug.apk`).
4. Unzip and sideload to your device (see below).

### Option B — Build locally

**Prerequisites**
- JDK 17 (e.g. `brew install temurin` / Adoptium)
- Android SDK 34 — Android Studio installs this automatically.
- `ANDROID_HOME` environment variable pointing at your SDK root, **or** a
  `local.properties` file in the project root:
  ```
  sdk.dir=/Users/<you>/Library/Android/sdk
  ```

```bash
# Clone and build
git clone https://github.com/cw769844-max/roster-forge-40k.git
cd roster-forge-40k
./gradlew :app:assembleDebug          # Linux / macOS
gradlew.bat :app:assembleDebug        # Windows

# APK output
app/build/outputs/apk/debug/app-debug.apk
```

### Sideloading (installing without Play Store)

1. **Enable Unknown Sources** on your Android device:
   - Android 8+: Settings → Apps → Special app access → Install unknown apps →
     select your file manager → Allow from this source.
2. Copy `app-debug.apk` to your device (USB, Google Drive, email, etc.).
3. Open a file manager on the device, navigate to the APK, tap it, and confirm
   the install prompt.
4. Find **Roster Forge 40K** in your app drawer and open it.

On first launch, two sample factions (Space Marines and Necrons) are seeded
automatically so you can explore the app without a network connection.
Use **Settings → Reset to sample data** to restore these at any time.

### Running unit tests

```bash
./gradlew :app:testDebugUnitTest
# HTML report: app/build/reports/tests/testDebugUnitTest/index.html
```

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
