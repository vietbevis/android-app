create index transactions_wallet_id_idx on public.transactions(wallet_id);
create index transactions_category_id_idx on public.transactions(category_id);
create index transaction_photos_transaction_id_idx on public.transaction_photos(transaction_id);

drop policy if exists "profiles_select_own" on public.profiles;
drop policy if exists "profiles_insert_own" on public.profiles;
drop policy if exists "profiles_update_own" on public.profiles;
drop policy if exists "wallets_select_own" on public.wallets;
drop policy if exists "wallets_insert_own" on public.wallets;
drop policy if exists "wallets_update_own" on public.wallets;
drop policy if exists "wallets_delete_own" on public.wallets;
drop policy if exists "categories_select_own" on public.categories;
drop policy if exists "categories_insert_own" on public.categories;
drop policy if exists "categories_update_own" on public.categories;
drop policy if exists "categories_delete_own" on public.categories;
drop policy if exists "transactions_select_own" on public.transactions;
drop policy if exists "transactions_insert_own" on public.transactions;
drop policy if exists "transactions_update_own" on public.transactions;
drop policy if exists "transactions_delete_own" on public.transactions;
drop policy if exists "transaction_photos_select_own" on public.transaction_photos;
drop policy if exists "transaction_photos_insert_own" on public.transaction_photos;
drop policy if exists "transaction_photos_update_own" on public.transaction_photos;
drop policy if exists "transaction_photos_delete_own" on public.transaction_photos;
drop policy if exists "transaction_photos_storage_select_own" on storage.objects;
drop policy if exists "transaction_photos_storage_insert_own" on storage.objects;
drop policy if exists "transaction_photos_storage_update_own" on storage.objects;
drop policy if exists "transaction_photos_storage_delete_own" on storage.objects;

create policy "profiles_select_own" on public.profiles for select to authenticated using (id = (select auth.uid()));
create policy "profiles_insert_own" on public.profiles for insert to authenticated with check (id = (select auth.uid()));
create policy "profiles_update_own" on public.profiles for update to authenticated using (id = (select auth.uid())) with check (id = (select auth.uid()));

create policy "wallets_select_own" on public.wallets for select to authenticated using (user_id = (select auth.uid()));
create policy "wallets_insert_own" on public.wallets for insert to authenticated with check (user_id = (select auth.uid()));
create policy "wallets_update_own" on public.wallets for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "wallets_delete_own" on public.wallets for delete to authenticated using (user_id = (select auth.uid()));

create policy "categories_select_own" on public.categories for select to authenticated using (user_id = (select auth.uid()));
create policy "categories_insert_own" on public.categories for insert to authenticated with check (user_id = (select auth.uid()));
create policy "categories_update_own" on public.categories for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "categories_delete_own" on public.categories for delete to authenticated using (user_id = (select auth.uid()));

create policy "transactions_select_own" on public.transactions for select to authenticated using (user_id = (select auth.uid()));
create policy "transactions_insert_own" on public.transactions for insert to authenticated with check (
  user_id = (select auth.uid())
  and exists (select 1 from public.wallets where wallets.id = wallet_id and wallets.user_id = (select auth.uid()))
  and (category_id is null or exists (
    select 1 from public.categories
    where categories.id = category_id and categories.user_id = (select auth.uid()) and categories.transaction_type = type
  ))
);
create policy "transactions_update_own" on public.transactions for update to authenticated using (user_id = (select auth.uid())) with check (
  user_id = (select auth.uid())
  and exists (select 1 from public.wallets where wallets.id = wallet_id and wallets.user_id = (select auth.uid()))
  and (category_id is null or exists (
    select 1 from public.categories
    where categories.id = category_id and categories.user_id = (select auth.uid()) and categories.transaction_type = type
  ))
);
create policy "transactions_delete_own" on public.transactions for delete to authenticated using (user_id = (select auth.uid()));

create policy "transaction_photos_select_own" on public.transaction_photos for select to authenticated using (user_id = (select auth.uid()));
create policy "transaction_photos_insert_own" on public.transaction_photos for insert to authenticated with check (
  user_id = (select auth.uid())
  and (transaction_id is null or exists (
    select 1 from public.transactions where transactions.id = transaction_id and transactions.user_id = (select auth.uid())
  ))
);
create policy "transaction_photos_update_own" on public.transaction_photos for update to authenticated using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy "transaction_photos_delete_own" on public.transaction_photos for delete to authenticated using (user_id = (select auth.uid()));

create policy "transaction_photos_storage_select_own" on storage.objects for select to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = (select auth.uid())::text
);
create policy "transaction_photos_storage_insert_own" on storage.objects for insert to authenticated with check (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = (select auth.uid())::text
);
create policy "transaction_photos_storage_update_own" on storage.objects for update to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = (select auth.uid())::text
) with check (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = (select auth.uid())::text
);
create policy "transaction_photos_storage_delete_own" on storage.objects for delete to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = (select auth.uid())::text
);
