-- Drop unused tables that were part of future feature planning
drop table if exists public.shared_transactions;
drop table if exists public.group_members;
drop table if exists public.groups;
drop table if exists public.friends;
drop table if exists public.investments;
drop table if exists public.loans;
drop table if exists public.transfers;
drop table if exists public.recurring_transactions;

-- Note: The related triggers and indexes are automatically removed when the tables are dropped.
