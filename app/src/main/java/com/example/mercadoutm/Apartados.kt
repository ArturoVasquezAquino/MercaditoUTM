package com.example.mercadoutm


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Apartados : AppCompatActivity(), ProductAdapter.OnApartarClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noApartadosTextView: TextView
    private lateinit var adapter: ProductAdapter
    private var apartadosList = mutableListOf<Product>() //Lista de productos apartados para mostrarse en la sección

    override fun onCreate(savedInstanceState: Bundle?) { //Inicio de la actividad
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartados)

        recyclerView = findViewById(R.id.apartadosRecyclerView) //para mostrar los elementos scrolleables
        noApartadosTextView = findViewById(R.id.noApartadosTextView) //Texto para decir que no hay elementos apartados

        val productosApartados = intent.getSerializableExtra("apartadosList") as? ArrayList<Product>
        if (productosApartados != null) { //Verifica que hay productos apartados en la sección
            apartadosList.clear()
            apartadosList.addAll(productosApartados)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter(apartadosList, this, isMainPage = false)
        recyclerView.adapter = adapter

        if (apartadosList.isEmpty()) {
            noApartadosTextView.visibility = View.VISIBLE //Aqui se hace visible el texto antes importado
        } else {
            noApartadosTextView.visibility = View.GONE
        }

        val regresarButton: Button = findViewById(R.id.regresarButton) //Botón que nos permite regresar al inicio
        regresarButton.setOnClickListener {
            regresarAPrincipal()
        }
    }

    private fun regresarAPrincipal() { //Función que nos permite regresar al inicio
        val intent = Intent()
        intent.putExtra("updatedList", ArrayList(apartadosList))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    override fun onApartarClick(product: Product) { //
        apartadosList.add(product)
        adapter.notifyItemInserted(apartadosList.size - 1)
        noApartadosTextView.visibility = View.GONE
    }

    override fun onEliminarClick(producto: Product) { //Sirve para eliminar productos apartados
        apartadosList.remove(producto)
        adapter.notifyDataSetChanged()
        if (apartadosList.isEmpty()) {
            noApartadosTextView.visibility = View.VISIBLE
        }
    }


}
