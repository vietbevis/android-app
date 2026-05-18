# DESIGN.md

Tài liệu này mô tả design system mục tiêu cho ứng dụng Android Kotlin/Jetpack Compose `SnapChi`, được diễn giải từ file Figma `Subscription Management App` và điều chỉnh cho sản phẩm quản lý tài chính cá nhân camera-first. Khi triển khai UI, ưu tiên Material 3, Compose, các component hiện có trong `ui/theme` và `ui/components`, đồng thời giữ tinh thần thị giác của Figma: nền kem ấm, chữ navy rất đậm, điểm nhấn cam san hô, card rõ ràng, ít trang trí, dữ liệu tài chính dễ quét.

## Nguồn Thiết Kế Figma

- File: `Subscription Management App`
- Node gốc: `0:1`
- Các màn hình tham chiếu:
  - `Splash-Screen`: màn giới thiệu với nền cam, graphic hình học lớn, CTA pill trắng.
  - `Login`: logo app, tiêu đề, mô tả ngắn, form trong khung kem, input viền mảnh, CTA cam.
  - `Home`: avatar, tổng số dư, danh sách mục sắp tới, danh sách giao dịch/đăng ký, bottom navigation nổi.
  - `Subscriptions`: danh sách item, item đang mở rộng với chi tiết và CTA hủy.
  - `Monthly Insights`: biểu đồ cột, chỉ số tháng, lịch sử giao dịch, bottom navigation nổi.

Figma là nguồn cho visual language, không phải mô hình nghiệp vụ cuối cùng. Khi áp dụng cho SnapChi, đổi nhãn và nội dung sang tài chính cá nhân: ví, giao dịch, biên lai, ngân sách, thống kê, tài khoản và cài đặt.

## Nguyên Tắc Thiết Kế

SnapChi là công cụ tài chính, vì vậy giao diện phải tạo cảm giác tin cậy, nhẹ, rõ và nhanh để thao tác hằng ngày.

- Ưu tiên dữ liệu: số tiền, ngày, ví, danh mục và trạng thái phải nổi bật hơn trang trí.
- Sử dụng nền sáng ấm làm mặc định để tạo cảm giác thân thiện, tránh dashboard tối nặng nề.
- Dùng màu cam san hô cho hành động chính, trạng thái đang chọn và các điểm nhấn cần chú ý.
- Dùng navy đậm cho chữ, thanh điều hướng và hành động có tính quyết định.
- Dùng card viền mảnh cho nội dung trung tính; dùng card nền màu cho nội dung nổi bật hoặc cần phân nhóm.
- Không lạm dụng bóng đổ. Sự phân cấp đến từ màu nền, viền, khoảng cách, typography và bo góc.
- Layout phải ổn định trên điện thoại nhỏ; không để số tiền hoặc nhãn tiếng Việt bị cắt.
- Mọi text tiếng Việt trong app phải có dấu đầy đủ và dùng `stringResource` khi ổn định.

## Màu Sắc

### Token Chính

| Token | Hex | Vai trò |
| --- | --- | --- |
| `SnapCream` | `#FFF9E3` | Nền app chính, thay cho background sáng. |
| `SnapCreamSurface` | `#FFF7E5` | Nền form/card lớn, gần với background nhưng tách lớp nhẹ. |
| `SnapSoftYellow` | `#F6ECC9` | Nền icon holder, chart card, vùng phụ nhẹ. |
| `SnapBorder` | `#C6BFA2` | Viền control, card trung tính, icon button. |
| `SnapBorderSoft` | `#E1DBCA` | Viền form container hoặc divider nhẹ. |
| `SnapNavy` | `#081226` | Chữ chính, bottom bar, nút quyết định. |
| `SnapSlate` | `#435875` | Chữ phụ, mô tả, thời gian, nhãn thứ cấp. |
| `SnapCoral` | `#EA7A53` | Primary action, selected nav, banner số dư, splash. |
| `SnapCoralSoft` | `#FFEAE2` | Text phụ trên nền coral hoặc trạng thái nhẹ. |
| `SnapBlue` | `#99B7DD` | Card thông tin tông xanh, lịch sử/giao dịch. |
| `SnapMint` | `#8BCBB8` | Card tích cực, ngân sách ổn, khoản thu. |
| `SnapYellow` | `#F7D44C` | Card cảnh báo nhẹ, mục sắp đến, hóa đơn cần chú ý. |
| `SnapWhite` | `#FFFFFF` | CTA trên splash, icon tile trên nền màu. |

