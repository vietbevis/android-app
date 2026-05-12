# Phase 02 - Design System And Navigation

## Goal

Replace the MVP Material look with a reusable CapMoney design system and new app shell. This phase creates the visual foundation used by every later screen.

## Current State

- The app uses the default Compose Material 3 theme with light/dark support and simple cards/buttons.
- Navigation is driven by `AppDestination` with five MVP tabs, including capture as a tab.
- Existing icons are local XML drawables for home, camera, receipt, wallet, and settings.

## Target UX

- Use a dark-first premium finance palette:
  - App background: deep navy/near black.
  - Card surfaces: layered blue navy.
  - Primary action: bright blue.
  - Income: mint/green.
  - Expense: coral/red.
  - Warning/premium: yellow.
  - Muted text/icons: cool gray.
- Use dense, rounded, tactile controls: segmented controls, filter chips, stat cards, large amount text, circular icon buttons, and floating add actions.
- Bottom navigation has `Trang chủ`, `Thống kê`, `Tài khoản`, `Ngân sách`, `Cá nhân`.
- Capture is opened from FAB/modal, not from bottom navigation.

## Implementation Tasks

- Create a design token layer for colors, spacing, card radius, elevation/shadow equivalents, typography roles, icon sizes, and state colors.
- Build shared Compose components:
  - `CapScaffold`, `CapBottomBar`, `CapTopHeader`.
  - `CapCard`, `MetricCard`, `SegmentedControl`, `FilterChipRow`.
  - `IconCircleButton`, `FloatingAddButton`, `AmountText`, `SectionHeader`.
  - Empty/loading/error state components.
- Replace `AppDestination` entries with target tabs: `HOME`, `STATISTICS`, `ACCOUNTS`, `BUDGETS`, `PROFILE`.
- Add a separate capture route/modal state that can be launched from any tab.
- Update string resources for target Vietnamese labels.
- Add or replace icons for home, chart, account/card, budget/card, profile, plus, eye, filter, calendar, wallet, bank, category, transfer, loan, settings, globe, moon, currency, star, mail, share.

## Data/API Changes

- No database changes.
- Navigation interface changes from tab-based capture to modal capture.
- ViewModels should not depend on Material components; UI state remains pure Kotlin.

## Edge Cases

- Android navigation suite may switch layout on tablets; ensure the visual system still supports compact phones first.
- Long Vietnamese labels must fit bottom nav and compact chips.
- The app must not rely only on color for income/expense; include signs, icons, and labels.

## Tests

- Compose UI test: bottom nav shows five target tabs and preserves selected tab state.
- Compose UI test: FAB opens capture modal route from at least Home.
- Screenshot/manual checks on small phone and large phone sizes for clipped text and overlapping controls.

## Acceptance Criteria

- App shell matches the target tab model.
- Capture is no longer a bottom tab.
- Shared components cover the visual patterns needed by later phases.
- Existing MVP screens still compile while they are being migrated behind the new shell.

