create index if not exists budgets_category_id_idx on public.budgets(category_id);
create index if not exists recurring_transactions_wallet_id_idx on public.recurring_transactions(wallet_id);
create index if not exists recurring_transactions_category_id_idx on public.recurring_transactions(category_id);
create index if not exists transfers_from_wallet_id_idx on public.transfers(from_wallet_id);
create index if not exists transfers_to_wallet_id_idx on public.transfers(to_wallet_id);
create index if not exists user_preferences_default_wallet_id_idx on public.user_preferences(default_wallet_id);
create index if not exists friends_friend_user_id_idx on public.friends(friend_user_id);
create index if not exists groups_owner_user_id_idx on public.groups(owner_user_id);
create index if not exists group_members_user_id_idx on public.group_members(user_id);
create index if not exists shared_transactions_transaction_id_idx on public.shared_transactions(transaction_id);
create index if not exists shared_transactions_shared_by_user_id_idx on public.shared_transactions(shared_by_user_id);

drop policy if exists "user_preferences_select_own" on public.user_preferences;
drop policy if exists "user_preferences_insert_own" on public.user_preferences;
drop policy if exists "user_preferences_update_own" on public.user_preferences;
create policy "user_preferences_select_own" on public.user_preferences for select to authenticated using (user_id = (select auth.uid()));
create policy "user_preferences_insert_own" on public.user_preferences for insert to authenticated with check (user_id = (select auth.uid()));
create policy "user_preferences_update_own" on public.user_preferences for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));

drop policy if exists "budgets_select_own" on public.budgets;
drop policy if exists "budgets_insert_own" on public.budgets;
drop policy if exists "budgets_update_own" on public.budgets;
drop policy if exists "budgets_delete_own" on public.budgets;
create policy "budgets_select_own" on public.budgets for select to authenticated using (user_id = (select auth.uid()));
create policy "budgets_insert_own" on public.budgets for insert to authenticated with check (user_id = (select auth.uid()));
create policy "budgets_update_own" on public.budgets for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "budgets_delete_own" on public.budgets for delete to authenticated using (user_id = (select auth.uid()));

drop policy if exists "recurring_transactions_select_own" on public.recurring_transactions;
drop policy if exists "recurring_transactions_insert_own" on public.recurring_transactions;
drop policy if exists "recurring_transactions_update_own" on public.recurring_transactions;
drop policy if exists "recurring_transactions_delete_own" on public.recurring_transactions;
create policy "recurring_transactions_select_own" on public.recurring_transactions for select to authenticated using (user_id = (select auth.uid()));
create policy "recurring_transactions_insert_own" on public.recurring_transactions for insert to authenticated with check (user_id = (select auth.uid()));
create policy "recurring_transactions_update_own" on public.recurring_transactions for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "recurring_transactions_delete_own" on public.recurring_transactions for delete to authenticated using (user_id = (select auth.uid()));

drop policy if exists "transfers_select_own" on public.transfers;
drop policy if exists "transfers_insert_own" on public.transfers;
drop policy if exists "transfers_update_own" on public.transfers;
drop policy if exists "transfers_delete_own" on public.transfers;
create policy "transfers_select_own" on public.transfers for select to authenticated using (user_id = (select auth.uid()));
create policy "transfers_insert_own" on public.transfers for insert to authenticated with check (user_id = (select auth.uid()));
create policy "transfers_update_own" on public.transfers for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "transfers_delete_own" on public.transfers for delete to authenticated using (user_id = (select auth.uid()));

drop policy if exists "loans_select_own" on public.loans;
drop policy if exists "loans_insert_own" on public.loans;
drop policy if exists "loans_update_own" on public.loans;
drop policy if exists "loans_delete_own" on public.loans;
create policy "loans_select_own" on public.loans for select to authenticated using (user_id = (select auth.uid()));
create policy "loans_insert_own" on public.loans for insert to authenticated with check (user_id = (select auth.uid()));
create policy "loans_update_own" on public.loans for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "loans_delete_own" on public.loans for delete to authenticated using (user_id = (select auth.uid()));

