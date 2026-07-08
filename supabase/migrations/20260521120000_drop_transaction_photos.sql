-- Drop transaction_photos table as it is currently unused metadata
-- MVP uses transactions.photo_path directly with Supabase Storage.
drop table if exists public.transaction_photos cascade;
