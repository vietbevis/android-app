package vn.vietbevis.apkbasic.domain.model

data class Friend(
    val id: String,
    val userId: String,
    val friendUserId: String,
    val status: FriendStatus,
)

enum class FriendStatus {
    PENDING,
    ACCEPTED,
    BLOCKED,
}

data class Group(
    val id: String,
    val ownerUserId: String,
    val name: String,
    val icon: String? = null,
)

data class GroupMember(
    val id: String,
    val groupId: String,
    val userId: String,
    val role: GroupRole,
)

enum class GroupRole {
    OWNER,
    MEMBER,
}

data class SharedTransaction(
    val id: String,
    val transactionId: String,
    val sharedByUserId: String,
    val sharedWithUserId: String? = null,
    val groupId: String? = null,
)