### Ánh Xạ Material 3

Cho theme sáng mục tiêu:

- `primary = SnapCoral`
- `onPrimary = SnapWhite`
- `primaryContainer = SnapCoralSoft`
- `onPrimaryContainer = SnapNavy`
- `secondary = SnapMint`
- `tertiary = SnapYellow`
- `background = SnapCream`
- `onBackground = SnapNavy`
- `surface = SnapCream`
- `surfaceVariant = SnapCreamSurface`
- `surfaceContainer = SnapCreamSurface`
- `surfaceContainerHigh = SnapSoftYellow`
- `outline = SnapBorder`
- `outlineVariant = SnapBorderSoft`
- `error`: dùng màu đỏ Material 3 hoặc một token riêng có độ tương phản tốt trên nền kem.

Nếu vẫn cần dark theme cho giai đoạn chuyển tiếp, giữ dark theme hiện tại nhưng không dùng làm hướng thị giác chính của các màn hình mới.

### Quy Tắc Sử Dụng Màu

- Nền toàn màn hình dùng `SnapCream`.
- Card trung tính dùng nền trong suốt/nền `SnapCream` với viền `SnapBorder`.
- Card có nội dung nổi bật dùng `SnapBlue`, `SnapMint`, `SnapYellow` hoặc `SnapCoral`.
- Text chính luôn dùng `SnapNavy`; text phụ dùng `SnapSlate`.
- Trên nền `SnapCoral`, text chính dùng trắng; text phụ dùng `SnapCoralSoft`.
- Trên nền màu nhạt (`SnapBlue`, `SnapMint`, `SnapYellow`), vẫn dùng `SnapNavy` và `SnapSlate`.
- Không chỉ dựa vào màu để biểu thị thu/chi; luôn kèm dấu `+`/`-`, nhãn hoặc icon.

## Typography

Figma dùng `Plus Jakarta Sans` với trọng lượng `Medium`, `SemiBold`, `Bold`. Trong Android:

- Ưu tiên thêm `Plus Jakarta Sans` vào `res/font` nếu có license và asset phù hợp.
- Nếu chưa có font, dùng `FontFamily.Default` nhưng giữ đúng scale, weight và line height.
- Không dùng tracking âm trong Compose. Figma có letter spacing âm, nhưng quy tắc repo yêu cầu letter spacing `0.sp`; giữ cảm giác bằng weight và line height.

### Type Scale

| Vai trò | Size | Weight | Line height | Dùng cho |
| --- | ---: | --- | ---: | --- |
| `displayLarge` | 40sp | Bold | 44sp | Splash headline ngắn. |
| `headlineLarge` | 36sp | Bold | 40sp | Số dư/tổng tiền nổi bật. |
| `headlineMedium` | 24sp | Bold | 30sp | Tiêu đề auth, tên app, tiêu đề màn phụ. |
| `titleLarge` | 20sp | Bold | 26sp | Section title, app bar title, tên người dùng. |
| `titleMedium` | 18sp | Bold | 24sp | Tên giao dịch, tên ví, số tiền trong list/card. |
| `bodyLarge` | 16sp | Medium | 22sp | Mô tả, input text, thông tin phụ quan trọng. |
| `labelLarge` | 16sp | SemiBold | 20sp | Nút phụ, label form, chip lớn. |
| `labelMedium` | 14sp | SemiBold | 18sp | Thời gian, subtitle, nhãn phụ trong list. |
| `labelSmall` | 12sp | SemiBold | 16sp | Trục biểu đồ, tooltip, metadata nhỏ. |

Quy tắc:

