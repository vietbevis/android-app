# CapMoney Release Checklist

## Build Evidence

- [x] `.\gradlew.bat test`
- [x] `.\gradlew.bat assembleDebug`
- [x] `.\gradlew.bat assembleRelease`

Production release still needs a real signing keystore before public distribution.

## UI Migration Evidence

- [x] Dark CapMoney design tokens and typography.
- [x] Bottom navigation: `Trang chủ`, `Thống kê`, `Tài khoản`, `Ngân sách`, `Cá nhân`.
- [x] Capture moved out of bottom nav and opened by FAB/modal.
- [x] Home calendar/feed uses real wallet, category, and transaction data.
- [x] Capture amount entry uses calculator keypad with unit tests.
- [x] Statistics tab uses real monthly aggregates and category progress.
- [x] Accounts tab shows wallet balances and total balance.
- [x] Budgets tab has tested progress math and preview UI.
- [x] Profile tab has overview cards and settings menu.
- [x] Supabase production project migrated for budgets, recurring transactions, transfers, loans, investments, preferences, friends, groups, and shared transactions.
- [x] Budget tab reads/writes real Supabase `budgets` rows.
- [x] Accounts tab reads real Supabase `transfers`, `loans`, and `investments` rows.

## Supabase Notes

- Supabase CLI is installed at `C:\Users\admin\scoop\shims\supabase.exe`.
- App runtime uses `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` from `local.properties`.
- Remote project `fosayyusmhfnogjwzttw` has migrations:
  - `20260512023844_expand_capmoney_feature_schema`
  - `20260512024146_optimize_capmoney_feature_schema`
- Security advisor only reports Auth leaked password protection disabled, which is a dashboard Auth setting.
- Performance advisor only reports unused indexes on newly created/low-traffic tables; FK and RLS initplan issues were fixed.
- Still verify RLS manually with two accounts before public release.

## Manual Device Tests

- Auth: sign up, sign in, sign out, sign in again.
- Onboarding: new user receives default wallet and categories.
- Navigation: all five tabs render without clipped Vietnamese text.
- Home: wallet filters, calendar day selection, and day feed update.
- Camera: deny permission, grant permission, capture, preview, retake.
- Capture keypad: digits, `000`, `C`, backspace, operators, equals, save.
- Save: save expense with photo, save income without photo, retry after upload failure.
- Statistics: totals and category progress match transaction data.
- Accounts: wallet balances match initial balance plus monthly transactions.
- Budgets: preview card layout fits small screens.
- Profile: overview values update and sign-out works.
- Security: user A cannot see user B data; storage paths remain under `{user_id}/...`.

## Backlog After Current Migration Slice

- Apply and verify the new Supabase migration.
- Implement Supabase DTOs/repositories for budgets, transfers, loans, investments, preferences, recurring transactions, and sharing.
- Add transaction detail/edit screen.
- Add gallery import and date picker to capture.
- Add real donut chart and optional map location reporting.
- Add offline read cache and durable upload retry queue.