drop policy if exists "investments_select_own" on public.investments;
drop policy if exists "investments_insert_own" on public.investments;
drop policy if exists "investments_update_own" on public.investments;
drop policy if exists "investments_delete_own" on public.investments;
create policy "investments_select_own" on public.investments for select to authenticated using (user_id = (select auth.uid()));
create policy "investments_insert_own" on public.investments for insert to authenticated with check (user_id = (select auth.uid()));
create policy "investments_update_own" on public.investments for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "investments_delete_own" on public.investments for delete to authenticated using (user_id = (select auth.uid()));

drop policy if exists "friends_select_related" on public.friends;
drop policy if exists "friends_insert_own" on public.friends;
drop policy if exists "friends_update_related" on public.friends;
drop policy if exists "friends_delete_related" on public.friends;
create policy "friends_select_related" on public.friends for select to authenticated using (user_id = (select auth.uid()) or friend_user_id = (select auth.uid()));
create policy "friends_insert_own" on public.friends for insert to authenticated with check (user_id = (select auth.uid()));
create policy "friends_update_related" on public.friends for update to authenticated using (user_id = (select auth.uid()) or friend_user_id = (select auth.uid())) with check (user_id = (select auth.uid()) or friend_user_id = (select auth.uid()));
create policy "friends_delete_related" on public.friends for delete to authenticated using (user_id = (select auth.uid()) or friend_user_id = (select auth.uid()));

drop policy if exists "groups_select_member" on public.groups;
drop policy if exists "groups_insert_own" on public.groups;
drop policy if exists "groups_update_owner" on public.groups;
drop policy if exists "groups_delete_owner" on public.groups;
create policy "groups_select_member" on public.groups for select to authenticated using (
  owner_user_id = (select auth.uid())
  or exists (select 1 from public.group_members where group_members.group_id = groups.id and group_members.user_id = (select auth.uid()))
);
create policy "groups_insert_own" on public.groups for insert to authenticated with check (owner_user_id = (select auth.uid()));
create policy "groups_update_owner" on public.groups for update to authenticated using (owner_user_id = (select auth.uid())) with check (owner_user_id = (select auth.uid()));
create policy "groups_delete_owner" on public.groups for delete to authenticated using (owner_user_id = (select auth.uid()));

drop policy if exists "group_members_select_self" on public.group_members;
drop policy if exists "group_members_insert_owner" on public.group_members;
drop policy if exists "group_members_update_owner" on public.group_members;
drop policy if exists "group_members_delete_owner_or_self" on public.group_members;
create policy "group_members_select_self" on public.group_members for select to authenticated using (user_id = (select auth.uid()));
create policy "group_members_insert_owner" on public.group_members for insert to authenticated with check (
  exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = (select auth.uid()))
);
create policy "group_members_update_owner" on public.group_members for update to authenticated using (
  exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = (select auth.uid()))
) with check (
  exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = (select auth.uid()))
);
create policy "group_members_delete_owner_or_self" on public.group_members for delete to authenticated using (
  user_id = (select auth.uid())
  or exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = (select auth.uid()))
);

drop policy if exists "shared_transactions_select_related" on public.shared_transactions;
drop policy if exists "shared_transactions_insert_owner" on public.shared_transactions;
drop policy if exists "shared_transactions_delete_owner" on public.shared_transactions;
create policy "shared_transactions_select_related" on public.shared_transactions for select to authenticated using (
  shared_by_user_id = (select auth.uid())
  or shared_with_user_id = (select auth.uid())
  or exists (select 1 from public.group_members where group_members.group_id = shared_transactions.group_id and group_members.user_id = (select auth.uid()))
);
create policy "shared_transactions_insert_owner" on public.shared_transactions for insert to authenticated with check (
  shared_by_user_id = (select auth.uid())
  and exists (select 1 from public.transactions where transactions.id = transaction_id and transactions.user_id = (select auth.uid()))
);
create policy "shared_transactions_delete_owner" on public.shared_transactions for delete to authenticated using (shared_by_user_id = (select auth.uid()));
