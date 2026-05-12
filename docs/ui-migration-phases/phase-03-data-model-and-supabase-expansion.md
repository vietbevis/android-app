# Phase 03 - Data Model And Supabase Expansion

## Goal

Expand the domain and Supabase schema for the target CapMoney feature set while preserving existing MVP data and security.

## Current State

- Existing tables: `profiles`, `wallets`, `categories`, `transactions`, `transaction_photos`.
- Existing storage bucket: private `transaction-photos`.
- RLS protects rows by `user_id = auth.uid()`.
- Existing domain models cover money, transactions, wallets, categories, and profile.

## Target UX

- Users can manage budgets, transfers, loans, investments, recurring transactions, shared/group transactions, and preferences without losing the simple daily capture flow.
- Schema supports the UI shown in the reference: monthly budgets, account balances, loan setup, recurring entries, shared transaction lists, language/theme/currency settings, and optional location for map reporting.

## Implementation Tasks

- Add domain models and repository contracts for:
  - `Budget`: name, amount, period, type, category scope, color, archived flag.
  - `RecurringTransaction`: amount, type, wallet, category, schedule, next run, enabled flag.
  - `Transfer`: from wallet, to wallet, amount, fee, occurred date, note.
  - `Loan`: type, principal, counterparty, interest mode, interest value, start date, due schedule, status.
  - `Investment`: name, type, principal/value, note, status.
  - `UserPreference`: theme, language, currency, start-of-week, default wallet.
  - `Friend`, `Group`, `SharedTransaction` for later social workflows.
- Add Supabase migrations for new tables and indexes.
- Add `location_lat`, `location_lng`, and `location_label` as nullable transaction metadata if map reporting is in scope for v1.
- Add DTOs and mappers matching the existing data layer pattern.
- Ensure all new tables have `created_at`, `updated_at`, archive/status fields where appropriate, and user ownership or membership-based access.

## Data/API Changes

- Add tables:
  - `budgets`
  - `recurring_transactions`
  - `transfers`
  - `loans`
  - `investments`
  - `user_preferences`
  - `friends`
  - `groups`
  - `group_members`
  - `shared_transactions`
- RLS:
  - User-owned tables use `user_id = auth.uid()`.
  - Group/shared tables require membership policies.
  - Storage remains private; do not expose service-role keys.
- Existing tables must remain backward-compatible.

## Edge Cases

- Transfers must not double-count as income/expense in statistics unless explicitly requested.
- Archived wallets/categories/budgets remain visible in historical records.
- Loan and recurring schedules need timezone-safe date handling.
- Group sharing policies must not allow a user to infer another user's private data outside shared records.

## Tests

- Unit tests for new mappers and enum serialization.
- Repository tests or integration checks for create/list/update/archive flows.
- Supabase advisor checks after migrations.
- Manual RLS test with two real accounts for private and shared data.

## Acceptance Criteria

- New schema supports all target feature areas.
- Existing MVP data still loads.
- RLS is enabled and verified for every new exposed table.
- Domain and repository contracts are ready for UI phases.

