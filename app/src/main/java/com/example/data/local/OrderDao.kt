package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CreationOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM creation_orders ORDER BY dateTimestamp DESC")
    fun getAllOrders(): Flow<List<CreationOrder>>

    @Query("SELECT * FROM creation_orders WHERE status = 'PENDING_CREATOR' ORDER BY dateTimestamp DESC")
    fun getPendingOrders(): Flow<List<CreationOrder>>

    @Query("SELECT * FROM creation_orders WHERE status = 'COMPLETED' ORDER BY dateTimestamp DESC")
    fun getCompletedOrders(): Flow<List<CreationOrder>>

    @Query("SELECT * FROM creation_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): CreationOrder?

    @Query("SELECT * FROM creation_orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderByOrderId(orderId: String): CreationOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CreationOrder): Long

    @Update
    suspend fun updateOrder(order: CreationOrder)

    @Delete
    suspend fun deleteOrder(order: CreationOrder)
}
