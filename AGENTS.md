# AGENTS.md

This file is the working guide for AI coding agents in this repository. Read it before making changes. It explains the project shape, implementation expectations, verification commands, and safety rules for future feature work.

## Project Summary

This is an Android personal finance app project named `APK Basic`, with the app displayed as `SnapChi`. The product direction is a camera-first finance manager: users can sign in, capture receipts or transaction photos, save transactions, manage wallets and categories, view dashboards/statistics, and store backend data in Supabase.

The app uses:

- Kotlin
- Android Gradle Plugin with Gradle Kotlin DSL
- Jetpack Compose and Material 3
- AndroidX Navigation-style app structure implemented in Compose
- ViewModels and lifecycle Compose helpers
- CameraX for capture flows
- Supabase Kotlin SDK for Auth, PostgREST, and Storage
- JUnit tests for domain/data logic

## Repository Layout

- `app/`: Android application module.
- `app/src/main/java/vn/vietbevis/apkbasic/`: Kotlin source root.
- `app/src/main/res/`: Android XML resources, strings, colors, themes, drawables, launcher assets.
- `app/src/test/`: local JVM tests.
- `app/src/androidTest/`: instrumented Android tests.
- `docs/`: planning, implementation phases, release checklist, and handoff notes.
- `supabase/migrations/`: SQL migrations for the Supabase backend.
- `gradle/libs.versions.toml`: dependency and plugin version catalog.
- `build.gradle.kts`, `settings.gradle.kts`, `app/build.gradle.kts`: Gradle configuration.
- `local.properties`: local machine config and Supabase publishable values. Do not commit secrets here.

## Architecture Guide

Follow the existing package structure:

- `core/common`: shared utilities, date codecs, error/result helpers.
- `core/di`: manual dependency container.
- `core/navigation`: root app composition, destinations, navigation state.
- `core/supabase`: Supabase config/provider setup.
- `domain/model`: pure business models.
- `domain/repository`: repository interfaces.
- `domain/reporting`: pure calculators/aggregators for finance reports.
- `domain/validation`: validation logic.
- `data/*`: Supabase DTOs, mappers, and repository implementations.
- `feature/*`: Compose screens and ViewModels grouped by feature.
- `ui/components`: reusable Compose UI components.
- `ui/theme`: Compose theme, colors, typography.

Preferred dependency direction:

```text
feature -> domain repository interfaces/domain models
data -> domain repository interfaces/domain models
core/di -> data implementations + core providers
domain -> no Android UI, no Supabase SDK
```

Keep domain logic as pure Kotlin when practical. This makes it easy to test without an emulator.

## Current Product Areas

Existing or planned feature areas include:

- Auth: login, signup, logout, auth state.
- Onboarding bootstrap: profile, default wallet, default categories.
- Dashboard/Home: monthly overview and transaction feed.
- Capture: CameraX preview, photo capture, transaction form, upload to Supabase Storage.
- Transactions: list, create/save flows, delete with confirmation.
- Wallets/Accounts: wallets, transfers, loans, investments.
- Categories and Budgets: category management and budget progress.
- Statistics: finance summaries and insights.
- Profile/Settings: preferences and account controls.

Before adding a feature, inspect nearby feature packages and reuse established patterns for state, events, loading/error display, and repository access.

## Kotlin And Compose Conventions

