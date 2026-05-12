# Phase 02 - Android Project Foundation

## Mục tiêu

Chuẩn hóa nền tảng Android để dự án sẵn sàng phát triển theo module logic rõ ràng: Kotlin, Jetpack Compose cho UI chính, XML cho manifest, theme/resource/drawable, Supabase client, CameraX và navigation.

## Hiện Trạng Repo

- Android app đang dùng Gradle Kotlin DSL.
- Package hiện tại: `vn.vietbevis.apkbasic`.
- UI ban đầu nằm chủ yếu trong `MainActivity.kt`.
- Đã có Compose, Material 3, NavigationSuiteScaffold và một số drawable XML.
- Chưa có cấu trúc feature/data/domain rõ ràng.

## Dependencies Cần Thêm

Thêm vào version catalog và `app/build.gradle.kts` theo thứ tự cần dùng:

- Navigation: `androidx.navigation:navigation-compose`.
- Lifecycle: `androidx.lifecycle:lifecycle-viewmodel-compose`.
- Serialization: Kotlin serialization plugin và `kotlinx-serialization-json`.
- Supabase Kotlin: Auth, Postgrest, Storage.
- Ktor client Android cho Supabase networking.
- CameraX: camera-core, camera-camera2, camera-lifecycle, camera-view.
- Coil Compose để hiển thị ảnh giao dịch.
- DataStore Preferences để lưu cấu hình nhỏ như currency, onboarding flag.
- Room chỉ thêm ở phase offline; không thêm sớm nếu chưa dùng.

## Cấu Trúc Package Đề Xuất

Tổ chức theo feature, giữ đơn giản cho app một module:

```text
vn.vietbevis.apkbasic
  core
    config
    design
    navigation
    result
    supabase
  data
    auth
    category
    transaction
    wallet
  domain
    model
    repository
    validation
  feature
    auth
    capture
    dashboard
    transactions
    wallet
    settings
  ui
    theme
```

Quy tắc:

- `feature/*` chứa Compose screen, component riêng của feature và ViewModel.
- `domain/*` chứa model app dùng nội bộ, validation và repository interface.
- `data/*` chứa DTO, Supabase implementation và mapping.
- `core/*` chứa helper dùng chung, không chứa business logic nặng.

## Navigation

Navigation chính MVP:

- `auth`: login/register.
- `dashboard`: tổng quan tháng hiện tại.
- `capture`: camera và form lưu giao dịch.
- `transactions`: lịch sử và detail.
- `wallets`: ví/tài khoản và danh mục.
- `settings`: profile, logout, cấu hình cơ bản.

Bottom navigation:

- Dashboard
- Capture
- Transactions
- Wallets
- Settings

Capture nên là tab/nút nổi bật nhất nhưng vẫn dùng Compose component nhất quán với Material 3.

## XML Resource

Giữ XML cho:

- `AndroidManifest.xml`: permissions, activity, file provider nếu cần.
- `res/values/strings.xml`: tên app, label màn hình, thông báo quyền.
- `res/values/colors.xml` và `themes.xml`: theme base.
- `res/xml/backup_rules.xml`, `data_extraction_rules.xml`: cập nhật nếu lưu dữ liệu nhạy cảm local.
- Drawable/vector icons cho navigation và trạng thái rỗng nếu dùng XML vector.

Permissions dự kiến:

- `android.permission.CAMERA`.
- `android.permission.INTERNET`.
- Android 13+: chỉ cần storage permission nếu đọc ảnh từ gallery; MVP capture trực tiếp không cần gallery.

## Checklist

- [ ] Thêm dependencies cần thiết theo version catalog.
- [ ] Thêm Kotlin serialization plugin nếu dùng DTO serialization.
- [ ] Tách `MainActivity.kt` để chỉ còn setContent và gọi root app.
- [ ] Tạo root navigation graph.
- [ ] Tạo theme tokens cơ bản: color, typography, spacing helper nếu cần.
- [ ] Cập nhật `strings.xml` cho tên app và label tiếng Việt.
- [ ] Thêm permissions vào manifest.
- [ ] Đảm bảo app vẫn build sau mỗi nhóm thay đổi.

## Tiêu Chí Hoàn Thành

- App build thành công bằng `gradlew assembleDebug`.
- Có navigation shell với các màn hình placeholder.
- Code không còn dồn toàn bộ vào `MainActivity.kt`.
- Resource XML được dùng đúng vai trò, Compose không hardcode mọi chuỗi hiển thị.

