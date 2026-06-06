-- Ensure 'transaction-photos' bucket is public
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('transaction-photos', 'transaction-photos', true, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
  public = excluded.public;

-- Drop old policies to avoid conflicts
drop policy if exists "transaction_photos_storage_select_own" on storage.objects;
drop policy if exists "Public Select" on storage.objects;
drop policy if exists "Allow public read access for transaction photos" on storage.objects;

-- Allow anyone to read transaction photos
create policy "Public Read Transaction Photos"
  on storage.objects for select
  using (bucket_id = 'transaction-photos');
