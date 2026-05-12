# Phase 07 - Accounts Transfers Loans Investments

## Goal

Build the new `Tài khoản` tab with account balances, transfers, loan creation, and investment tracking backed by real Supabase tables.

## Current State

- `AccountsScreen` and `AccountsViewModel` are implemented.
- Wallet balances include real transactions and transfer effects.
- Transfers, loans, and investments use real repositories and Supabase tables.
- Wallet/category legacy screens remain for compatibility but the primary UX is the new account tab.

## Target UX

- Top segmented control supports `Tài khoản`, `Khoản vay`, and `Đầu tư`.
- Accounts view shows total balance, income/expense snippets, transfer actions, transfer history, and wallet cards.
- Loans view supports adding loan records with title, counterparty, principal, interest mode, and archive action.
- Investments view supports manually entered investment records and archive action.

## Implementation Tasks

- Create `AccountsScreen` and `AccountsViewModel`.
- Move wallet/account summary into the new `Tài khoản` tab.
- Implement transfers:
  - Create transfer between two active wallets.
  - Store transfer record in Supabase.
  - Adjust balance calculations without counting transfer as income/expense.
  - Show transfer history.
- Implement loan creation form:
  - Loan title and counterparty.
  - Principal amount.
  - Interest rate or amount.
  - Interest method field.
  - Archive action.
- Implement investment create/list/archive using real `investments` rows.

## Data/API Changes

- Uses `transfers`, `loans`, and `investments` from Phase 03.
- Balance calculation includes:
  - Wallet initial balance.
  - Income transactions.
  - Expense transactions.
  - Incoming/outgoing transfers.
- Repository APIs separate wallets, transfers, loans, and investments.

## Edge Cases

- Transfer from and to the same wallet is invalid.
- Archived wallets cannot be selected for new transfers but remain visible in history.
- Loan interest calculations must be deterministic and documented.
- Investment values are manually entered; the app must not imply live market sync.

## Tests

- Unit tests for transfer balance effects.
- Unit tests for simple loan interest and reducing-balance loan calculations selected for v1.
- ViewModel tests for create account, create transfer, create loan, archive wallet, and invalid selections.
- Compose UI tests for account list, transfer flow, and add loan form validation.

## Acceptance Criteria

- Users can understand and manage balances from the Accounts tab.
- Transfers are recorded without polluting income/expense stats.
- Loan creation is usable and validates required fields.
- Investments are functional with real create/list/archive paths.
