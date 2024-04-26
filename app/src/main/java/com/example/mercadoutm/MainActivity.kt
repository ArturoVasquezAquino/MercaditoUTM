package com.example.mercadoutm

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mercadoutm.R.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

// Adaptador para los productos
class ProductAdapter(
    private val products: List<Product>,
    private val listener: OnClickListener,
    private val currentPage: Int)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnClickListener {
        fun onApartarClick(producto: Product)
        fun onComprarClick(producto: Product)
        fun onEliminarClick(producto: Product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_MAIN -> {
                val view = inflater.inflate(layout.item_product, parent, false)
                MainViewHolder(view)
            }
            VIEW_TYPE_APARTADOS -> {
                val view = inflater.inflate(layout.ventana_apartados, parent, false)
                ApartadosViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = products[position]

        when (currentPage) { // Muestra los elementos de la página principal
            1 -> {
                val mainHolder = holder as MainViewHolder
                mainHolder.productName.text = "Nombre: ${product.nombre}"
                mainHolder.productDesc.text = product.descripcion
                mainHolder.productCant.text = "Cantidad: ${product.cantidad}"
                mainHolder.productPrice.text = "Precio $${product.precio}"
                // Carga la imagen utilizando Glide y la URL de descarga
                Glide.with(holder.itemView.context)
                    .load(product.imagenUrl) // Utiliza la URL de descarga de la imagen
                    .into(mainHolder.productImage)
                mainHolder.agregarButton.visibility = View.VISIBLE
                mainHolder.eliminarButton.visibility = View.GONE
                mainHolder.agregarButton.setOnClickListener {
                    listener.onApartarClick(product)
                }
            }
            2 -> { // Muestra elementos de la página de apartados
                val apartadosHolder = holder as ApartadosViewHolder
                apartadosHolder.productName.text = "Nombre: ${product.nombre}"
                apartadosHolder.productDesc.text = product.descripcion
                apartadosHolder.productCant.text = "Cantidad: ${product.cantidad}"
                apartadosHolder.productPrice.text = "Precio $${product.precio}"
                // Carga la imagen utilizando Glide y la URL de descarga
                Glide.with(holder.itemView.context)
                    .load(product.imagenUrl) // Utiliza la URL de descarga de la imagen
                    .into(apartadosHolder.productImage)
                apartadosHolder.agregarButton.visibility = View.GONE
                apartadosHolder.eliminarButton.visibility = View.VISIBLE
                apartadosHolder.eliminarButton.setOnClickListener {
                    listener.onEliminarClick(product)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentPage) {
            1 -> VIEW_TYPE_MAIN
            2 -> VIEW_TYPE_APARTADOS
            3 -> VIEW_TYPE_REALIZAR_COMPRA
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { //se declaran los elementos de cada página
        val productName: TextView = itemView.findViewById(id.textViewProductName)
        val productDesc: TextView = itemView.findViewById(id.textViewProductDesc)
        val productCant: TextView = itemView.findViewById(id.textViewProductCantidad)
        val productPrice: TextView = itemView.findViewById(id.textViewProductPrice)
        val productImage: ImageView = itemView.findViewById(id.imageViewProduct)
        val agregarButton: Button = itemView.findViewById(id.apartarButton)
        val eliminarButton: Button = itemView.findViewById(id.eliminarButton)
    }

    inner class ApartadosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(id.textViewProductName)
        val productDesc: TextView = itemView.findViewById(id.textViewProductDesc)
        val productCant: TextView = itemView.findViewById(id.textViewProductCantidad)
        val productPrice: TextView = itemView.findViewById(id.textViewProductPrice)
        val productImage: ImageView = itemView.findViewById(id.imageViewProduct)
        val agregarButton: Button = itemView.findViewById(id.apartarButton)
        val eliminarButton: Button = itemView.findViewById(id.eliminarButton)
    }

    inner class RealizarCompraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // si
    }

    companion object {
        private const val VIEW_TYPE_MAIN = 1
        private const val VIEW_TYPE_APARTADOS = 2
        private const val VIEW_TYPE_REALIZAR_COMPRA = 3
    }
}

// Clase para representar un producto
data class Product(val nombre: String,  val descripcion : String,val cantidad:String, val precio: String, val imagenUrl: String, var reservado: Boolean = false) : Serializable {
}

// Actividad principal
class MainActivity : AppCompatActivity(), ProductAdapter.OnClickListener {

    // Firebase Realtime Database
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("productos")
    // Firebase Firestore
    val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductAdapter
    private var products = mutableListOf<Product>()
    private val apartadosList = mutableListOf<Product>()
    var currentPage = 1
    val APARTADOS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                products.clear()
                for (snapshot in dataSnapshot.children) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                    val cantidad = snapshot.child("cantidad").getValue(String::class.java) ?: ""
                    val descripcion = snapshot.child("descripcion").getValue(String::class.java) ?: ""
                    val precio = snapshot.child("precio").getValue(String::class.java) ?: ""
                    val imageURL = snapshot.child("imagenUrl").getValue(String::class.java) ?: ""
                    // Aquí debes obtener la referencia a la imagen, dependiendo de cómo la hayas guardado en la base de datos
                    val product = Product(nombre,descripcion,cantidad, precio, imageURL) // Reemplaza R.drawable.default_image con la referencia real a la imagen
                    products.add(product)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores al leer desde Firebase Realtime Database
            }
        })


        // Lista inicial de productos
        db.collection("productos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombre") ?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val cantidad = document.getString("cantidad") ?: ""
                    val precio = document.getString("precio") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    // Aquí debes obtener la referencia a la imagen, dependiendo de cómo la hayas guardado en la base de datos
                    val product = Product(nombre,descripcion,cantidad, precio, imageUrl) // Reemplaza R.drawable.default_image con la referencia real a la imagen
                    products.add(product)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        // Actualizar lista de productos si hay una lista actualizada al iniciar la actividad
        val updatedList = intent.getSerializableExtra("updatedList") as? List<Product>
        updatedList?.let {
            products.clear()
            products.addAll(it)
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = ProductAdapter(products, this, currentPage)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        val agregarButton: ImageButton = findViewById(R.id.agregarButton)
        agregarButton.setOnClickListener {
            val intent = Intent(this, NuevoProducto::class.java)
            startActivity(intent)
        }




    // Configurar el OnClickListener para el botón "Apartados"
        val apartadosButton: ImageButton = findViewById(id.apartadosButton)
        apartadosButton.setOnClickListener {
            currentPage = 2 // Cambiar a la página de apartados
            val intent = Intent(this, Apartados::class.java)
            intent.putExtra("apartadosList", ArrayList(apartadosList))
            startActivityForResult(intent, APARTADOS_REQUEST_CODE)
        }
    }

    override fun onApartarClick(product: Product) {
        apartadosList.add(product)
        Toast.makeText(this, "Producto apartado: ${product.nombre}", Toast.LENGTH_SHORT).show()
    }

    override fun onComprarClick(product: Product) {
        Toast.makeText(this, "COMPRAAAAAA: ${product.nombre}", Toast.LENGTH_SHORT).show()
    }

    override fun onEliminarClick(producto: Product) {
        apartadosList.remove(producto)
        adapter.notifyDataSetChanged()
        val intent = Intent(this, Apartados::class.java)
        intent.putExtra("apartadosList", ArrayList(apartadosList))
        startActivityForResult(intent, APARTADOS_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APARTADOS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val updatedList = data?.getSerializableExtra("updatedList") as? List<Product>
            updatedList?.let {
                apartadosList.clear()
                apartadosList.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun regresarAPrincipal() {
        currentPage = 1 // Cambiar a la página principal
        adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado

        // Resto del código para regresar a la actividad principal
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}
