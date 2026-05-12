# Phase 04 - Home Calendar And Transaction Feed

## Goal

Build the new `Trang chủ` tab: greeting, daily/monthly summary, account filters, calendar view, day feed, transaction cards, and quick add interactions.

## Current State

- `DashboardScreen` shows monthly income, expense, net change, wallet balances, category totals, recent transactions, and a capture CTA.
- `TransactionsScreen` shows a simple monthly list and delete confirmation.
- There is no home calendar, day/month toggle, thumbnail transaction card, search, or detail route.

## Target UX

- Header shows time-aware greeting, display name, avatar, and daily spending status chip.
- Segmented control switches `Ngày` and `Tháng`.
- Summary cards show spending and income with visibility toggles.
- Wallet filter chips show `Tất cả`, `Wallet`, bank accounts, and other wallets.
- Month calendar shows days, add buttons, transaction dots/amount hints, and disabled future or empty states.
- Day view shows selected date, total spent badge, transaction thumbnail cards, and a floating add button.

## Implementation Tasks

- Create `HomeScreen` and `HomeViewModel` using existing wallet/category/transaction repositories.
- Add period state: selected date, selected month, view mode `DAY` or `MONTH`, selected wallet filter.
- Add calendar aggregate model per day: income, expense, transaction count, hasPhoto, dominant category color.
- Reuse existing transactions list data but group by selected date/month and include category/wallet display metadata.
- Add transaction card UI with optional photo thumbnail, category chip, signed amount, and click target.
- Add transaction detail route or modal with view/edit/delete entry points; edit can be implemented in a later subtask but the route must be reserved.
- Add empty states:
  - No spending today.
  - No transactions this month.
  - No wallet selected or wallet archived.
- Add FAB to open capture modal with selected date/wallet prefilled.

## Data/API Changes

- No required schema change if using existing transactions.
- Optional: use nullable transaction location fields from Phase 03 for future map/reporting.
- ViewModel exposes pure UI state for calendar days, summary metrics, wallet filters, and selected transactions.

## Edge Cases

- Month starts on Vietnamese locale week layout; use a consistent start-of-week from preferences when available.
- Future dates should not suggest existing transactions but may allow scheduling only if recurring flow is in scope.
- Transactions without category show `Khác`.
- Deleted or archived wallets/categories still display historical names where available.

## Tests

- Unit tests for calendar day aggregation and month grid generation.
- ViewModel tests for view mode switch, wallet filter, selected date, and refresh error.
- Compose UI tests for empty month, populated day, and FAB opening capture.

## Acceptance Criteria

- `Trang chủ` replaces the old dashboard/transaction list as the primary screen.
- Users can scan monthly activity and drill into a date.
- Existing transactions with photos appear as visual cards.
- FAB opens capture with sensible prefilled context.

