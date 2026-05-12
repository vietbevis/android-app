# Phase 05 - Camera Transaction Capture

## Mục tiêu

Triển khai trải nghiệm camera-first: mở camera nhanh, chụp ảnh, preview, nhập tay giao dịch, upload ảnh và lưu transaction. Đây là feature trung tâm tạo khác biệt so với app quản lý chi tiêu thông thường.

## UX Flow

1. User mở tab Capture.
2. App kiểm tra quyền camera.
3. Nếu chưa có quyền, hiển thị permission rationale ngắn và nút cấp quyền.
4. Khi có quyền, hiển thị camera preview toàn màn hình hoặc gần toàn màn hình.
5. User chụp ảnh bằng nút lớn ở dưới.
6. App hiển thị ảnh preview.
7. User chọn:
   - Dùng ảnh này.
   - Chụp lại.
   - Bỏ ảnh và nhập giao dịch thủ công.
8. Form nhập giao dịch xuất hiện.
9. User lưu.
10. App upload ảnh, tạo transaction, cập nhật history/dashboard.

## Camera UI

Controls MVP:

- Capture button lớn.
- Toggle camera trước/sau nếu thiết bị hỗ trợ.
- Flash on/off nếu camera hỗ trợ.
- Nút đóng/quay lại.

Không thêm filter, crop, sticker, caption social hoặc gallery picker trong MVP.

## Transaction Form Sau Khi Chụp

Fields:

- Amount: bắt buộc, lớn hơn 0.
- Type: expense/income, mặc định expense.
- Category: bắt buộc với danh sách tương ứng type.
- Wallet: bắt buộc, mặc định wallet đầu tiên chưa archive.
- Occurred at: mặc định thời điểm hiện tại, cho phép chỉnh.
- Note: optional.
- Photo preview: hiển thị thumbnail ảnh vừa chụp.

Validation:

- Không lưu nếu amount rỗng hoặc <= 0.
- Không lưu nếu chưa có wallet.
- Category phải cùng type với transaction.
- Nếu upload ảnh thất bại, user được chọn retry hoặc lưu giao dịch không ảnh.

## Data Flow

- CameraX tạo file ảnh tạm trong cache app.
- Khi user lưu, repository upload ảnh vào Supabase Storage path `{user_id}/{transaction_id}/{timestamp}.jpg`.
- Sau upload thành công, lưu transaction với `photo_path`.
- Nếu transaction tạo trước upload để lấy id, cần rollback hoặc update trạng thái khi upload lỗi.

Khuyến nghị MVP:

1. Generate transaction id client-side.
2. Upload ảnh theo id đó.
3. Insert transaction kèm `photo_path`.
4. Nếu insert lỗi sau upload, xóa ảnh vừa upload nếu SDK hỗ trợ; nếu không, ghi nhận cleanup backlog.

## Android Implementation Notes

- Dùng CameraX `PreviewView` trong Compose bằng `AndroidView`.
- Bind camera lifecycle theo `LocalLifecycleOwner`.
- Tách camera controller/helper khỏi Composable nếu logic dài.
- Dùng Coil để hiển thị preview từ file local và ảnh remote.
- Compress ảnh trước upload nếu dung lượng lớn; mục tiêu dưới 1MB cho MVP.

## Checklist

- [x] Thêm camera permission vào manifest.
- [x] Thêm CameraX dependencies.
- [x] Tạo `CaptureScreen`.
- [x] Tạo permission state UI.
- [x] Tạo camera preview với capture button.
- [x] Tạo preview ảnh sau chụp.
- [x] Tạo transaction form.
- [x] Tạo upload photo repository.
- [x] Tạo save transaction flow.
- [x] Xử lý loading, retry và error states.
- [ ] Thêm instrumented test hoặc manual test checklist cho permission/capture.

## Tiêu Chí Hoàn Thành

- Trên thiết bị/emulator có camera, user chụp được ảnh và lưu thành transaction.
- Transaction mới xuất hiện trong danh sách với ảnh thumbnail.
- Khi từ chối quyền camera, app không crash và có hướng dẫn rõ ràng.
- Khi mất mạng, app báo lỗi lưu/upload rõ ràng.
