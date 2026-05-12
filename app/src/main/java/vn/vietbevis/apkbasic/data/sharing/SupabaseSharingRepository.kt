package vn.vietbevis.apkbasic.data.sharing

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import vn.vietbevis.apkbasic.core.common.appResult
import vn.vietbevis.apkbasic.domain.model.Friend
import vn.vietbevis.apkbasic.domain.model.Group
import vn.vietbevis.apkbasic.domain.model.GroupMember
import vn.vietbevis.apkbasic.domain.model.GroupRole
import vn.vietbevis.apkbasic.domain.model.SharedTransaction
import vn.vietbevis.apkbasic.domain.repository.SharingRepository
import java.util.UUID

class SupabaseSharingRepository(
    private val client: SupabaseClient,
) : SharingRepository {
    override suspend fun listFriends(): Result<List<Friend>> = appResult {
        client.from("friends")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<FriendDto>()
            .map { it.toDomain() }
    }

    override suspend fun createFriend(friend: Friend): Result<Friend> = appResult {
        client.from("friends").insert(friend.toDto())
        readFriend(friend.id)
    }

    override suspend fun listGroups(): Result<List<Group>> = appResult {
        client.from("groups")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<GroupDto>()
            .map { it.toDomain() }
    }

    override suspend fun createGroup(group: Group): Result<Group> = appResult {
        client.from("groups").insert(group.toDto())
        client.from("group_members").insert(
            GroupMember(
                id = UUID.randomUUID().toString(),
                groupId = group.id,
                userId = group.ownerUserId,
                role = GroupRole.OWNER,
            ).toDto(),
        )
        readGroup(group.id)
    }

    override suspend fun listGroupMembers(groupId: String): Result<List<GroupMember>> = appResult {
        client.from("group_members")
            .select {
                filter { eq("group_id", groupId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<GroupMemberDto>()
            .map { it.toDomain() }
    }

    override suspend fun listSharedTransactions(): Result<List<SharedTransaction>> = appResult {
        client.from("shared_transactions")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<SharedTransactionDto>()
            .map { it.toDomain() }
    }

    override suspend fun shareTransaction(sharedTransaction: SharedTransaction): Result<SharedTransaction> = appResult {
        client.from("shared_transactions").insert(sharedTransaction.toDto())
        readSharedTransaction(sharedTransaction.id)
    }

    override suspend fun deleteSharedTransaction(sharedTransactionId: String): Result<Unit> = appResult {
        client.from("shared_transactions").delete {
            filter { eq("id", sharedTransactionId) }
        }
        Unit
    }

    private suspend fun readFriend(friendId: String): Friend =
        client.from("friends")
            .select { filter { eq("id", friendId) } }
            .decodeSingle<FriendDto>()
            .toDomain()

    private suspend fun readGroup(groupId: String): Group =
        client.from("groups")
            .select { filter { eq("id", groupId) } }
            .decodeSingle<GroupDto>()
            .toDomain()

    private suspend fun readSharedTransaction(sharedTransactionId: String): SharedTransaction =
        client.from("shared_transactions")
            .select { filter { eq("id", sharedTransactionId) } }
            .decodeSingle<SharedTransactionDto>()
            .toDomain()
}
