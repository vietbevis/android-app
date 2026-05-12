# Session Handoff - 2026-05-12

## Mục tiêu Đã Nhận

Triển khai lần lượt các phase cho app quản lý tài chính cá nhân camera-first, dùng Kotlin, Jetpack Compose, XML resources và Supabase backend. Supabase MCP đã được kết nối và dùng project thật.

## Phase Đã Hoàn Thành

### Phase 01 - Planning Docs

Đã tạo tài liệu phase trong `docs/phases/`:

- `phase-01-discovery-and-product-spec.md`
- `phase-02-android-project-foundation.md`
- `phase-03-supabase-backend-design.md`
- `phase-04-core-app-architecture.md`
- `phase-05-camera-transaction-capture.md`
- `phase-06-finance-management-features.md`
- `phase-07-polish-offline-and-security.md`
- `phase-08-testing-release-and-maintenance.md`

### Phase 02 - Android Foundation

Đã thay đổi:

- Rút gọn `MainActivity.kt`, chỉ còn `setContent` và gọi `APKBasicApp`.
- Thêm root navigation trong `core/navigation`.
- Thêm 5 destination MVP:
  - Dashboard
  - Capture
  - Transactions
  - Wallets
  - Settings
- Thêm placeholder screen theo feature trong `feature/*`.
- Thêm `PlaceholderScreen` dùng chung.
- Thêm permissions trong `AndroidManifest.xml`:
  - `android.permission.CAMERA`
  - `android.permission.INTERNET`
  - camera feature optional.
- Đổi `app_name` thành `SnapChi`.
- Thêm strings tiếng Việt không dấu cho navigation/placeholder.
- Thêm icon XML:
  - `ic_camera.xml`
  - `ic_receipt.xml`
  - `ic_wallet.xml`
  - `ic_settings.xml`
- Cập nhật theme color từ màu Compose mẫu sang palette tài chính riêng.
- Tắt dynamic color mặc định để UI giữ brand color ổn định.

### Phase 03 - Supabase Backend

Supabase MCP hoạt động. Project đang dùng:

- Project name: `Android`
- Project id/ref: `fosayyusmhfnogjwzttw`
- URL: `https://fosayyusmhfnogjwzttw.supabase.co`
- Organization: `vietbevis's Org`

Đã apply migration remote:

- `20260512004102_create_personal_finance_schema`
- `20260512004132_optimize_rls_and_foreign_key_indexes`

Đã tạo local migration files:

- `supabase/migrations/20260512004102_create_personal_finance_schema.sql`
- `supabase/migrations/20260512004132_optimize_rls_and_foreign_key_indexes.sql`

Schema đã tạo:

- `public.profiles`
- `public.wallets`
- `public.categories`
- `public.transactions`
- `public.transaction_photos`

Storage:

- Bucket private: `transaction-photos`
- File size limit: 5 MB
- Allowed mime types: `image/jpeg`, `image/png`, `image/webp`
- Path policy theo prefix user id: `{user_id}/...`

Security:

- RLS bật cho tất cả bảng app.
- User chỉ thao tác dữ liệu có `user_id = auth.uid()`.
- `profiles.id = auth.uid()`.
- Storage object chỉ truy cập được khi folder đầu tiên là user id.

Advisor:

- Security advisor: không có lint.
- Performance advisor: chỉ còn `unused_index`, hợp lý vì database mới chưa có data/query.

### Phase 04 - Core Architecture Foundation

Đã thêm domain layer:

- `domain/model/Money.kt`
- `domain/model/TransactionType.kt`
- `domain/model/Wallet.kt`
- `domain/model/Category.kt`
- `domain/model/Transaction.kt`
- `domain/model/UserProfile.kt`

Đã thêm repository contracts:

- `AuthRepository`
- `WalletRepository`
- `CategoryRepository`
- `TransactionRepository`
- `PhotoRepository`

Đã thêm validation:

- `domain/validation/TransactionValidator.kt`
- Kiểm tra amount > 0, wallet tồn tại/chưa archive, category đúng type/user, occurredAt hợp lệ.

Đã thêm Supabase SDK wiring:

- Version catalog thêm Supabase BOM, Auth, Postgrest, Storage, Ktor Android, Kotlin serialization plugin.
- `app/build.gradle.kts` đọc `SUPABASE_URL` và `SUPABASE_PUBLISHABLE_KEY` từ `local.properties` vào `BuildConfig`.
- `core/supabase/SupabaseConfig.kt`
- `core/supabase/SupabaseProvider.kt`

Đã thêm DTO/mappers:

- `data/profile/ProfileDto.kt`
- `data/wallet/WalletDto.kt`
- `data/category/CategoryDto.kt`
- `data/transaction/TransactionDto.kt`

Local config:

- `local.properties` đã có:
  - `SUPABASE_URL=https://fosayyusmhfnogjwzttw.supabase.co`
  - `SUPABASE_PUBLISHABLE_KEY=sb_publishable_...`
- Key này là publishable key, không phải service role key.
- Không đưa service role key vào app.

## Tests Và Build Đã Chạy

Đã pass:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

## Phase 04 - Update 2026-05-12

Da hoan thanh phan con thieu cua Phase 04:

