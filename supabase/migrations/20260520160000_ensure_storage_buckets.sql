-- Ensure 'transaction-photos' bucket exists
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('transaction-photos', 'transaction-photos', false, 5242880, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

-- Re-apply policies for 'transaction-photos' (Safe to run multiple times)
drop policy if exists "transaction_photos_storage_select_own" on storage.objects;
create policy "transaction_photos_storage_select_own" on storage.objects for select to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);

drop policy if exists "transaction_photos_storage_insert_own" on storage.objects;
create policy "transaction_photos_storage_insert_own" on storage.objects for insert to authenticated with check (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);

drop policy if exists "transaction_photos_storage_update_own" on storage.objects;
create policy "transaction_photos_storage_update_own" on storage.objects for update to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
) with check (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);

drop policy if exists "transaction_photos_storage_delete_own" on storage.objects;
create policy "transaction_photos_storage_delete_own" on storage.objects for delete to authenticated using (
  bucket_id = 'transaction-photos' and (storage.foldername(name))[1] = auth.uid()::text
);
