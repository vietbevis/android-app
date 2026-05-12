# Phase 04 - Core App Architecture

## Mục tiêu

Xây dựng kiến trúc app đủ rõ để các feature phát triển ổn định: domain model, repository, Supabase data source, ViewModel state, navigation và error handling.

## Layering

Domain layer:

- Model dùng trong app: `UserProfile`, `Wallet`, `Category`, `Transaction`, `TransactionType`, `Money`.
- Repository interface: `AuthRepository`, `WalletRepository`, `CategoryRepository`, `TransactionRepository`, `PhotoRepository`.
- Validation: validate số tiền, danh mục theo loại thu/chi, ví chưa archive, ngày hợp lệ.

Data layer:

- Supabase DTO tương ứng với bảng.
- Mapper DTO sang domain model.
- Repository implementation gọi Supabase.
- Result wrapper hoặc dùng Kotlin `Result` nhất quán.

Feature layer:

- Mỗi screen có ViewModel riêng khi có state hoặc side effect.
- UI state là data class immutable.
- Event từ UI đi vào ViewModel qua function rõ nghĩa, không truyền Supabase client trực tiếp vào Composable.

## State Model

Quy ước UI state:

```kotlin
data class ScreenUiState(
    val isLoading: Boolean = false,
    val data: ... = ...,
    val errorMessage: String? = null
)
```

Quy ước one-off event:

- Navigate after save.
- Show snackbar.
- Request permission handled ở UI, kết quả chuyển vào ViewModel nếu ảnh hưởng flow.

## Auth Session Flow

Root app quan sát auth state:

- Loading session.
- Unauthenticated: hiển thị Auth graph.
- Authenticated: chạy onboarding check rồi hiển thị Main graph.

Onboarding check:

- Nếu profile/wallet/categories mặc định chưa có, tạo dữ liệu mặc định.
- Nếu tạo lỗi, hiển thị retry.

## Error Handling

Chuẩn hóa lỗi:

- Network unavailable.
- Unauthorized/session expired.
- Validation error.
- Supabase/database error.
- Upload photo failed.

UI không hiển thị stack trace. Log chi tiết trong debug, message thân thiện cho user.

## Money Và Date

Money:

- MVP dùng VND.
- Lưu database bằng `numeric`.
- Trong app dùng `Long` cho số tiền VND nếu không có decimal, hoặc `BigDecimal` nếu mapper Supabase yêu cầu.
- Format hiển thị: `45.000 đ`.

Date:

- Lưu `occurred_at` bằng timestamptz.
- UI group theo timezone thiết bị.
- Dashboard mặc định tháng hiện tại.

## Checklist

- [x] Tạo domain models và enum `TransactionType`.
- [x] Tạo repository interfaces.
- [x] Tạo Supabase client provider.
- [x] Tạo DTO và mapper cho bảng MVP.
- [x] Tạo root auth state handling.
- [x] Tạo onboarding data bootstrap.
- [x] Tạo navigation route constants/types.
- [x] Tạo error/result convention dùng chung.
- [x] Thêm unit test cho validation và money formatting.

## Tiêu Chí Hoàn Thành

- Feature code không gọi Supabase SDK trực tiếp từ Composable.
- Auth state quyết định đúng graph hiển thị.
- Onboarding tạo được dữ liệu mặc định sau đăng ký.
- Có test cho logic domain cơ bản.
