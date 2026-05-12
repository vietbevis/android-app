# Phase 06 - Finance Management Features

## Mục tiêu

Hoàn thiện các tính năng tài chính cốt lõi quanh capture flow: dashboard, lịch sử giao dịch, chi tiết giao dịch, ví/tài khoản và danh mục.

## Dashboard

Hiển thị:

- Tổng thu tháng hiện tại.
- Tổng chi tháng hiện tại.
- Số dư ước tính theo ví.
- Chi tiêu theo danh mục lớn nhất.
- Giao dịch gần đây.
- CTA mở Capture.

Rules:

- Mặc định lọc theo tháng hiện tại.
- Số âm/dương phải dễ hiểu, không chỉ dựa vào màu.
- Nếu chưa có giao dịch, hiển thị empty state và CTA tạo giao dịch đầu tiên.

## Transaction History

Tính năng:

- Danh sách giao dịch group theo ngày.
- Mỗi item hiển thị loại, amount, category, wallet, note ngắn, thumbnail nếu có.
- Filter theo tháng, type, category, wallet.
- Search theo note hoặc category name.
- Pull-to-refresh nếu phù hợp.

Detail:

- Ảnh lớn nếu có.
- Tất cả metadata giao dịch.
- Edit amount/type/category/wallet/note/date.
- Delete transaction với confirm dialog.

## Wallets

MVP:

- Tạo ví mới.
- Đổi tên ví.
- Chọn type: cash, bank, e-wallet, other.
- Archive ví thay vì xóa nếu đã có transaction.
- Xem số dư ước tính: initial balance + income - expense.

Không cần reconciliation ngân hàng trong MVP.

## Categories

MVP:

- Danh mục mặc định được tạo lúc onboarding.
- User có thể tạo category mới cho income hoặc expense.
- User có thể đổi tên/icon/color category tự tạo.
- Archive category thay vì xóa nếu đã có transaction.

Nếu category bị archive, transaction cũ vẫn hiển thị tên category đó.

## Reporting Logic

Các aggregate cần có:

- Monthly income.
- Monthly expense.
- Net change.
- Expense by category.
- Wallet balance estimate.

Ban đầu có thể tính ở repository/client sau khi query transactions theo tháng. Khi dữ liệu lớn hơn, chuyển sang SQL view/RPC.

## Checklist

- [x] Tạo Dashboard screen với dữ liệu thật.
- [x] Tạo Transaction History screen.
- [ ] Tạo Transaction Detail screen.
- [ ] Tạo edit/delete transaction flow.
- [ ] Tạo Wallet list/create/edit/archive.
- [ ] Tạo Category list/create/edit/archive.
- [ ] Tạo filter/search cho transaction list.
- [x] Tạo empty states cho dashboard/history/wallet/category.
- [x] Thêm unit test cho aggregate logic.
- [ ] Thêm Compose UI test cho form và list states.

## Tiêu Chí Hoàn Thành

- User có thể quản lý toàn bộ vòng đời giao dịch sau khi capture.
- Dashboard phản ánh đúng dữ liệu tháng hiện tại.
- Wallet balance và category totals tính đúng trong test.
- Không mất dữ liệu lịch sử khi archive wallet/category.