- Số tiền lớn phải nổi bật bằng weight và size, không dùng nhiều màu cùng lúc.
- Các list item dùng `titleMedium` cho tên và số tiền, `labelMedium` cho ngày/chu kỳ/ví.
- Label form dùng `labelLarge`; placeholder dùng `bodyLarge` màu `SnapSlate`.
- Hạn chế viết hoa toàn bộ; chỉ dùng cho nhãn thương hiệu ngắn như tagline.

## Spacing Và Layout

Figma tham chiếu màn hình 440 x 956, nội dung chính rộng 408 với margin ngang 16.

### Token Khoảng Cách

| Token | Giá trị | Dùng cho |
| --- | ---: | --- |
| `space2` | 2dp | Chỉnh vi mô, divider nội bộ. |
| `space4` | 4dp | Icon/text nhỏ. |
| `space8` | 8dp | Avatar + text, control compact. |
| `space10` | 10dp | Khoảng cách text trong item. |
| `space12` | 12dp | Section nội bộ, form helper. |
| `space14` | 14dp | Padding card nhỏ, khoảng cách item ngang. |
| `space16` | 16dp | Page margin, khoảng cách chuẩn. |
| `space18` | 18dp | Khoảng cách list item theo Figma. |
| `space20` | 20dp | Padding card/banner. |
| `space24` | 24dp | Padding form container. |
| `space30` | 30dp | Auth section gap. |
| `space32` | 32dp | Khoảng cách giữa nhóm lớn. |
| `space40` | 40dp | Padding pill CTA/bottom bar. |
| `space80` | 80dp | Khoảng cách lớn trên màn auth/splash. |

### Layout Màn Hình

- Root screen dùng `Box` hoặc scaffold với nền `SnapCream`.
- Nội dung chính đặt trong `Column` có `Modifier.padding(horizontal = 16.dp)`.
- Với màn có status bar, dùng `WindowInsets.statusBars`/`safeDrawing`; không hardcode 66dp nếu Android inset khác.
- Khoảng cách từ status bar tới header khoảng 20dp trên điện thoại compact.
- Mỗi section là `Column` với gap 18dp; các section lớn cách nhau 32dp.
- Với danh sách dọc, item cách nhau 18-20dp.
- Với carousel ngang, card rộng khoảng 180dp, cao 124dp, gap 14dp.
- Tránh card lồng card. Form container là một khung lớn; input là control bên trong, không thêm card trang trí dư.

## Shape Và Border

| Component | Shape |
| --- | --- |
| App frame / preview mock | 30dp, chỉ dùng trong preview/mockup, không áp lên root Android thật. |
| Hero balance banner | Bo góc bất đối xứng: topStart 0dp, topEnd 20dp, bottomEnd 0dp, bottomStart 20dp. |
| Colored transaction/subscription banner | Bo góc bất đối xứng giống hero banner. |
| Neutral list card | 16dp. |
| Form container | 16dp. |
| Text field | 14dp. |
| Icon tile | 10dp. |
| Circular icon button | Circle/1000dp, size 50dp. |
| Small pill/chip | 40dp. |
| Bottom navigation pill | 10000dp hoặc `CircleShape`/`RoundedCornerShape(percent = 50)`. |
| Primary CTA pill | 14dp trong form, 40dp hoặc full pill trên splash/detail. |

Viền:

- Card trung tính: `1.dp` `SnapBorder`.
- Form container: `1.dp` `SnapBorderSoft`.
- Input: `1.dp` `SnapBorder`.
- Nút phụ pill: `1.dp` `SnapBorder` hoặc `SnapNavy` trên nền màu.
- Nút/icon trên nền sáng: viền `SnapBorder`, nền trong suốt.

## Component System

### App Shell

`SnapScaffold` nên bao gồm:

- Nền `SnapCream`.
- Nội dung có padding ngang 16dp.
- Slot top bar cho màn chính hoặc màn chi tiết.
- Slot bottom bar nổi, chỉ hiện ở các tab chính.
- Fade nền ở đáy khi bottom bar phủ lên danh sách: gradient từ trong suốt tới `SnapCream`, cao khoảng 120-190dp, pointer/input không chặn item nếu không cần.

