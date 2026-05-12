# Phase 10 - Polish Accessibility Offline And Release

## Goal

Bring the migrated CapMoney experience to release quality: motion, haptics, accessibility, offline behavior, security checks, screenshots, and release readiness.

## Current State

- MVP has passed unit tests and debug/release builds before.
- Some manual tests still require emulator/device validation.
- UI is functional but not yet polished to the reference quality.

## Target UX

- App feels fast, coherent, and premium.
- State transitions are smooth but not distracting.
- Offline and Supabase errors are understandable.
- Accessibility labels, contrast, touch targets, and Vietnamese text fitting are reliable.
- Release artifacts and manual evidence are documented.

## Implementation Tasks

- Add motion polish:
  - Tab transitions.
  - FAB/capture modal transition.
  - Card press states.
  - Calendar month navigation.
  - Chart reveal animation.
- Add haptics for capture shutter, keypad taps, save success, validation failure, and destructive confirmation.
- Standardize all loading, empty, error, and retry states.
- Add offline behavior:
  - Detect no network.
  - Cache last loaded summaries/lists where practical.
  - Queue new transactions/photos only if a robust retry mechanism is implemented; otherwise show clear save limitation.
- Harden Supabase security:
  - Run advisors after schema changes.
  - Verify RLS with two accounts.
  - Scan app source for service-role/secret keys.
  - Confirm storage bucket policies.
- Add accessibility:
  - Content descriptions for icon-only buttons.
  - Minimum touch target sizes.
  - Screen-reader-friendly amount labels.
  - Contrast checks for dark UI.
- Update release docs and screenshots for each target tab and capture flow.

## Data/API Changes

- Optional local cache layer may be added, but must not become a second source of truth without sync rules.
- Offline queue requires durable local records with sync status and conflict handling; if not implemented, document as backlog.
- No service-role or secret keys in app, docs examples, or build config.

## Edge Cases

- User signs out while offline or while upload is pending.
- Photo upload succeeds but transaction save fails, or the reverse.
- App is killed during capture/save.
- Very large histories may make client-side aggregation slow.
- Accessibility font scaling may require more vertical space than the reference screenshots.

## Tests

- Full unit test suite.
- ViewModel test suite for new features.
- Compose UI tests for bottom nav, home, capture keypad, budgets, accounts, profile settings.
- `assembleDebug` and `assembleRelease`.
- Manual device/emulator test matrix:
  - Sign up/sign in/sign out.
  - Camera permission denied/granted.
  - Capture with photo, gallery, and skip photo.
  - Home day/month navigation.
  - Statistics filters.
  - Account transfer.
  - Budget create/detail.
  - Profile preferences.
  - RLS with two accounts.

## Acceptance Criteria

- Release checklist is complete with build evidence and manual test notes.
- App has no known clipped text or overlapping controls on target phone sizes.
- Security checks pass for Supabase tables and storage.
- The migrated UI matches the reference direction closely enough to replace the MVP experience.

