package com.ramitsuri.choresclient.android.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.ramitsuri.choresclient.android.model.Member
import java.time.Instant

@Entity(tableName = "Members")
class MemberEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "createdDate")
    val createdDate: Instant
) {
    constructor(member: Member): this(
        member.id,
        member.name,
        member.createdDate
    )
}

@Dao
abstract class MemberDao {
    @Query("SELECT * FROM Members WHERE id = :id")
    abstract suspend fun get(id: String): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(memberEntity: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(memberEntities: List<MemberEntity>)

    @Query("DELETE FROM Members")
    abstract suspend fun delete()

    @Transaction
    open suspend fun clearAndInsert(memberEntities: List<MemberEntity>) {
        delete()
        insert(memberEntities)
    }
}