### Top Bar

Có hai biến thể:

- Home top bar:
  - Avatar 50dp.
  - Tên người dùng `titleLarge`.
  - Nút thêm 50dp hình tròn ở bên phải, icon 24dp.
- Detail top bar:
  - Nút back 50dp hình tròn bên trái.
  - Tiêu đề giữa `titleLarge`.
  - Nút more 50dp hình tròn bên phải.

Quy tắc:

- Icon button luôn có `contentDescription`.
- Không dùng text button cho back/more/add khi đã có icon rõ nghĩa.
- Tiêu đề giữa phải ellipsize nếu quá dài, không đẩy icon ra khỏi màn hình.

### Bottom Navigation

Figma dùng bottom bar nổi:

- Width: full content width, margin ngang 16dp.
- Height: 70dp.
- Nền: `SnapNavy`.
- Shape: full pill.
- Padding ngang: 40dp; vertical: 20dp.
- Icon: 28-30dp.
- Indicator active: circle 46dp màu `SnapCoral`.
- Không hiển thị label trong bar compact; dùng `contentDescription` và semantics.

Áp dụng cho SnapChi với 5 tab mục tiêu:

- `Trang chủ`: tổng quan, feed giao dịch.
- `Thống kê`: biểu đồ, insight.
- `Tài khoản`: ví, chuyển khoản, khoản vay, đầu tư.
- `Ngân sách`: ngân sách, cảnh báo, định kỳ.
- `Cá nhân`: hồ sơ, cài đặt.

Capture/scan biên lai không nên là tab cố định. Dùng FAB/nút `+` ở Home hoặc action trong top bar để mở capture flow/modal.

### Primary CTA

- Màu nền `SnapCoral`, text trắng bold 16sp.
- Form CTA: height khoảng 54dp, radius 14dp.
- Splash CTA: height 60dp, radius full pill, nền trắng, text `SnapNavy`.
- Trạng thái loading: disable click, giảm alpha hoặc dùng progress indicator nhỏ.
- Trạng thái disabled: nền `SnapBorderSoft`, text `SnapSlate`.

### Secondary Button / Pill

- Border `SnapBorder` trên nền sáng, text `SnapNavy`.
- Padding ngang 14dp, dọc 10dp.
- Radius 40dp.
- Dùng cho `Xem tất cả`, filter, lựa chọn phụ.

Trên card màu, secondary action dùng border `SnapNavy`, text `SnapNavy`, nền trong suốt.

### Text Field

- Container nền `SnapCream`.
- Border `SnapBorder`.
- Radius 14dp.
- Padding ngang 14dp, dọc 18dp.
- Label ngoài field, `labelLarge`, màu `SnapNavy`.
- Placeholder màu `SnapSlate`, weight SemiBold hoặc Medium.
- Error text đặt dưới field, dùng màu error và không làm layout nhảy quá mạnh.
- Không đặt repository call hoặc validation backend trực tiếp trong composable.

### Balance / Summary Banner

Dùng cho tổng số dư, tháng hiện tại, ví chính hoặc thống kê lớn.

- Nền `SnapCoral`.
- Padding 20dp ngang, 26dp dọc.
- Radius bất đối xứng 20dp.
- Label 20sp SemiBold trắng.
- Amount 36sp Bold trắng.
- Metadata bên phải 20sp SemiBold trắng.
- Với nhiều tiền tệ, giữ alignment theo baseline và tránh auto-resize quá nhỏ; ưu tiên format gọn.

### Neutral List Item

Dùng cho ví, giao dịch, subscription-like row, danh mục.

- Height tối thiểu 85-96dp.
- Padding ngang 14dp, dọc 20dp.
- Border `SnapBorder`, radius 16dp.
- Left: icon tile 56dp, radius 10dp, nền `SnapSoftYellow`.
- Middle: title `titleMedium`, subtitle `labelMedium`.
- Right: amount `titleMedium`, metadata `labelMedium`, text align end.
- Nội dung giữa dùng weight còn lại và ellipsis một dòng nếu quá dài.

### Colored List Banner

