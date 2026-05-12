package vn.vietbevis.apkbasic.core.common

sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class MissingSession : AppError("Phiên đăng nhập không hợp lệ.")
    class Validation(message: String) : AppError(message)
    class Remote(cause: Throwable) : AppError(cause.toUserFacingRemoteMessage(), cause)
}

suspend inline fun <T> appResult(crossinline block: suspend () -> T): Result<T> =
    runCatching { block() }.recoverCatching { throwable ->
        throw when (throwable) {
            is AppError -> throwable
            else -> AppError.Remote(throwable)
        }
    }

fun Throwable.userMessage(): String = when (this) {
    is AppError -> message ?: "Đã có lỗi xảy ra."
    else -> "Đã có lỗi xảy ra. Vui lòng thử lại."
}

private fun Throwable.toUserFacingRemoteMessage(): String {
    val rawMessage = message.orEmpty()
    val normalized = rawMessage.lowercase()
    return when {
        "invalid login credentials" in normalized ||
            "invalid_grant" in normalized ||
            "400" in normalized && "token" in normalized ->
            "Email hoặc mật khẩu chưa đúng."
        "email not confirmed" in normalized || "confirm" in normalized ->
            "Tài khoản chưa xác nhận email. Vui lòng kiểm tra hộp thư rồi đăng nhập lại."
        "already registered" in normalized || "user already registered" in normalized ->
            "Email này đã được đăng ký. Vui lòng đăng nhập hoặc dùng email khác."
        "network" in normalized || "timeout" in normalized || "unable to resolve host" in normalized ->
            "Không thể kết nối mạng. Vui lòng kiểm tra Internet rồi thử lại."
        else -> "Không thể đồng bộ dữ liệu. Vui lòng thử lại."
    }
}
