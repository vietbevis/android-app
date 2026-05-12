alter table public.transactions
  add column if not exists location_lat double precision,
  add column if not exists location_lng double precision,
  add column if not exists location_label text;

create table if not exists public.user_preferences (
  user_id uuid primary key references auth.users(id) on delete cascade,
  language text not null default 'vi',
  theme_mode text not null default 'dark',
  currency text not null default 'VND',
  week_starts_on text not null default 'monday',
  default_wallet_id uuid references public.wallets(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint user_preferences_language_valid check (language in ('vi', 'en')),
  constraint user_preferences_theme_mode_valid check (theme_mode in ('dark', 'light', 'system')),
  constraint user_preferences_currency_not_blank check (length(trim(currency)) > 0),
  constraint user_preferences_week_starts_on_valid check (week_starts_on in ('monday', 'sunday'))
);

create table if not exists public.budgets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  amount numeric(14, 2) not null,
  cycle text not null,
  scope_type text not null default 'total',
  category_id uuid references public.categories(id),
  color text,
  icon text,
  starts_at date,
  is_archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint budgets_name_not_blank check (length(trim(name)) > 0),
  constraint budgets_amount_positive check (amount > 0),
  constraint budgets_cycle_valid check (cycle in ('daily', 'weekly', 'biweekly', 'monthly', 'yearly', 'custom')),
  constraint budgets_scope_type_valid check (scope_type in ('total', 'category')),
  constraint budgets_category_scope_valid check (
    (scope_type = 'total' and category_id is null)
    or (scope_type = 'category' and category_id is not null)
  )
);

create table if not exists public.recurring_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  wallet_id uuid not null references public.wallets(id),
  category_id uuid references public.categories(id),
  type text not null,
  amount numeric(14, 2) not null,
  frequency text not null,
  interval_count int not null default 1,
  day_of_month int,
  day_of_week int,
  next_run_at timestamptz not null,
  note text,
  is_enabled boolean not null default true,
  is_archived boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint recurring_transactions_type_valid check (type in ('income', 'expense')),
  constraint recurring_transactions_amount_positive check (amount > 0),
  constraint recurring_transactions_frequency_valid check (frequency in ('daily', 'weekly', 'monthly', 'yearly')),
  constraint recurring_transactions_interval_positive check (interval_count > 0),
  constraint recurring_transactions_day_of_month_valid check (day_of_month is null or day_of_month between 1 and 31),
  constraint recurring_transactions_day_of_week_valid check (day_of_week is null or day_of_week between 1 and 7)
);

create table if not exists public.transfers (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  from_wallet_id uuid not null references public.wallets(id),
  to_wallet_id uuid not null references public.wallets(id),
  amount numeric(14, 2) not null,
  fee numeric(14, 2) not null default 0,
  occurred_at timestamptz not null,
  note text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint transfers_amount_positive check (amount > 0),
  constraint transfers_fee_non_negative check (fee >= 0),
  constraint transfers_distinct_wallets check (from_wallet_id <> to_wallet_id)
);

create table if not exists public.loans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  type text not null,
  name text not null,
  counterparty_name text,
  principal numeric(14, 2) not null,
  interest_input_mode text not null default 'percent_per_year',
  interest_method text not null default 'simple',
  interest_value numeric(14, 2) not null default 0,
  repayment_method text not null default 'manual',
  start_at date not null,
  due_at date,
  status text not null default 'active',
  note text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint loans_type_valid check (type in ('borrowed', 'lent', 'installment')),
  constraint loans_name_not_blank check (length(trim(name)) > 0),
  constraint loans_principal_positive check (principal > 0),
  constraint loans_interest_value_non_negative check (interest_value >= 0),
  constraint loans_interest_input_mode_valid check (interest_input_mode in ('percent_per_year', 'fixed_amount')),
  constraint loans_interest_method_valid check (interest_method in ('simple', 'declining_balance', 'initial_balance', 'actual_days')),
  constraint loans_repayment_method_valid check (repayment_method in ('manual', 'monthly', 'custom')),
  constraint loans_status_valid check (status in ('active', 'paid', 'archived'))
);