Dùng khi cần nhấn mạnh một giao dịch, insight, hóa đơn sắp tới hoặc nhóm ngân sách.

- Nền luân phiên theo ngữ cảnh: `SnapBlue`, `SnapMint`, `SnapYellow`, `SnapCoral`.
- Padding 20dp.
- Radius bất đối xứng 20dp.
- Icon tile 56dp, nền trắng 30% alpha hoặc trắng trên card mint.
- Text chính `SnapNavy`; text phụ `SnapSlate`.
- Không dùng quá nhiều card màu liên tiếp nếu màn hình có nhiều dữ liệu tài chính; xen kẽ card trung tính để giảm nhiễu.

### Expanded Detail Card

Dùng cho giao dịch/subscription/ngân sách được mở rộng.

- Header giống list banner.
- Phần detail là các dòng label/value, gap 24dp.
- Label `SnapSlate`, value `SnapNavy`.
- Action phụ dạng pill nhỏ bên phải, width khoảng 80dp.
- Destructive action hoặc action quyết định dùng pill full width nền `SnapNavy`, text trắng.
- Với thao tác xóa/hủy giao dịch, cần dialog xác nhận riêng; không thực hiện ngay từ card.

### Chart Card

Dùng cho thống kê tháng, chi tiêu theo ngày, ngân sách.

- Container nền `SnapSoftYellow`, radius 16dp, cao khoảng 260dp cho biểu đồ lớn.
- Axis label 12sp SemiBold, màu `SnapSlate`.
- Grid line dùng `SnapBorderSoft` hoặc `SnapBorder` alpha thấp, dạng dashed nếu vẽ custom.
- Bar width khoảng 12dp, radius 6dp.
- Bar thường màu `SnapNavy`; bar đang chọn hoặc điểm nổi bật màu `SnapCoral`.
- Tooltip nhỏ nền trắng, text `SnapCoral`, 12sp Bold.
- Chart phải có mô tả truy cập được: tổng quan bằng text/semantics, không chỉ là hình.

### Icon Và Asset

- Figma dùng icon outline kiểu Vuesax. Trong Android, ưu tiên vector drawable hiện có hoặc Material Symbols/Compose Icons nếu đã có dependency.
- Kích thước icon chính: 24dp trong button, 30dp trong bottom nav, 36dp trong tile 56dp.
- Icon tile nền sáng giúp logo/danh mục dễ nhận diện.
- Với receipt/camera/category, giữ hình rõ nghĩa hơn là trang trí abstract.
- Tất cả icon button cần `contentDescription` tiếng Việt có dấu.

## Navigation Và Screen Patterns

### Splash

Mục tiêu:

- Giới thiệu app nhanh, không biến thành landing page dài.
- Nền `SnapCoral`.
- Graphic hình học hoặc ảnh minh họa tài chính/camera chiếm nửa trên màn hình.
- Headline trắng 40sp Bold, ví dụ `Rõ ràng từng khoản chi`.
- Subtitle `SnapCoralSoft` 20sp Medium.
- CTA pill trắng full width: `Bắt đầu`.

Quy tắc:

- Không hiển thị quá nhiều mô tả.
- CTA nằm gần đáy nhưng không chạm navigation/home indicator.
- Nếu dùng hình minh họa, đảm bảo không che headline và không tạo cảm giác quảng cáo quá mức.

### Auth

Pattern:

- Logo nhỏ ở giữa phía trên: tile cam 64dp + tên app.
- Header text ở giữa.
- Form container `SnapCreamSurface`, viền `SnapBorderSoft`, radius 16dp.
- Field label rõ ràng, placeholder ngắn.
- CTA cam full width.
- Link phụ màu `SnapCoral`.

Nội dung SnapChi gợi ý:

- Tiêu đề: `Chào mừng trở lại`
- Subtitle: `Đăng nhập để tiếp tục quản lý chi tiêu`
- Field: `Email`, `Mật khẩu`
- CTA: `Đăng nhập`
- Link: `Chưa có tài khoản? Tạo tài khoản`

### Home

Pattern:

