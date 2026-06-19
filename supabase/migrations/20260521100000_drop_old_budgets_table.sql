-- Migration to drop the old budgets table as we transitioned to monthly_budgets

drop table if exists public.budgets cascade;
