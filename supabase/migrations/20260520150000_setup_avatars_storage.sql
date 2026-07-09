-- 1. Create a new public bucket for avatars
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('avatars', 'avatars', true, 2097152, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

-- 2. Allow users to upload their own avatar
create policy "Avatar upload policy" on storage.objects
  for insert to authenticated
  with check (bucket_id = 'avatars' and (storage.foldername(name))[1] = auth.uid()::text);

-- 3. Allow users to update their own avatar
create policy "Avatar update policy" on storage.objects
  for update to authenticated
  using (bucket_id = 'avatars' and (storage.foldername(name))[1] = auth.uid()::text);

-- 4. Allow public to view avatars (since the bucket is public, we just need a select policy)
create policy "Avatar view policy" on storage.objects
  for select to public
  using (bucket_id = 'avatars');