- Header avatar + tên người dùng + nút thêm.
- Banner tổng số dư hoặc tháng hiện tại.
- Section `Sắp tới` cho hóa đơn, ngân sách gần chạm ngưỡng hoặc giao dịch định kỳ.
- Section `Giao dịch gần đây` cho feed.
- Bottom navigation nổi.

Áp dụng cho SnapChi:

- Banner có thể hiển thị `Số dư`, `Chi tháng này`, hoặc ví mặc định.
- Horizontal cards dùng cho khoản sắp tới/ngân sách.
- List banners dùng cho giao dịch gần đây, với màu theo loại hoặc danh mục.

### Transactions / Capture Result

Pattern từ list/detail:

- Header detail với back và more.
- Form hoặc detail card rõ ràng.
- Các trường: số tiền, loại, danh mục, ví, ngày, ghi chú, ảnh biên lai.
- Nút lưu chính màu `SnapCoral`.
- Nút xóa/hủy hoặc hành động nguy hiểm cần xác nhận, dùng `SnapNavy` hoặc error theo ngữ cảnh.

Không để camera/capture chiếm bottom tab. Capture mở từ CTA hoặc FAB và quay lại màn trước sau khi lưu.

### Wallets / Accounts

Pattern từ `Subscriptions`:

- List card trung tính cho ví/tài khoản.
- Expanded card cho ví đang chọn: số dư, loại ví, hành động `Quản lý`, `Chuyển tiền`, `Lưu thay đổi`.
- Card màu mint/blue dùng cho tài khoản chính hoặc tổng tài sản.

### Budgets

Pattern:

- Card màu cho ngân sách nổi bật.
- List item trung tính cho ngân sách thường.
- Dùng progress, số tiền đã chi/còn lại, thời gian còn lại.
- Màu cảnh báo dùng `SnapYellow`; vượt ngân sách dùng error rõ ràng, không chỉ đổi màu.

### Statistics

Pattern từ `Monthly Insights`:

- Top bar detail hoặc tab `Thống kê`.
- Section chart đầu màn.
- Summary row ngay dưới chart.
- History/insight list dưới.

Áp dụng:

- Biểu đồ chi tiêu theo ngày/tuần/tháng.
- Bar selected màu `SnapCoral`.
- Tổng chi, tổng thu, chênh lệch phải có format tiền chính xác và dấu rõ ràng.

### Profile / Settings

Pattern:

- Card trung tính cho nhóm cài đặt.
- Icon tile 50-56dp.
- List item có title/subtitle.
- Toggle/selector dùng Material 3 nhưng giữ palette và shape.

## State Patterns

### Loading

- Nút submit hiển thị progress nhỏ hoặc đổi text sang trạng thái đang xử lý.
- Danh sách dùng skeleton/card placeholder có nền `SnapSoftYellow` alpha nhẹ.
- Không làm biến mất bottom navigation khi chỉ loading nội dung tab.

### Empty

- Dùng card trung tính hoặc vùng trống không viền.
- Text chính ngắn, text phụ rõ hành động tiếp theo.
- CTA chính nếu có hành động tạo mới: `Thêm giao dịch`, `Quét biên lai`, `Tạo ví`.

### Error

- Hiển thị thông báo thân thiện, không lộ chi tiết backend/Supabase.
- Với lỗi mạng, có nút thử lại.
- Với lỗi validation, gắn lỗi gần field.

### Success

- Sau khi lưu giao dịch, quay về màn phù hợp và cập nhật feed.
- Có thể dùng snackbar Material 3, màu nền `SnapNavy`, text trắng.

### Disabled

- Giữ layout không đổi.
- Nút disabled giảm tương phản nhưng vẫn đọc được.
- Không chỉ disable bằng alpha quá thấp.

## Accessibility

- Mọi icon button có `contentDescription` tiếng Việt, ví dụ `Quay lại`, `Thêm giao dịch`, `Mở tùy chọn`.
- Amount phải đọc được bằng TalkBack: thêm mô tả như `Chi 120.000 đồng` thay vì chỉ `-120000`.
- Chart cần semantic summary.
- Touch target tối thiểu 48dp.
- Contrast text trên nền màu phải đạt mức đọc được; ưu tiên `SnapNavy` trên card màu nhạt và trắng trên `SnapCoral`/`SnapNavy`.
- Không dùng animation hoặc gradient làm giảm khả năng đọc dữ liệu.

