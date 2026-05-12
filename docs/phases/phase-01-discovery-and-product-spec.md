# Phase 01 - Discovery And Product Spec

## Mục tiêu

Định nghĩa rõ MVP cho app quản lý tài chính cá nhân theo hướng camera-first: người dùng mở app, chụp ảnh nhanh như Locket, nhập tay thông tin thu/chi, lưu lại ảnh như bằng chứng/ngữ cảnh và xem lại lịch sử tài chính.

Phase này chưa triển khai code tính năng. Kết quả chính là spec đủ rõ để các phase Android, Supabase và UI không phải tự đoán.

## Phạm vi MVP

Trong scope:

- Đăng ký, đăng nhập và đăng xuất bằng Supabase Auth.
- Quản lý tài chính cá nhân cho một user.
- Chụp ảnh giao dịch, xem preview, nhập tay thông tin giao dịch.
- Lưu thu/chi gồm số tiền, loại giao dịch, danh mục, ví/tài khoản, ghi chú, thời gian và ảnh đính kèm.
- Xem dashboard tổng quan, lịch sử giao dịch và chi tiết giao dịch.
- Đồng bộ dữ liệu và ảnh qua Supabase.

Ngoài scope MVP:

- Social feed, bạn bè, reaction, comment.
- Widget home-screen kiểu Locket.
- OCR hóa đơn, AI phân loại giao dịch.
- Nhóm gia đình, chia sẻ ví, phân quyền nhiều thành viên.
- Kết nối ngân hàng hoặc ví điện tử.

## Persona Và Use Case

Persona chính:

- Người dùng cá nhân muốn ghi lại chi tiêu nhanh trong ngày.
- Có thói quen chụp ảnh hóa đơn, món đồ, địa điểm hoặc khoảnh khắc liên quan tới khoản chi.
- Muốn xem lại mình đã chi bao nhiêu theo ngày, tháng, danh mục và ví.

Use case chính:

- Tôi vừa mua cà phê, mở app, chụp ảnh ly cà phê, nhập 45.000 VND, chọn "Ăn uống", lưu trong vài giây.
- Tôi nhận lương, tạo giao dịch thu nhập không cần ảnh hoặc có ảnh chứng từ.
- Tôi muốn xem tháng này đã chi bao nhiêu cho ăn uống và di chuyển.
- Tôi muốn mở lại một giao dịch để xem ảnh gốc và ghi chú.

## Luồng Sản Phẩm Chính

1. User mở app.
2. Nếu chưa đăng nhập, app hiển thị Auth screen.
3. Sau khi đăng nhập, app vào Dashboard.
4. User nhấn tab hoặc nút Capture.
5. App xin quyền camera nếu chưa có.
6. Camera mở nhanh với UI tối giản: preview, nút chụp, đổi camera, flash nếu hỗ trợ.
7. User chụp ảnh.
8. App hiển thị preview và form nhập giao dịch.
9. User nhập số tiền, chọn thu/chi, danh mục, ví, ghi chú và thời gian.
10. App validate form, upload ảnh, lưu transaction.
11. App đưa user về detail hoặc history, transaction mới xuất hiện ngay.

## Nguyên Tắc UX

- Capture là hành động trung tâm, luôn dễ truy cập từ navigation chính.
- Form sau khi chụp phải ngắn, ưu tiên thao tác một tay.
- Không bắt buộc ảnh cho mọi giao dịch; thu nhập hoặc chỉnh sửa thủ công vẫn được hỗ trợ.
- Dữ liệu tài chính phải rõ ràng hơn yếu tố social.
- Locket chỉ là tham chiếu về sự nhanh, đơn giản và lịch sử ảnh; không sao chép social model.

## Deliverables

- Chốt tên app tạm thời trong resource string.
- Chốt danh sách màn hình MVP.
- Chốt các entity nghiệp vụ: User, Wallet, Category, Transaction, TransactionPhoto.
- Chốt format tiền mặc định: VND, không decimal trong MVP.
- Chốt danh mục mặc định ban đầu.

Danh mục mặc định đề xuất:

- Expense: Ăn uống, Di chuyển, Mua sắm, Hóa đơn, Giải trí, Sức khỏe, Giáo dục, Khác.
- Income: Lương, Thưởng, Đầu tư, Quà tặng, Khác.

## Checklist

- [ ] Viết mô tả MVP ngắn gọn trong README hoặc tài liệu overview nếu cần.
- [ ] Xác nhận app chỉ hỗ trợ cá nhân trong release đầu.
- [ ] Xác nhận VND là tiền tệ mặc định.
- [ ] Xác nhận danh mục mặc định.
- [ ] Xác nhận ảnh giao dịch là optional hay required. Mặc định: optional, nhưng capture flow sẽ có ảnh.
- [ ] Xác nhận không triển khai social/widget/OCR/AI trong MVP.

## Tiêu Chí Hoàn Thành

- Team có thể giải thích app làm gì trong một đoạn ngắn.
- Không còn nhầm lẫn giữa app tài chính và app social.
- Các phase sau có thể thiết kế schema, UI và architecture dựa trên scope này.

