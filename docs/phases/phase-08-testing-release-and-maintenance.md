# Phase 08 - Testing Release And Maintenance

## Mục tiêu

Hoàn thiện kiểm thử, build release và checklist bảo trì để app có thể phát hành nội bộ hoặc public sau MVP.

## Test Strategy

Unit tests:

- Money parsing và formatting VND.
- Transaction validation.
- Category filtering theo income/expense.
- Dashboard aggregate theo tháng.
- Wallet balance estimate.
- DTO/domain mapping.

Compose UI tests:

- Auth form validation.
- Navigation giữa Dashboard, Capture, Transactions, Wallets, Settings.
- Transaction form validation.
- Empty/loading/error states.
- Transaction list render đúng group ngày.

Instrumented/manual tests:

- Camera permission allow/deny.
- Chụp ảnh, preview, retake, save.
- Upload ảnh khi mạng ổn định.
- Mất mạng khi upload/save.
- Logout/login lại vẫn thấy dữ liệu.
- User A không thấy dữ liệu user B.

## Build Checks

Chạy trước khi release:

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
.\gradlew.bat connectedAndroidTest
.\gradlew.bat assembleRelease
```

`connectedAndroidTest` cần emulator hoặc thiết bị thật. Nếu không có thiết bị, ghi rõ chưa chạy được và thay bằng manual test trên máy thật khi có.

## Release Preparation

- Cập nhật `versionCode` và `versionName`.
- Cấu hình signing release.
- Kiểm tra app name, icon, permissions và privacy copy.
- Kiểm tra ProGuard/R8 nếu bật minify.
- Tạo Supabase project production riêng nếu cần.
- Không dùng database dev cho release public.
- Ghi lại migration SQL đã chạy.

## Acceptance Criteria MVP

MVP được xem là hoàn thành khi:

- User đăng ký/đăng nhập/đăng xuất được.
- User tạo được wallet/category mặc định qua onboarding.
- User chụp ảnh và lưu giao dịch thu/chi được.
- User tạo giao dịch không ảnh được.
- User xem dashboard tháng hiện tại được.
- User xem, sửa, xóa giao dịch được.
- User quản lý wallet/category cơ bản được.
- Dữ liệu và ảnh đồng bộ Supabase.
- RLS chặn truy cập chéo user.
- App build debug thành công và có checklist release.

## Maintenance Backlog Sau MVP

Ưu tiên cao:

- OCR hóa đơn để gợi ý amount/date/merchant.
- Offline write queue.
- Home-screen widget hiển thị ảnh/giao dịch gần đây.
- Export CSV.
- Budget theo danh mục.

Ưu tiên trung bình:

- Nhóm gia đình/shared wallet.
- Recurring transactions.
- Multi-currency.
- Push notification nhắc ghi chi tiêu.
- Biểu đồ nâng cao.

Ưu tiên thấp hoặc cần cân nhắc:

- Social friend feed kiểu Locket.
- Reaction/comment cho ảnh giao dịch.
- AI tự phân loại ảnh.
- Kết nối ngân hàng/ví điện tử.

## Checklist

- [x] Chạy unit tests.
- [x] Chạy build debug.
- [ ] Chạy instrumented tests nếu có thiết bị.
- [ ] Test manual end-to-end trên máy thật.
- [ ] Test RLS bằng hai tài khoản.
- [ ] Chuẩn bị release signing.
- [x] Tạo release notes.
- [x] Chốt backlog sau MVP.

Ghi chú 2026-05-12: `assembleRelease` đã chạy thành công. Release signing production vẫn cần cấu hình keystore thật trước khi phát hành public.

## Tiêu Chí Hoàn Thành

- Có bằng chứng test/build rõ ràng.
- Release candidate không dùng secret hoặc database dev nhạy cảm.
- Team có checklist bảo trì và backlog sau MVP.
