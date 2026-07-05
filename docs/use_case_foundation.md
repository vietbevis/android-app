# Nền tảng Use Case SnapChi

Tài liệu này cung cấp các khối chức năng nền tảng để xây dựng các use case chi tiết cho ứng dụng quản lý tài chính cá nhân SnapChi. Nội dung dựa trên các đặc tả tính năng hiện tại của dự án.

## Tác nhân chính: Người dùng cá nhân
Một người muốn theo dõi các khoản thu chi hàng ngày, quản lý ngân sách và trực quan hóa sức khỏe tài chính của mình bằng một ứng dụng di động ưu tiên trải nghiệm camera.

---

## 1. Xác thực & Bắt đầu sử dụng
*   **Đăng ký**: Tạo tài khoản mới bằng email và mật khẩu.
*   **Đăng nhập**: Truy cập vào tài khoản đã có.
*   **Đăng xuất**: Kết thúc phiên làm việc hiện tại.

## 2. Theo dõi hàng ngày (Bảng điều khiển)
*   **Xem tóm tắt tháng**: Xem nhanh tổng thu nhập và chi tiêu của tháng hiện tại.
*   **Kiểm tra hạn mức tiêu dùng**: Xem số tiền "An toàn để chi tiêu" được tính toán cho ngày hôm nay và tuần này.
*   **Tương tác với lịch**: Chạm vào các ngày cụ thể để xem tóm tắt (số lượng giao dịch, tổng thu/chi và danh sách chi tiết).

## 3. Quản lý & Thêm giao dịch
*   **Thêm giao dịch mới**: Ghi lại các khoản thu/chi bằng cách chụp ảnh biên lai (CameraX) hoặc nhập liệu thủ công.
*   **Tính toán số tiền**: Sử dụng bàn phím máy tính tích hợp (với phím tắt "000") để xác định số tiền cuối cùng.
*   **Gán thông tin bổ sung**: Chọn danh mục, ví, ngày tháng và thêm ghi chú tùy chọn.
*   **Lưu và Đồng bộ**: Lưu bản ghi cục bộ và đồng bộ với Supabase Cloud (bao gồm cả lưu trữ hình ảnh).
*   **Duyệt lịch sử**: Xem danh sách chi tiết tất cả các giao dịch trong một tháng cụ thể.
*   **Xem chi tiết**: Mở rộng thẻ giao dịch để xem đầy đủ thông tin và ảnh biên lai đính kèm.
*   **Chỉnh sửa giao dịch**: Thay đổi thông tin của một giao dịch đã tồn tại.
*   **Xóa giao dịch**: Loại bỏ giao dịch khỏi hệ thống (có yêu cầu xác nhận).

## 4. Kiểm soát ngân sách
*   **Thiết lập ngân sách tổng**: Xác định hạn mức chi tiêu cho toàn bộ tháng.
*   **Thiết lập ngân sách danh mục**: Xác định hạn mức cụ thể cho từng danh mục (ví dụ: "Ăn uống").
*   **Theo dõi tiến độ**: Giám sát tốc độ chi tiêu thông qua các thanh tiến độ trực quan và màu sắc cảnh báo.

## 5. Thống kê & Xuất dữ liệu
*   **Xem thống kê tháng**: Phân tích phân phối thu nhập/chi tiêu qua biểu đồ tròn và theo dõi việc thực hiện mục tiêu.
*   **Xem thống kê năm**: Quan sát các mô hình tài chính trong năm và theo dõi các tháng "tiết kiệm thành công" (Hiệu quả quản lý).
*   **Xuất dữ liệu**: Tạo và chia sẻ tệp CSV chứa các bản ghi giao dịch để báo cáo hoặc lưu trữ bên ngoài.

## 6. Cá nhân hóa & Hành vi hệ thống
*   **Quản lý hồ sơ**: Xem và chỉnh sửa thông tin tài khoản người dùng (tên hiển thị, ảnh đại diện).
*   **Thiết lập tùy chọn**: Tùy chỉnh ngôn ngữ (Tiếng Việt/Tiếng Anh) và chế độ giao diện (Sáng/Tối).
*   **Đồng bộ liền mạch**: Trải nghiệm khả năng cách ly dữ liệu an toàn và xử lý lỗi mạng mượt mà.