- Prefer small, focused composables.
- Keep screen state in a ViewModel when the state is not purely local UI state.
- Expose immutable UI state from ViewModels.
- Avoid doing repository calls directly inside composables.
- Every UI view/screen/composable that is newly created or significantly changed must include a corresponding Compose `@Preview`.
- Keep previews lightweight by passing sample state/data instead of real repositories, ViewModels, Supabase clients, camera objects, or Android runtime-only dependencies.
- If a preview cannot be added for a specific view, document the reason in the final response and consider extracting a previewable stateless composable.
- Use Material 3 components and existing app theme/colors.
- Use `stringResource` for user-facing text when the text is stable and belongs in resources.
- All Vietnamese user-facing text must use proper Vietnamese accents. This applies to Compose UI text, Android string resources, validation messages, docs, and final responses.
- Save files containing Vietnamese text as UTF-8 and verify the rendered text does not contain mojibake such as `Ã`, `áº`, `Ä`, or replacement characters.
- Add meaningful `contentDescription` for icon buttons and important controls.
- For forms, handle loading, empty, error, disabled, and success states deliberately.
- For financial amounts, avoid floating-point math. Use the existing `Money` model/patterns.

## Data And Supabase Conventions

- Repository interfaces live in `domain/repository`.
- Supabase-backed implementations live under `data/<area>/`.
- DTOs should be explicit and should include mapper functions to/from domain models.
- Keep Supabase table/column names aligned with migration SQL.
- Preserve RLS assumptions: users should only access their own data.
- Never put service role keys in Android source, docs, tests, or resources.
- The Android app may use a Supabase publishable key only.
- Supabase values are read by Gradle from Gradle properties, environment variables, or `local.properties`:
  - `SUPABASE_URL`
  - `SUPABASE_PUBLISHABLE_KEY`
- Do not hardcode real credentials in Kotlin files.
- For Storage paths, keep user-scoped prefixes where applicable, for example `{user_id}/...`.
- When changing schema, add a new migration under `supabase/migrations/` instead of editing an already-applied migration unless the user explicitly says the migration has not been applied anywhere.

## Error Handling

- Prefer existing `AppError`/result helper patterns where available.
- Surface user-friendly messages in UI state.
- Keep technical details useful in code/logging, but avoid exposing secrets or raw backend internals in UI text.
- Handle network failures, empty data, unauthorized state, and validation failures distinctly when the user experience depends on it.

## Testing Expectations

Add or update tests when changing:

- Domain models or value objects.
- Validators.
- DTO mapping.
- Reporting/calculation logic.
- Parsing or formatting logic.
- Behavior with non-trivial branching.

Prefer local JVM tests in `app/src/test/` for pure logic. Use Android instrumentation tests only when Android framework behavior must be exercised.

Existing tests include examples for:

- `Money`
- transaction validation
- DTO mapping
- finance summary calculations
- budget progress calculations
- calculator engine behavior

## Build And Verification Commands

Run commands from the repository root on Windows PowerShell.

Fastest common verification:

```powershell
.\gradlew.bat test
```

Build debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Build release APK:

```powershell
.\gradlew.bat assembleRelease
```

Stop Gradle daemon if cache/daemon issues appear:

```powershell
.\gradlew.bat --stop
```

Recommended verification by change type:

- Pure Kotlin/domain/data mapping change: `.\gradlew.bat test`
- Compose UI or ViewModel feature change: `.\gradlew.bat test` then `.\gradlew.bat assembleDebug`
- Gradle/dependency change: `.\gradlew.bat test` then `.\gradlew.bat assembleDebug`
- Release-related change: `.\gradlew.bat test`, `.\gradlew.bat assembleDebug`, and `.\gradlew.bat assembleRelease`
- Camera/device behavior: also perform manual emulator/device testing when available
- Supabase RLS/auth behavior: test with real accounts when possible

Important: avoid running multiple Gradle tasks in parallel in this repository. Previous work saw Kotlin incremental cache conflicts when `test` and `assembleDebug` ran at the same time. Run Gradle commands sequentially.

## Documentation To Check

Before large changes, inspect these docs:

- `docs/session-handoff.md`: prior implementation context and current status.
- `docs/release-checklist.md`: release and manual testing expectations.
- `docs/phases/`: original implementation phase plan.
- `docs/ui-migration-phases/`: UI migration/product direction notes.

Update docs when a change affects architecture, release readiness, backend schema, or the implementation roadmap.

