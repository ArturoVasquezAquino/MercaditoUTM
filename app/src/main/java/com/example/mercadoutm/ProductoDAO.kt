package com.example.mercadoutm

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductoDAO{
    @Query("SELECT * FROM producto")
    fun getAll(): List<Producto>

    @Query("SELECT * FROM Producto WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Producto>

    @Query("SELECT * FROM producto WHERE nombre LIKE :first AND " +
            "nombre LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Producto

    @Insert
    fun insertAll(vararg producto: Producto)

    @Delete
    fun delete(producto: Producto)
}