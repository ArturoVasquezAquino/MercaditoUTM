package com.example.mercadoutm
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Producto(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "Nombre") val Nombre: String?,
    @ColumnInfo(name = "Cantidad") val Cantidad: Int?,
    @ColumnInfo(name = "Descripción") val Descripción: String?,
    @ColumnInfo(name = "Imagen") val Imagen: ByteArray?
)