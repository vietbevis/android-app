# CapMoney UI Migration Phases

## Goal

Migrate the current SnapChi MVP into the CapMoney-style experience shown in the reference screens: premium dark UI, bottom navigation with five primary tabs, fast camera-first transaction capture, strong monthly overview, budget management, statistics, accounts, loans, and profile/settings.

This migration keeps the existing Kotlin, Jetpack Compose, Supabase, and repository architecture. The work must be incremental and must not destroy existing transaction, wallet, category, profile, or photo data.

## Product Direction

- Make the first screen useful immediately: show greeting, daily/monthly summary, filters, calendar or transaction feed, and a floating add action.
- Replace the generic Material MVP look with a custom dark finance design system: deep navy surfaces, blue primary actions, coral expense accents, mint income accents, dense rounded cards, large numeric typography, and icon-led controls.
- Move capture out of the bottom navigation. Capture becomes a modal/full-screen flow opened from a floating action button, shortcuts, and contextual plus buttons.
- Keep Vietnamese as the primary language. All user-facing copy must fit on small Android screens and use consistent finance terms.
- Treat the reference images as the visual target, but implement with native Compose patterns and accessible semantics.

## Target App Shell

The new bottom navigation has five tabs:

- `Trang chủ`: home calendar, daily/monthly feed, quick capture.
- `Thống kê`: period filters, charts, category totals, and transaction-location data view.
- `Tài khoản`: accounts, transfers, loans, investments.
- `Ngân sách`: budget list, add/edit budget, progress and daily remaining.
- `Cá nhân`: profile overview, preferences, friends/groups/shared transactions, settings.

Capture is a separate route/modal opened by FAB and contextual add buttons.

## Phase Order

1. `phase-01-product-audit-and-target-spec.md`
2. `phase-02-design-system-and-navigation.md`
3. `phase-03-data-model-and-supabase-expansion.md`
4. `phase-04-home-calendar-and-transaction-feed.md`
5. `phase-05-camera-capture-and-calculator.md`
6. `phase-06-statistics-and-insights.md`
7. `phase-07-accounts-transfers-loans-investments.md`
8. `phase-08-budgets-and-recurring-transactions.md`
9. `phase-09-profile-settings-and-social.md`
10. `phase-10-polish-accessibility-offline-and-release.md`

## Shared Acceptance Rules

- Existing users can still sign in and see their current wallets, categories, transactions, and photos.
- Supabase RLS remains enabled for all user data. Public clients never receive service-role or secret keys.
- Every new feature has empty, loading, error, and success states.
- Every screen works on small phones without clipped Vietnamese text.
- Unit tests cover domain calculations; ViewModel tests cover state transitions; key Compose flows have UI tests.
