package vn.vietbevis.apkbasic.data.sharing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.vietbevis.apkbasic.domain.model.Friend
import vn.vietbevis.apkbasic.domain.model.FriendStatus
import vn.vietbevis.apkbasic.domain.model.Group
import vn.vietbevis.apkbasic.domain.model.GroupMember
import vn.vietbevis.apkbasic.domain.model.GroupRole
import vn.vietbevis.apkbasic.domain.model.SharedTransaction

@Serializable
data class FriendDto(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("friend_user_id") val friendUserId: String,
    @SerialName("status") val status: String,
)

@Serializable
data class GroupDto(
    @SerialName("id") val id: String,
    @SerialName("owner_user_id") val ownerUserId: String,
    @SerialName("name") val name: String,
    @SerialName("icon") val icon: String? = null,
)

@Serializable
data class GroupMemberDto(
    @SerialName("id") val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("role") val role: String,
)

@Serializable
data class SharedTransactionDto(
    @SerialName("id") val id: String,
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("shared_by_user_id") val sharedByUserId: String,
    @SerialName("shared_with_user_id") val sharedWithUserId: String? = null,
    @SerialName("group_id") val groupId: String? = null,
)

fun FriendDto.toDomain(): Friend = Friend(
    id = id,
    userId = userId,
    friendUserId = friendUserId,
    status = when (status) {
        "accepted" -> FriendStatus.ACCEPTED
        "blocked" -> FriendStatus.BLOCKED
        else -> FriendStatus.PENDING
    },
)

fun Friend.toDto(): FriendDto = FriendDto(
    id = id,
    userId = userId,
    friendUserId = friendUserId,
    status = status.name.lowercase(),
)

fun GroupDto.toDomain(): Group = Group(
    id = id,
    ownerUserId = ownerUserId,
    name = name,
    icon = icon,
)

fun Group.toDto(): GroupDto = GroupDto(
    id = id,
    ownerUserId = ownerUserId,
    name = name,
    icon = icon,
)

fun GroupMemberDto.toDomain(): GroupMember = GroupMember(
    id = id,
    groupId = groupId,
    userId = userId,
    role = if (role == "owner") GroupRole.OWNER else GroupRole.MEMBER,
)

fun GroupMember.toDto(): GroupMemberDto = GroupMemberDto(
    id = id,
    groupId = groupId,
    userId = userId,
    role = role.name.lowercase(),
)

fun SharedTransactionDto.toDomain(): SharedTransaction = SharedTransaction(
    id = id,
    transactionId = transactionId,
    sharedByUserId = sharedByUserId,
    sharedWithUserId = sharedWithUserId,
    groupId = groupId,
)

fun SharedTransaction.toDto(): SharedTransactionDto = SharedTransactionDto(
    id = id,
    transactionId = transactionId,
    sharedByUserId = sharedByUserId,
    sharedWithUserId = sharedWithUserId,
    groupId = groupId,
)
