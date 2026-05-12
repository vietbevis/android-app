# Phase 03 - Supabase Backend Design

## Mục tiêu

Thiết kế backend Supabase cho MVP cá nhân: auth, database schema, private storage cho ảnh giao dịch và Row Level Security để mỗi user chỉ truy cập dữ liệu của chính mình.

## Supabase Services

Sử dụng:

- Supabase Auth: email/password trong MVP.
- Supabase Postgres: lưu profile, ví, danh mục, giao dịch.
- Supabase Storage: lưu ảnh giao dịch trong private bucket.
- Supabase client trên Android: gọi Auth, Postgrest, Storage trực tiếp với RLS.

Không dùng Edge Functions trong MVP trừ khi cần signed URL hoặc logic upload phức tạp. Nếu client SDK xử lý đủ, giữ kiến trúc đơn giản.

## Database Schema

Bảng `profiles`:

- `id uuid primary key references auth.users(id) on delete cascade`
- `display_name text`
- `currency text not null default 'VND'`
- `created_at timestamptz not null default now()`
- `updated_at timestamptz not null default now()`

Bảng `wallets`:

- `id uuid primary key default gen_random_uuid()`
- `user_id uuid not null references auth.users(id) on delete cascade`
- `name text not null`
- `type text not null`
- `initial_balance numeric not null default 0`
- `is_archived boolean not null default false`
- `created_at timestamptz not null default now()`
- `updated_at timestamptz not null default now()`

Bảng `categories`:

- `id uuid primary key default gen_random_uuid()`
- `user_id uuid references auth.users(id) on delete cascade`
- `name text not null`
- `transaction_type text not null`
- `icon text`
- `color text`
- `is_default boolean not null default false`
- `is_archived boolean not null default false`
- `created_at timestamptz not null default now()`

Bảng `transactions`:

- `id uuid primary key default gen_random_uuid()`
- `user_id uuid not null references auth.users(id) on delete cascade`
- `wallet_id uuid not null references wallets(id)`
- `category_id uuid references categories(id)`
- `type text not null`
- `amount numeric not null`
- `note text`
- `occurred_at timestamptz not null`
- `photo_path text`
- `created_at timestamptz not null default now()`
- `updated_at timestamptz not null default now()`

Bảng `transaction_photos` nếu cần metadata riêng:

- `id uuid primary key default gen_random_uuid()`
- `user_id uuid not null references auth.users(id) on delete cascade`
- `transaction_id uuid references transactions(id) on delete cascade`
- `storage_path text not null`
- `width int`
- `height int`
- `content_type text`
- `created_at timestamptz not null default now()`

MVP có thể dùng trực tiếp `transactions.photo_path`; chỉ tạo `transaction_photos` khi cần nhiều ảnh hoặc metadata rõ hơn.

## Constraints Và Indexes

- `transactions.amount > 0`.
- `transactions.type in ('income', 'expense')`.
- `categories.transaction_type in ('income', 'expense')`.
- Index `transactions(user_id, occurred_at desc)`.
- Index `transactions(user_id, category_id)`.
- Index `wallets(user_id, is_archived)`.
- Unique mềm cho wallet/category theo user nếu cần: `user_id, lower(name), transaction_type`.

## Row Level Security

Bật RLS cho tất cả bảng app.

Policy nguyên tắc:

- User chỉ select/insert/update/delete row có `user_id = auth.uid()`.
- `profiles.id = auth.uid()`.
- Default categories có thể được clone vào user khi onboarding. Không dùng shared global categories trong MVP để tránh policy phức tạp.

Storage:

- Bucket `transaction-photos` là private.
- Path format: `{user_id}/{transaction_id}/{timestamp}.jpg`.
- Policy storage chỉ cho phép user thao tác object có prefix bằng `auth.uid()`.

## Seed Và Onboarding Data

Khi user đăng ký lần đầu:

- Tạo `profiles`.
- Tạo wallet mặc định: `Tiền mặt`.
- Tạo categories mặc định cho income/expense.

Có thể thực hiện bằng:

- Client-side onboarding sau login lần đầu, dễ debug cho MVP.
- Database trigger sau auth signup, sạch hơn nhưng cần SQL kỹ hơn.

Mặc định phase này chọn client-side onboarding để giảm rủi ro triển khai ban đầu.

## Checklist

- [ ] Tạo Supabase project.
- [ ] Lưu `SUPABASE_URL` và `SUPABASE_ANON_KEY` theo cơ chế build config/local config, không commit secret nhạy cảm.
- [ ] Viết migration SQL tạo bảng, constraints và indexes.
- [ ] Bật RLS và viết policies.
- [ ] Tạo private bucket `transaction-photos`.
- [ ] Test bằng hai user khác nhau để xác nhận không đọc chéo dữ liệu.
- [ ] Ghi lại lệnh SQL migration vào tài liệu hoặc thư mục backend sau này.

## Tiêu Chí Hoàn Thành

- User đăng nhập có thể đọc/ghi dữ liệu của chính mình.
- User khác không đọc được transaction, wallet, category hoặc ảnh.
- App có thể upload ảnh vào đúng path và lấy lại ảnh bằng cơ chế hợp lệ.

