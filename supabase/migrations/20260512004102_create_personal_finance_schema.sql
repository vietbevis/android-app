create extension if not exists pgcrypto with schema extensions;

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text,
  currency text not null default 'VND',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint profiles_currency_not_blank check (length(trim(currency)) > 0)
);

create table public.wallets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  type text not null default 'cash',
  initial_balance numeric(14, 2) not null default 0,
  is_archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint wallets_name_not_blank check (length(trim(name)) > 0),
  constraint wallets_type_valid check (type in ('cash', 'bank', 'e_wallet', 'other'))
);

create table public.categories (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  transaction_type text not null,
  icon text,
  color text,
  is_default boolean not null default false,
  is_archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint categories_name_not_blank check (length(trim(name)) > 0),
  constraint categories_transaction_type_valid check (transaction_type in ('income', 'expense'))
);

create table public.transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  wallet_id uuid not null references public.wallets(id),
  category_id uuid references public.categories(id),
  type text not null,
  amount numeric(14, 2) not null,
  note text,
  occurred_at timestamptz not null,
  photo_path text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint transactions_type_valid check (type in ('income', 'expense')),
  constraint transactions_amount_positive check (amount > 0)
);

create table public.transaction_photos (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  transaction_id uuid references public.transactions(id) on delete cascade,
  storage_path text not null,
  width int,
  height int,
  content_type text,
  created_at timestamptz not null default now(),
  constraint transaction_photos_storage_path_not_blank check (length(trim(storage_path)) > 0),
  constraint transaction_photos_width_positive check (width is null or width > 0),
  constraint transaction_photos_height_positive check (height is null or height > 0)
);

create index wallets_user_id_archived_idx on public.wallets(user_id, is_archived);
create index categories_user_id_type_archived_idx on public.categories(user_id, transaction_type, is_archived);
create index transactions_user_id_occurred_at_idx on public.transactions(user_id, occurred_at desc);
create index transactions_user_id_category_id_idx on public.transactions(user_id, category_id);
create index transactions_user_id_wallet_id_idx on public.transactions(user_id, wallet_id);
create index transaction_photos_user_id_transaction_id_idx on public.transaction_photos(user_id, transaction_id);

create unique index wallets_user_name_active_idx
  on public.wallets(user_id, lower(name))
  where is_archived = false;

create unique index categories_user_name_type_active_idx
  on public.categories(user_id, lower(name), transaction_type)
  where is_archived = false;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
security invoker
set search_path = public
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger set_profiles_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

create trigger set_wallets_updated_at
before update on public.wallets
for each row execute function public.set_updated_at();

create trigger set_categories_updated_at
before update on public.categories
for each row execute function public.set_updated_at();

create trigger set_transactions_updated_at
before update on public.transactions
for each row execute function public.set_updated_at();

alter table public.profiles enable row level security;
alter table public.wallets enable row level security;
alter table public.categories enable row level security;
alter table public.transactions enable row level security;
alter table public.transaction_photos enable row level security;

create policy "profiles_select_own" on public.profiles for select to authenticated using (id = auth.uid());
create policy "profiles_insert_own" on public.profiles for insert to authenticated with check (id = auth.uid());
create policy "profiles_update_own" on public.profiles for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

create policy "wallets_select_own" on public.wallets for select to authenticated using (user_id = auth.uid());
create policy "wallets_insert_own" on public.wallets for insert to authenticated with check (user_id = auth.uid());
create policy "wallets_update_own" on public.wallets for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "wallets_delete_own" on public.wallets for delete to authenticated using (user_id = auth.uid());

create policy "categories_select_own" on public.categories for select to authenticated using (user_id = auth.uid());
create policy "categories_insert_own" on public.categories for insert to authenticated with check (user_id = auth.uid());
create policy "categories_update_own" on public.categories for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "categories_delete_own" on public.categories for delete to authenticated using (user_id = auth.uid());

create policy "transactions_select_own" on public.transactions for select to authenticated using (user_id = auth.uid());
create policy "transactions_insert_own" on public.transactions for insert to authenticated with check (
  user_id = auth.uid()
  and exists (select 1 from public.wallets where wallets.id = wallet_id and wallets.user_id = auth.uid())
  and (category_id is null or exists (
    select 1 from public.categories
    where categories.id = category_id and categories.user_id = auth.uid() and categories.transaction_type = type
  ))
);
create policy "transactions_update_own" on public.transactions for update to authenticated using (user_id = auth.uid()) with check (
  user_id = auth.uid()
  and exists (select 1 from public.wallets where wallets.id = wallet_id and wallets.user_id = auth.uid())
  and (category_id is null or exists (
    select 1 from public.categories
    where categories.id = category_id and categories.user_id = auth.uid() and categories.transaction_type = type
  ))
);
create policy "transactions_delete_own" on public.transactions for delete to authenticated using (user_id = auth.uid());

create policy "transaction_photos_select_own" on public.transaction_photos for select to authenticated using (user_id = auth.uid());
create policy "transaction_photos_insert_own" on public.transaction_photos for insert to authenticated with check (
  user_id = auth.uid()
  and (transaction_id is null or exists (
    select 1 from public.transactions where transactions.id = transaction_id and transactions.user_id = auth.uid()
  ))
);
create policy "transaction_photos_update_own" on public.transaction_photos for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "transaction_photos_delete_own" on public.transaction_photos for delete to authenticated using (user_id = auth.uid());

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('transaction-photos', 'transaction-photos', false, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

create policy "transaction_photos_storage_select_own" on storage.objects for select to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);
create policy "transaction_photos_storage_insert_own" on storage.objects for insert to authenticated with check (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);
create policy "transaction_photos_storage_update_own" on storage.objects for update to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
) with check (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);
create policy "transaction_photos_storage_delete_own" on storage.objects for delete to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);
