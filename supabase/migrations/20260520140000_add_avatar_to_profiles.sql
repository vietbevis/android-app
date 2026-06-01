-- Add avatar column to profiles table
alter table public.profiles
add column avatar text;

-- Note: No changes to RLS policies are needed as they already cover all columns in the profiles table.
