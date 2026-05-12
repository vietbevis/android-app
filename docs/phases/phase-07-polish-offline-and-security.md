# Phase 07 - Polish Offline And Security

## Mục tiêu

Nâng chất lượng app sau khi MVP chạy được: trạng thái loading/error tốt hơn, cache/offline cơ bản, bảo mật token/config, tối ưu ảnh và hoàn thiện trải nghiệm trước release.

## UX Polish

Cần hoàn thiện:

- Loading skeleton hoặc progress phù hợp cho dashboard/history.
- Snackbar/toast cho thao tác lưu, xóa, lỗi mạng.
- Empty states có CTA rõ ràng.
- Confirm dialogs cho delete/archive.
- Form không mất dữ liệu khi xoay màn hình hoặc app bị background ngắn.
- Keyboard handling tốt cho amount/note.
- Accessibility labels cho nút camera, đổi camera, flash, save.

## Offline Strategy

MVP có thể bắt đầu bằng cache nhẹ:

- DataStore cho settings nhỏ.
- Room cho transactions/wallets/categories nếu cần đọc offline.
- Queue upload offline là backlog nếu chưa đủ thời gian.

Khuyến nghị release đầu:

- Nếu không có Room, app cần báo rõ khi offline và không hứa lưu được offline.
- Nếu có Room, cache read-only trước: dashboard/history dùng dữ liệu cuối cùng đã sync.
- Offline write queue chỉ làm khi có đủ test vì dễ phát sinh conflict.

## Security

- Không hardcode Supabase URL/key trực tiếp trong source nếu repo public.
- Dùng `local.properties`, Gradle BuildConfig hoặc secret plugin phù hợp.
- Supabase anon key không phải service role key; tuyệt đối không đưa service role vào app.
- Bật RLS trước khi test app với dữ liệu thật.
- Storage bucket private.
- Không log access token, refresh token, URL signed nhạy cảm hoặc payload tài chính trong release.

## Privacy

- Ảnh giao dịch có thể chứa thông tin nhạy cảm như hóa đơn, địa chỉ, số điện thoại.
- Backup rules nên tránh backup cache ảnh tạm nếu không cần.
- Có tùy chọn xóa ảnh khi xóa transaction.
- Chính sách dữ liệu cần ghi rõ nếu phát hành public.

## Performance

- Nén ảnh trước upload.
- Dùng thumbnail/list image size hợp lý.
- Paging transaction list nếu dữ liệu tăng.
- Query dashboard theo khoảng ngày, không tải toàn bộ lịch sử.
- Tránh recomposition nặng trong camera preview.

## Checklist

- [x] Rà soát loading/error/empty states toàn app.
- [x] Thêm accessibility labels cho controls chính.
- [x] Kiểm tra keyboard và responsive layout.
- [x] Chuyển config Supabase ra khỏi source hardcoded.
- [x] Xác nhận không có service role key trong app.
- [ ] Kiểm tra RLS bằng hai tài khoản.
- [x] Tối ưu kích thước ảnh upload.
- [ ] Cập nhật backup/data extraction XML nếu cần.
- [x] Quyết định có làm Room cache trong release đầu hay để backlog.

## Tiêu Chí Hoàn Thành

- App không crash trong các case mất mạng, hết session, từ chối permission.
- Không lộ secret nguy hiểm trong source.
- Ảnh upload có dung lượng hợp lý.
- UX đủ rõ để người dùng không bị kẹt ở trạng thái lỗi hoặc rỗng.
