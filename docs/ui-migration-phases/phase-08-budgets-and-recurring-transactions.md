# Phase 08 - Budgets And Recurring Transactions

## Goal

Build the new `Ngân sách` tab and recurring transaction management.

## Current State

- No budget model, budget UI, daily remaining calculation, or recurring transaction feature exists.
- Categories already exist and can be used as budget scopes.

## Target UX

- Budget list matches the reference: header, add button, budget cards with icon, cycle, progress bar, percent spent, days remaining, spent, remaining, daily allowance, and total budget.
- Add budget screen has preview card, name field, amount field, period/cycle selector, budget type selector, color picker, and category sync option.
- Recurring transactions live under profile/settings or budget-related management and support periodic bills/income.

## Implementation Tasks

- Create `BudgetsScreen`, `BudgetDetailScreen`, `AddBudgetScreen`, and `BudgetsViewModel`.
- Implement budget scopes:
  - Total spending budget.
  - Category-specific budget.
- Implement cycles:
  - Daily.
  - Weekly.
  - Every two weeks.
  - Monthly.
  - Yearly.
  - Custom if schema supports it.
- Calculate budget progress:
  - Spent amount in current cycle.
  - Remaining amount.
  - Percent spent.
  - Days remaining.
  - Suggested daily allowance.
- Add budget color/icon picker aligned with categories.
- Add recurring transaction list/create/edit/archive:
  - amount, type, wallet, category, schedule, next run, note, enabled.
- Decide whether recurring transactions auto-create records or appear as due reminders for v1; default to due reminders unless background scheduling is explicitly implemented.

## Data/API Changes

- Requires `budgets` and `recurring_transactions` from Phase 03.
- Budget calculations query transactions by cycle range and category scope.
- Recurring transactions must use timezone-safe local date rules.

## Edge Cases

- Budget overrun should be visually clear but not block new transactions.
- If a category is archived, existing category budget remains readable but cannot be newly selected unless restored.
- Custom cycles must have valid start/end dates.
- Recurring reminders must not duplicate transactions after app restart.

## Tests

- Unit tests for budget cycle range generation.
- Unit tests for percent spent, daily remaining, over-budget, no-days-left, and category-specific budgets.
- Unit tests for recurring next-run calculations.
- ViewModel tests for budget create/edit/archive and recurring enable/disable.
- Compose UI tests for budget list and add budget validation.

## Acceptance Criteria

- Budget tab is a first-class screen matching the reference visual language.
- Budget progress numbers are correct and tested.
- Recurring transactions can be managed without corrupting normal transactions.
- Empty states guide users to create their first budget.