create table if not exists public.investments (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  type text not null,
  principal numeric(14, 2) not null,
  current_value numeric(14, 2),
  note text,
  status text not null default 'active',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint investments_name_not_blank check (length(trim(name)) > 0),
  constraint investments_type_valid check (type in ('savings', 'stock', 'fund', 'crypto', 'gold', 'other')),
  constraint investments_principal_non_negative check (principal >= 0),
  constraint investments_current_value_non_negative check (current_value is null or current_value >= 0),
  constraint investments_status_valid check (status in ('active', 'closed', 'archived'))
);

create table if not exists public.friends (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  friend_user_id uuid not null references auth.users(id) on delete cascade,
  status text not null default 'pending',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint friends_status_valid check (status in ('pending', 'accepted', 'blocked')),
  constraint friends_not_self check (user_id <> friend_user_id)
);

create table if not exists public.groups (
  id uuid primary key default gen_random_uuid(),
  owner_user_id uuid not null references auth.users(id) on delete cascade,
  name text not null,
  icon text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint groups_name_not_blank check (length(trim(name)) > 0)
);

create table if not exists public.group_members (
  id uuid primary key default gen_random_uuid(),
  group_id uuid not null references public.groups(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  role text not null default 'member',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint group_members_role_valid check (role in ('owner', 'member'))
);

create table if not exists public.shared_transactions (
  id uuid primary key default gen_random_uuid(),
  transaction_id uuid not null references public.transactions(id) on delete cascade,
  shared_by_user_id uuid not null references auth.users(id) on delete cascade,
  shared_with_user_id uuid references auth.users(id) on delete cascade,
  group_id uuid references public.groups(id) on delete cascade,
  created_at timestamptz not null default now(),
  constraint shared_transactions_target_valid check (
    (shared_with_user_id is not null and group_id is null)
    or (shared_with_user_id is null and group_id is not null)
  )
);

create unique index if not exists friends_pair_idx on public.friends(user_id, friend_user_id);
create unique index if not exists group_members_group_user_idx on public.group_members(group_id, user_id);
create index if not exists budgets_user_id_archived_idx on public.budgets(user_id, is_archived);
create index if not exists recurring_transactions_user_id_next_run_idx on public.recurring_transactions(user_id, next_run_at) where is_enabled = true and is_archived = false;
create index if not exists transfers_user_id_occurred_at_idx on public.transfers(user_id, occurred_at desc);
create index if not exists loans_user_id_status_idx on public.loans(user_id, status);
create index if not exists investments_user_id_status_idx on public.investments(user_id, status);
create index if not exists shared_transactions_shared_with_user_idx on public.shared_transactions(shared_with_user_id);
create index if not exists shared_transactions_group_id_idx on public.shared_transactions(group_id);

create trigger set_user_preferences_updated_at
before update on public.user_preferences
for each row execute function public.set_updated_at();

create trigger set_budgets_updated_at
before update on public.budgets
for each row execute function public.set_updated_at();

create trigger set_recurring_transactions_updated_at
before update on public.recurring_transactions
for each row execute function public.set_updated_at();

create trigger set_transfers_updated_at
before update on public.transfers
for each row execute function public.set_updated_at();

create trigger set_loans_updated_at
before update on public.loans
for each row execute function public.set_updated_at();

create trigger set_investments_updated_at
before update on public.investments
for each row execute function public.set_updated_at();

create trigger set_friends_updated_at
before update on public.friends
for each row execute function public.set_updated_at();

create trigger set_groups_updated_at
before update on public.groups
for each row execute function public.set_updated_at();

create trigger set_group_members_updated_at
before update on public.group_members
for each row execute function public.set_updated_at();

alter table public.user_preferences enable row level security;
alter table public.budgets enable row level security;
alter table public.recurring_transactions enable row level security;
alter table public.transfers enable row level security;
alter table public.loans enable row level security;
alter table public.investments enable row level security;
alter table public.friends enable row level security;
alter table public.groups enable row level security;
alter table public.group_members enable row level security;
alter table public.shared_transactions enable row level security;

create policy "user_preferences_select_own" on public.user_preferences for select to authenticated using (user_id = auth.uid());
create policy "user_preferences_insert_own" on public.user_preferences for insert to authenticated with check (user_id = auth.uid());
create policy "user_preferences_update_own" on public.user_preferences for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy "budgets_select_own" on public.budgets for select to authenticated using (user_id = auth.uid());
create policy "budgets_insert_own" on public.budgets for insert to authenticated with check (user_id = auth.uid());
create policy "budgets_update_own" on public.budgets for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "budgets_delete_own" on public.budgets for delete to authenticated using (user_id = auth.uid());

create policy "recurring_transactions_select_own" on public.recurring_transactions for select to authenticated using (user_id = auth.uid());
create policy "recurring_transactions_insert_own" on public.recurring_transactions for insert to authenticated with check (user_id = auth.uid());
create policy "recurring_transactions_update_own" on public.recurring_transactions for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "recurring_transactions_delete_own" on public.recurring_transactions for delete to authenticated using (user_id = auth.uid());

create policy "transfers_select_own" on public.transfers for select to authenticated using (user_id = auth.uid());
create policy "transfers_insert_own" on public.transfers for insert to authenticated with check (user_id = auth.uid());
create policy "transfers_update_own" on public.transfers for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "transfers_delete_own" on public.transfers for delete to authenticated using (user_id = auth.uid());

create policy "loans_select_own" on public.loans for select to authenticated using (user_id = auth.uid());
create policy "loans_insert_own" on public.loans for insert to authenticated with check (user_id = auth.uid());
create policy "loans_update_own" on public.loans for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "loans_delete_own" on public.loans for delete to authenticated using (user_id = auth.uid());

create policy "investments_select_own" on public.investments for select to authenticated using (user_id = auth.uid());
create policy "investments_insert_own" on public.investments for insert to authenticated with check (user_id = auth.uid());
create policy "investments_update_own" on public.investments for update to authenticated using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy "investments_delete_own" on public.investments for delete to authenticated using (user_id = auth.uid());

create policy "friends_select_related" on public.friends for select to authenticated using (user_id = auth.uid() or friend_user_id = auth.uid());
create policy "friends_insert_own" on public.friends for insert to authenticated with check (user_id = auth.uid());
create policy "friends_update_related" on public.friends for update to authenticated using (user_id = auth.uid() or friend_user_id = auth.uid()) with check (user_id = auth.uid() or friend_user_id = auth.uid());
create policy "friends_delete_related" on public.friends for delete to authenticated using (user_id = auth.uid() or friend_user_id = auth.uid());

create policy "groups_select_member" on public.groups for select to authenticated using (
  owner_user_id = auth.uid()
  or exists (select 1 from public.group_members where group_members.group_id = groups.id and group_members.user_id = auth.uid())
);
create policy "groups_insert_own" on public.groups for insert to authenticated with check (owner_user_id = auth.uid());
create policy "groups_update_owner" on public.groups for update to authenticated using (owner_user_id = auth.uid()) with check (owner_user_id = auth.uid());
create policy "groups_delete_owner" on public.groups for delete to authenticated using (owner_user_id = auth.uid());

create policy "group_members_select_self" on public.group_members for select to authenticated using (user_id = auth.uid());
create policy "group_members_insert_owner" on public.group_members for insert to authenticated with check (
  exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = auth.uid())
);
create policy "group_members_update_owner" on public.group_members for update to authenticated using (
  exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = auth.uid())
) with check (
  exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = auth.uid())
);
create policy "group_members_delete_owner_or_self" on public.group_members for delete to authenticated using (
  user_id = auth.uid()
  or exists (select 1 from public.groups where groups.id = group_id and groups.owner_user_id = auth.uid())
);

create policy "shared_transactions_select_related" on public.shared_transactions for select to authenticated using (
  shared_by_user_id = auth.uid()
  or shared_with_user_id = auth.uid()
  or exists (select 1 from public.group_members where group_members.group_id = shared_transactions.group_id and group_members.user_id = auth.uid())
);
create policy "shared_transactions_insert_owner" on public.shared_transactions for insert to authenticated with check (
  shared_by_user_id = auth.uid()
  and exists (select 1 from public.transactions where transactions.id = transaction_id and transactions.user_id = auth.uid())
);
create policy "shared_transactions_delete_owner" on public.shared_transactions for delete to authenticated using (shared_by_user_id = auth.uid());