## Implementation Workflow For AI Agents

1. Read this file.
2. Inspect the relevant package/files before editing.
3. Identify the narrowest set of files required for the task.
4. Preserve unrelated local changes.
5. Implement using existing patterns.
6. Add or update focused tests when behavior changes.
7. Run the narrowest relevant verification command.
8. Report what changed and which verification ran.

For larger features, leave a short implementation note in the final response that mentions:

- main files changed
- behavioral effect
- tests/builds run
- anything still requiring manual device/backend validation

## Git And Change Safety

- Do not run destructive git commands unless the user explicitly asks.
- Do not revert user changes.
- Do not rewrite unrelated files.
- Do not update dependency versions casually.
- Do not format the entire project unless the user requested formatting.
- Keep commits, if requested, focused and named clearly.
- Never include local secrets in commits.

## Secrets And Local Files

Treat these as sensitive:

- Supabase service role keys
- signing keys/keystores
- passwords
- private tokens
- private `.env` values
- machine-specific SDK paths

`local.properties` may contain local SDK and publishable Supabase values. Do not expose its full contents unless the user explicitly asks and understands the risk. Never add service role keys to Android client code.

## Android Permissions And Device Behavior

The app may use:

- `android.permission.CAMERA`
- `android.permission.INTERNET`

When changing camera or upload flows:

- handle permission denied and permanently denied states
- handle camera unavailable cases
- keep capture controls accessible
- test on emulator/device when possible
- verify Storage upload with a real authenticated user when possible

## UI/UX Expectations

This is a finance utility app, so prioritize clarity and trust:

- Keep screens practical and scannable.
- Use restrained visual hierarchy.
- Show amounts, dates, wallet/category names, and transaction types clearly.
- Confirm destructive actions such as delete/archive.
- Disable submit buttons while saving.
- Show progress and error states.
- Preserve a stable layout on small screens.
- Avoid decorative UI that makes finance information harder to read.

## Backend Migration Rules

When adding or changing database behavior:

- Create a new timestamped SQL migration in `supabase/migrations/`.
- Keep RLS enabled for user data tables.
- Add indexes for foreign keys and common filters when needed.
- Avoid broad public access policies.
- Document any manual Supabase dashboard steps in `docs/` or the final response.
- Keep Storage bucket policies user-scoped.

## Dependency Rules

- Add dependencies through `gradle/libs.versions.toml`.
- Use version catalog aliases in Gradle files.
- Prefer AndroidX, Kotlin, Compose, CameraX, Ktor, and Supabase libraries already used by the app.
- Avoid adding a large framework for a small feature.
- After dependency changes, run at least `.\gradlew.bat assembleDebug`.

## Style Notes

- Use clear Kotlin names.
- Prefer explicit domain names over abbreviations.
- Keep comments sparse and useful.
- Keep files cohesive; do not create abstractions before they remove real duplication or complexity.
- Prefer constructor parameters and simple dependency passing over hidden global state.
- Follow existing package naming: `vn.vietbevis.apkbasic`.

## Known Manual Verification Gaps

Some behavior cannot be fully verified with local JVM tests:

- Camera permission and capture on real devices.
- Supabase auth flows with real email accounts.
- Supabase RLS isolation between two users.
- Storage upload/download policies.
- Release signing with a production keystore.

When touching these areas, state clearly what was verified locally and what still needs manual/device/backend testing.

## Useful Search Commands

```powershell
rg "TODO|FIXME" app docs supabase
rg "SUPABASE|service_role|secret" app docs supabase
rg "data class .*Dto" app/src/main/java
rg "interface .*Repository" app/src/main/java
rg "@Composable" app/src/main/java
```

Use `rg --files` to understand the repo quickly.

## Final Response Expectations For AI Agents

When finished, respond briefly with:

- what changed
- where it changed
- what verification ran
- any remaining risk or manual follow-up

Do not claim tests passed unless they were actually run and passed.
