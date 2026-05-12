package vn.vietbevis.apkbasic.domain.repository

import vn.vietbevis.apkbasic.domain.model.Friend
import vn.vietbevis.apkbasic.domain.model.Group
import vn.vietbevis.apkbasic.domain.model.GroupMember
import vn.vietbevis.apkbasic.domain.model.SharedTransaction

interface SharingRepository {
    suspend fun listFriends(): Result<List<Friend>>
    suspend fun createFriend(friend: Friend): Result<Friend>
    suspend fun listGroups(): Result<List<Group>>
    suspend fun createGroup(group: Group): Result<Group>
    suspend fun listGroupMembers(groupId: String): Result<List<GroupMember>>
    suspend fun listSharedTransactions(): Result<List<SharedTransaction>>
    suspend fun shareTransaction(sharedTransaction: SharedTransaction): Result<SharedTransaction>
    suspend fun deleteSharedTransaction(sharedTransactionId: String): Result<Unit>
}