- Them `AppError`, `appResult` va `DateCodecs` dung chung.
- Them `AppContainer` de gom Supabase client va repository implementations.
- Implement repository Supabase thuc te cho `AuthRepository`, `WalletRepository`, `CategoryRepository`, `TransactionRepository`.
- Them onboarding bootstrap: upsert profile, tao wallet mac dinh `Tien mat`, tao categories mac dinh income/expense.
- Them `AuthViewModel` va `AuthScreen` cho login, signup, logout, root auth state.
- Root app chay bootstrap truoc khi vao main graph.
- Cap nhat Settings co nut dang xuat.

Da pass:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

Buoc tiep theo: chuyen sang Phase 05 Camera Transaction Capture, bat dau tu CameraX dependencies, permission state UI, camera preview, capture preview, transaction form va save/upload flow.

## Phase 05 - Update 2026-05-12

Da trien khai camera transaction capture MVP:

- Them CameraX 1.6.0 va lifecycle runtime compose dependencies.
- `CaptureScreen` khong con placeholder: co permission rationale, camera preview, nut chup, doi camera, toggle flash.
- Anh chup duoc luu tam trong cache app va hien preview truoc khi luu.
- Them form giao dich dung wallets/categories that tu Supabase.
- Them `CaptureViewModel` quan ly state, validation, upload anh va save transaction.
- Them `SupabasePhotoRepository` upload vao bucket private `transaction-photos` theo path `{user_id}/{transaction_id}/{timestamp}.jpg`.
- Neu upload anh that bai, user co the thu lai hoac luu giao dich khong anh.
- Root app truyen `AppContainer` va `UserProfile` vao capture flow.

Da pass:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

Con lai cua Phase 05: can manual test tren emulator/device co camera de xac nhan permission/capture thuc te va storage upload voi tai khoan Supabase that.

## Phase 06-08 - Update 2026-05-12

Da tiep tuc hoan thien MVP cho cac phase con lai:

- Phase 06:
  - Dashboard dung du lieu Supabase that cho thang hien tai.
  - Lich su giao dich hien danh sach thang hien tai va xoa co confirm.
  - Man hinh vi/danh muc tao moi va archive co confirm.
  - Them aggregate logic `FinanceSummaryCalculator` va unit test.
- Phase 07:
  - Them accessibility labels cho camera controls chinh.
  - Amount keyboard dung numeric.
  - Supabase config da doc tu Gradle property/env/local, khong hardcode trong source.
  - Da scan source khong thay `service_role`/secret key trong app/docs/supabase.
  - Anh CameraX dat JPEG quality 82 de giam dung luong upload.
- Phase 08:
  - Them `docs/release-checklist.md` gom build evidence, manual test, release notes draft va backlog sau MVP.
  - Da chay `assembleRelease` thanh cong. Release public van can keystore production.

Da pass:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

Con lai can thiet bi/emulator:

- `connectedAndroidTest`
- Manual camera permission/capture/upload
- RLS test bang hai tai khoan Supabase that

Lưu ý:

- Có lần chạy `test` và `assembleDebug` song song gây tranh chấp Kotlin incremental cache.
- Đã chạy `.\gradlew.bat --stop`, sau đó chạy tuần tự thì pass.
- Khi cần verify, nên chạy tuần tự từng lệnh Gradle.

## Files Chính Đã Thay Đổi

Android:

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/vn/vietbevis/apkbasic/MainActivity.kt`
- `app/src/main/java/vn/vietbevis/apkbasic/core/navigation/*`
- `app/src/main/java/vn/vietbevis/apkbasic/core/supabase/*`
- `app/src/main/java/vn/vietbevis/apkbasic/domain/*`
- `app/src/main/java/vn/vietbevis/apkbasic/data/*`
- `app/src/main/java/vn/vietbevis/apkbasic/feature/*`
- `app/src/main/res/values/*`
- `app/src/main/res/drawable/ic_camera.xml`
- `app/src/main/res/drawable/ic_receipt.xml`
- `app/src/main/res/drawable/ic_wallet.xml`
- `app/src/main/res/drawable/ic_settings.xml`

Tests:

- Removed sample `ExampleUnitTest.kt`.
- Added:
  - `app/src/test/java/vn/vietbevis/apkbasic/domain/model/MoneyTest.kt`
  - `app/src/test/java/vn/vietbevis/apkbasic/domain/validation/TransactionValidatorTest.kt`
  - `app/src/test/java/vn/vietbevis/apkbasic/data/DtoMappingTest.kt`

Docs/backend:

- `docs/phases/*.md`
- `docs/session-handoff.md`
- `supabase/migrations/*.sql`

## Bước Tiếp Theo Đề Xuất

Tiếp tục Phase 04:

1. Implement repository Supabase thực tế cho Auth/Profile/Wallet/Category.
2. Thêm onboarding bootstrap:
   - tạo profile,
   - tạo wallet mặc định `Tien mat`,
   - tạo categories mặc định income/expense.
3. Tạo Auth UI/ViewModel:
   - login,
   - signup,
   - logout,
   - root auth state.
4. Sau khi Auth và onboarding chạy được, chuyển sang Phase 05 Camera Transaction Capture.

Trước khi tiếp tục, nên chạy:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```
