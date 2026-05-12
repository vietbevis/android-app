# Phase 06 - Statistics And Insights

## Goal

Build the new `Thống kê` tab with visual summaries, period filters, category analysis, and real transaction-location data states.

## Current State

- Implemented against real Supabase transactions, wallets, and categories.
- Monthly summary cards, category ranking, and location-aware data section are live.
- Location fields are persisted on transactions as nullable data; the UI reports real coverage instead of showing fake map data.

## Target UX

- Top segmented control supports `Tháng`, `Năm`, and `Tất cả`.
- Period navigation uses left/right buttons and a centered period title.
- Wallet chips filter all stats.
- Cards show income, expense, and balance with clear signs and colors.
- Category tab shows chart/list analysis from real categories.
- Map/data tab shows transaction locations when coordinates exist, or a truthful empty state with the count of real transactions missing location.

## Implementation Tasks

- Create `StatisticsScreen` and `StatisticsViewModel`.
- Generalize `FinanceSummaryCalculator` to support month, year, and all-time ranges.
- Add chart data model: category name, amount, percent, color, icon.
- Implement donut chart in Compose Canvas or a proven chart library if accepted by project constraints.
- Add category detail drill-down route or filtered transaction list.
- Use transaction `latitude`, `longitude`, and `locationLabel` for location summaries.
- Add insight copy such as largest category, spending change from previous period, and no-data nudges.

## Data/API Changes

- Existing transaction/category/wallet data supports category statistics.
- Transaction DTO/domain includes nullable location fields from Supabase.
- Repository exposes date-range transaction queries and wallet/category lookup.

## Edge Cases

- Zero income/expense must not break percent calculations or chart rendering.
- Unknown or archived category must still appear in historical totals.
- Large transaction lists may require server-side aggregation later; initial implementation aggregates client-side with documented limits.
- Transfers should be excluded from income/expense unless product explicitly adds transfer reporting.

## Tests

- Unit tests for period range generation and category percent calculations.
- Unit tests for zero-data and archived category aggregation.
- ViewModel tests for period switch, previous/next navigation, wallet filter, and location empty state.
- Compose UI test for chart/list rendering with real shaped data.

## Acceptance Criteria

- Statistics tab provides clear month/year/all-time analysis.
- Category chart and list agree numerically.
- Location states are truthful and powered by transaction fields.
- Existing dashboard aggregate tests still pass or are migrated to the new calculator.