## Compose Implementation Notes

### Token Đề Xuất

Tạo hoặc mở rộng `ui/theme/Color.kt`:

```kotlin
val SnapCream = Color(0xFFFFF9E3)
val SnapCreamSurface = Color(0xFFFFF7E5)
val SnapSoftYellow = Color(0xFFF6ECC9)
val SnapBorder = Color(0xFFC6BFA2)
val SnapBorderSoft = Color(0xFFE1DBCA)
val SnapNavy = Color(0xFF081226)
val SnapSlate = Color(0xFF435875)
val SnapCoral = Color(0xFFEA7A53)
val SnapCoralSoft = Color(0xFFFFEAE2)
val SnapBlue = Color(0xFF99B7DD)
val SnapMint = Color(0xFF8BCBB8)
val SnapYellow = Color(0xFFF7D44C)
```

Tạo spacing/radius object nếu dự án bắt đầu dùng nhiều màn mới:

```kotlin
object SnapSpacing {
    val screen = 16.dp
    val section = 32.dp
    val item = 18.dp
    val card = 20.dp
}

object SnapRadius {
    val card = 16.dp
    val banner = 20.dp
    val input = 14.dp
    val iconTile = 10.dp
    val pill = 40.dp
}
```

### Component Nên Có

- `SnapScaffold`
- `SnapTopBar`
- `SnapBottomBar`
- `SnapIconButton`
- `SnapPrimaryButton`
- `SnapSecondaryPill`
- `SnapTextField`
- `SnapSummaryBanner`
- `SnapSectionHeader`
- `SnapListItem`
- `SnapColoredListBanner`
- `SnapExpandedDetailCard`
- `SnapChartCard`
- `SnapEmptyState`
- `SnapErrorState`
- `SnapLoadingState`

Khi tạo hoặc thay đổi đáng kể composable, thêm `@Preview` nhẹ với dữ liệu mẫu. Preview không được phụ thuộc ViewModel, Supabase, CameraX hoặc Android runtime-only dependency.

## Nội Dung Và Ngôn Ngữ

- App hiển thị tiếng Việt có dấu.
- Dùng thuật ngữ nhất quán:
  - `Trang chủ`
  - `Giao dịch`
  - `Quét biên lai`
  - `Ví`
  - `Tài khoản`
  - `Ngân sách`
  - `Thống kê`
  - `Cá nhân`
  - `Cài đặt`
  - `Thu nhập`
  - `Chi tiêu`
  - `Chuyển khoản`
  - `Khoản vay`
  - `Đầu tư`
- Không dùng text tiếng Anh từ Figma trong UI cuối, trừ thương hiệu hoặc dữ liệu mẫu bắt buộc.
- Format tiền theo locale/người dùng; tránh float, dùng model `Money`.

## Quy Tắc Áp Dụng Cho Feature Mới

1. Bắt đầu từ màn hình thực dụng, không tạo landing page.
2. Dùng `SnapCream` làm nền, `SnapNavy` làm chữ chính.
3. Mỗi màn có một primary action rõ ràng, thường là màu `SnapCoral`.
4. Dữ liệu tài chính quan trọng phải nằm trong vùng dễ quét ở nửa trên màn hình.
5. Card/list phải có cấu trúc nhất quán: icon, title/subtitle, amount/status.
6. Bottom navigation chỉ dành cho vùng chính; capture là action nổi hoặc modal.
7. Không thêm dependency UI lớn chỉ để tái tạo một component đơn giản.
8. Luôn kiểm tra màn hình nhỏ để tránh cắt nhãn tiếng Việt và số tiền dài.
9. Với thay đổi UI đáng kể, chạy ít nhất `.\gradlew.bat test`; nếu đổi Compose/theme/navigation, chạy thêm `.\gradlew.bat assembleDebug`.

