# Phase 09 - Profile Settings And Social

## Goal

Build the new `Cá nhân` tab with profile overview, real preferences, recurring transactions, and social/shared transaction workflows.

## Current State

- `ProfileScreen` and `ProfileViewModel` are implemented.
- Monthly profile overview reads real transactions.
- Preferences are read and updated in `user_preferences`.
- Recurring transactions are created/listed/archived in `recurring_transactions`.
- Friends, groups, and shared transactions use real Supabase tables through `SharingRepository`.

## Target UX

- Profile card shows avatar/name/month context.
- Overview cards show transaction count, income, expense, and balance.
- Settings rows cover language, theme, currency, week start, categories, feedback, and share app.
- Recurring transaction management supports create/list/archive.
- Social section supports friend request by user id, group creation, and sharing a real transaction to a selected group.

## Implementation Tasks

- Create `ProfileScreen` and `ProfileViewModel`.
- Add preference controls:
  - Language.
  - Theme mode.
  - Currency display.
  - Week start.
- Add recurring transaction controls:
  - Wallet/category chips.
  - Amount/note input.
  - Monthly schedule creation.
  - Archive action.
- Add social workflows:
  - Create friend request by target user id.
  - Create group with owner membership.
  - List real friends/groups/shared transactions.
  - Share latest real transaction to a selected group.
- Keep premium/Apple sign-in out of the working UI until auth/product scope is ready.

## Data/API Changes

- Uses `user_preferences`, `recurring_transactions`, `friends`, `groups`, `group_members`, and `shared_transactions`.
- All tables keep RLS scoped to `auth.uid()` or group membership.
- App never uses service role keys.

## Edge Cases

- Empty profile data must still render cleanly.
- Preferences should upsert defaults if no row exists yet.
- Friend request cannot target the current user id.
- Group sharing requires both a real transaction and a real group.
- Shared transaction delete should only remove rows owned by the current user.

## Tests

- Unit tests for preference upsert defaults.
- ViewModel tests for recurring create/archive and social validation.
- Compose UI tests for profile settings, recurring form, and social form.
- Manual RLS test with two users for friends/groups/shared transactions.

## Acceptance Criteria

- Profile tab uses real user and finance data.
- Preference changes persist to Supabase.
- Recurring transactions persist to Supabase.
- Social/shared workflows use real tables and truthful empty states.
