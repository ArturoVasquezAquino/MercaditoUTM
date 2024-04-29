package com.example.mercadoutm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Compra : AppCompatActivity(), ProductAdapter.OnClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private var productoSeleccionado = mutableListOf<Product>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compra)

        recyclerView = findViewById(R.id.comprarRecyclerView)

        val producto = intent.getSerializableExtra("producto") as? ArrayList<Product>
        if (producto != null) {
            productoSeleccionado.clear()
            productoSeleccionado.addAll(producto)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter(productoSeleccionado, this, currentPage = 3)
        recyclerView.adapter = adapter
    }
    override fun onApartarClick(producto: Product) {
        TODO("Not yet implemented")
    }

    override fun onEliminarClick(producto: Product) {
        TODO("Not yet implemented")
    }
    override fun onComprarClick(producto: Product) {
        TODO("Not yet implemented")
    }
}
