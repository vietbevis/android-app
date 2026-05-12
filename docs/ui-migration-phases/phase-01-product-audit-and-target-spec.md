# Phase 01 - Product Audit And Target Spec

## Goal

Create the decision-complete target spec for migrating the current MVP to the CapMoney reference experience. This phase defines what changes, what stays, and how success is measured before code migration starts.

## Current State

- Current app uses Kotlin, Jetpack Compose, Supabase Auth/PostgREST/Storage, and a simple `NavigationSuiteScaffold`.
- Current bottom destinations are `Dashboard`, `Capture`, `Transactions`, `Wallets`, and `Settings`.
- Implemented MVP capabilities include auth, onboarding defaults, wallet/category CRUD/archive, camera capture, photo upload, transaction create/delete, dashboard aggregates, and monthly transaction history.
- Missing or incomplete capabilities include transaction detail/edit, advanced filters/search, budget management, recurring transactions, transfers, loans, investments, social/shared transactions, richer profile, gallery import, calculator keypad, charts, offline queue, and premium-level polish.

## Target UX

- New app identity is CapMoney-style: dark, polished, finance-focused, and fast for daily use.
- Home becomes the central daily/monthly control surface with greeting, summary chips, period toggle, wallet filters, calendar/feed, and FAB.
- Capture becomes a full-screen modal with camera preview, optional gallery/skip, calculator keypad, category/account/date chips, and save confirmation.
- Statistics becomes a visual analysis area with period filters, income/expense/balance cards, donut category chart, and map tab.
- Accounts, budgets, and profile become first-class tabs rather than secondary settings.

## Implementation Tasks

- Inventory current screens, ViewModels, repositories, domain models, DTOs, and Supabase tables against the target feature list.
- Produce a migration matrix:
  - `Dashboard` maps into `Trang chủ` and partly `Thống kê`.
  - `Capture` maps into a modal route opened by FAB.
  - `Transactions` maps into the `Trang chủ` feed and transaction detail route.
  - `Wallets` maps into `Tài khoản` plus settings category management.
  - `Settings` maps into `Cá nhân`.
- Define target routes, tab ownership, shared components, and phase dependencies.
- Decide which existing MVP code can be reused versus rewritten for the new UI.
- Document non-goals for the first migration pass: bank scraping, real investment market data, paid subscription enforcement, and production Apple sign-in unless separately scoped.

## Data/API Changes

- No schema changes in this phase.
- Define the target domain additions for later phases: `Budget`, `RecurringTransaction`, `Transfer`, `Loan`, `Investment`, `UserPreference`, `Friend`, `Group`, and `SharedTransaction`.
- Confirm existing data must remain readable without migration loss.

## Edge Cases

- Existing users may have no wallets/categories if onboarding failed; target screens must repair or explain this state.
- Existing transactions may not have photos, notes, category IDs, or rich metadata.
- Existing docs are partially encoded incorrectly; new docs must use clear UTF-8 Vietnamese.

## Tests

- No app tests required in this phase.
- Add an implementation checklist for later verification across unit, ViewModel, Compose UI, and manual device tests.

## Acceptance Criteria

- A migration matrix exists and is clear enough for implementation.
- Target app shell, feature ownership, and phase dependencies are unambiguous.
- Existing data preservation is explicitly required.
- Later schema and UI phases have no unresolved product decisions.

