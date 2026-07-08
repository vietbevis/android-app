-- Drop unused tables with CASCADE to handle dependent policies and foreign keys
-- These were part of future feature planning but are not currently used.
drop table if exists public.shared_transactions cascade;
drop table if exists public.group_members cascade;
drop table if exists public.groups cascade;
drop table if exists public.friends cascade;
drop table if exists public.investments cascade;
drop table if exists public.loans cascade;
drop table if exists public.transfers cascade;
drop table if exists public.recurring_transactions cascade;

-- Note: CASCADE will also remove dependent RLS policies, triggers, and foreign keys.
