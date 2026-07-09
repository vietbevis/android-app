-- Migration to add monthly and category-specific budgets

create table if not exists public.monthly_budgets (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references auth.users(id) on delete cascade not null,
  amount numeric(15, 2) not null, -- Hạn mức tổng của cả tháng
  budget_month date not null,     -- Lưu ngày mùng 1 của tháng đó (Ví dụ: 2026-06-01)
  created_at timestamp with time zone default now() not null,
  -- Đảm bảo mỗi user chỉ có 1 hạn mức tổng duy nhất cho 1 tháng cụ thể
  unique (user_id, budget_month)
);

create table if not exists public.category_budgets (
  id uuid default gen_random_uuid() primary key,
  monthly_budget_id uuid references public.monthly_budgets(id) on delete cascade not null,
  category_id uuid references public.categories(id) on delete cascade not null,
  amount numeric(15, 2) not null, -- Hạn mức riêng cho danh mục này trong tháng đó
  created_at timestamp with time zone default now() not null,
  -- Đảm bảo một danh mục chỉ xuất hiện 1 lần trong 1 tháng ngân sách
  unique (monthly_budget_id, category_id)
);

-- Enable Row Level Security
alter table public.monthly_budgets enable row level security;
alter table public.category_budgets enable row level security;

-- RLS Policies for monthly_budgets
create policy "monthly_budgets_select_own" on public.monthly_budgets
  for select to authenticated
  using (user_id = auth.uid());

create policy "monthly_budgets_insert_own" on public.monthly_budgets
  for insert to authenticated
  with check (user_id = auth.uid());

create policy "monthly_budgets_update_own" on public.monthly_budgets
  for update to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

create policy "monthly_budgets_delete_own" on public.monthly_budgets
  for delete to authenticated
  using (user_id = auth.uid());

-- RLS Policies for category_budgets
create policy "category_budgets_select_own" on public.category_budgets
  for select to authenticated
  using (
    exists (
      select 1 from public.monthly_budgets
      where monthly_budgets.id = monthly_budget_id
      and monthly_budgets.user_id = auth.uid()
    )
  );

create policy "category_budgets_insert_own" on public.category_budgets
  for insert to authenticated
  with check (
    exists (
      select 1 from public.monthly_budgets
      where monthly_budgets.id = monthly_budget_id
      and monthly_budgets.user_id = auth.uid()
    )
  );

create policy "category_budgets_update_own" on public.category_budgets
  for update to authenticated
  using (
    exists (
      select 1 from public.monthly_budgets
      where monthly_budgets.id = monthly_budget_id
      and monthly_budgets.user_id = auth.uid()
    )
  )
  with check (
    exists (
      select 1 from public.monthly_budgets
      where monthly_budgets.id = monthly_budget_id
      and monthly_budgets.user_id = auth.uid()
    )
  );

create policy "category_budgets_delete_own" on public.category_budgets
  for delete to authenticated
  using (
    exists (
      select 1 from public.monthly_budgets
      where monthly_budgets.id = monthly_budget_id
      and monthly_budgets.user_id = auth.uid()
    )
  );

-- Indexes for performance
create index if not exists monthly_budgets_user_month_idx on public.monthly_budgets(user_id, budget_month);
create index if not exists category_budgets_monthly_id_idx on public.category_budgets(monthly_budget_id);
