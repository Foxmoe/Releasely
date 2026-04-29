package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class WishRecord(
    val id: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val createdAt: Long
)

class WishlistService(private val database: top.foxmoe.releasely.database.AppDatabase) {

    suspend fun getAllWishes(): List<WishRecord> = withContext(Dispatchers.IO) {
        database.wishlistQueries.getAllWishes().executeAsList().map { row ->
            WishRecord(
                id = row.id,
                title = row.title,
                description = row.description,
                isCompleted = row.is_completed == 1L,
                createdAt = row.created_at
            )
        }
    }

    suspend fun getWishById(id: String): WishRecord? = withContext(Dispatchers.IO) {
        database.wishlistQueries.getWishById(id).executeAsOneOrNull()?.let { row ->
            WishRecord(
                id = row.id,
                title = row.title,
                description = row.description,
                isCompleted = row.is_completed == 1L,
                createdAt = row.created_at
            )
        }
    }

    suspend fun insertWish(title: String, description: String?): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        database.wishlistQueries.insertWish(id, title, description)
        id
    }

    suspend fun updateWish(id: String, title: String, description: String?) = withContext(Dispatchers.IO) {
        database.wishlistQueries.updateWish(title, description, id)
    }

    suspend fun toggleWishStatus(id: String, completed: Boolean) = withContext(Dispatchers.IO) {
        if (completed) {
            database.wishlistQueries.markWishCompleted(id)
        } else {
            database.wishlistQueries.markWishIncomplete(id)
        }
    }

    suspend fun deleteWish(id: String) = withContext(Dispatchers.IO) {
        database.wishlistQueries.deleteWish(id)
    }

    suspend fun getWishCount(): Long = withContext(Dispatchers.IO) {
        database.wishlistQueries.countWishes().executeAsOne()
    }

    suspend fun getCompletedCount(): Long = withContext(Dispatchers.IO) {
        database.wishlistQueries.countCompletedWishes().executeAsOne()
    }
}
