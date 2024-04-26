package com.example.mercadoutm
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.room.Room
import com.example.mercadoutm.Apartados
import com.example.mercadoutm.R
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
                val view = inflater.inflate(R.layout.item_product, parent, false)
                MainViewHolder(view)
            }
            VIEW_TYPE_APARTADOS -> {
                val view = inflater.inflate(R.layout.ventana_apartados, parent, false)
                ApartadosViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = products[position]

        when (currentPage) {
            1 -> {
                holder as MainViewHolder
                holder.productName.text = product.name
                holder.productPrice.text = product.price
                holder.productImage.setImageResource(product.image)
                holder.agregarButton.visibility = View.VISIBLE
                holder.eliminarButton.visibility = View.GONE
                holder.agregarButton.setOnClickListener {
                    listener.onApartarClick(product)
                }
                holder.comprarButton.setOnClickListener {
                    listener.onComprarClick(product)
                }
            }
            2 -> {
                holder as ApartadosViewHolder
                holder.productName.text = product.name
                holder.productPrice.text = product.price
                holder.productImage.setImageResource(product.image)
                holder.agregarButton.visibility = View.GONE
                holder.eliminarButton.visibility = View.VISIBLE
                holder.eliminarButton.setOnClickListener {
                    listener.onEliminarClick(product)
                }
            }
            3 -> {
                holder as RealizarCompraViewHolder

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
        val productName: TextView = itemView.findViewById(R.id.textViewProductName)
        val productPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        val productImage: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val agregarButton: Button = itemView.findViewById(R.id.apartarButton)
        val comprarButton: Button = itemView.findViewById(R.id.comprarButton)
        val eliminarButton: Button = itemView.findViewById(R.id.eliminarButton)
    }

    inner class ApartadosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.textViewProductName)
        val productPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        val productImage: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val agregarButton: Button = itemView.findViewById(R.id.apartarButton)
        val eliminarButton: Button = itemView.findViewById(R.id.eliminarButton)
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
data class Product(val name: String, val price: String, val image: Int, var reservado: Boolean = false) : Serializable {
}

// Actividad principal
class MainActivity : AppCompatActivity(), ProductAdapter.OnClickListener {
    /*val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "producto"
    ).build()*/

    private lateinit var adapter: ProductAdapter
    private var products = mutableListOf<Product>()
    private val apartadosList = mutableListOf<Product>()
    var currentPage = 1
    val APARTADOS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Lista inicial de productos
        products = mutableListOf(
            Product("Casa 1", "$10,000", R.drawable.product1),
            Product("Casa 2", "$150,000", R.drawable.product2),
            Product("Playera Nike", "$400", R.drawable.nike),
            Product("PlayStation 4", "$3000", R.drawable.ps4),
            Product("Xbox One", "$9000", R.drawable.xbox_jog),
            Product("Iphone 1", "$10", R.drawable.resource),
            Product("Oreos", "$150", R.drawable.oreo),
            Product("Red Dead Redemption2", "$100000", R.drawable.rdr2)
        )

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

        // Configurar el OnClickListener para el botón "Apartados"
        val apartadosButton: ImageButton = findViewById(R.id.apartadosButton)
        apartadosButton.setOnClickListener {
            currentPage = 2 // Cambiar a la página de apartados
            val intent = Intent(this, Apartados::class.java)
            intent.putExtra("apartadosList", ArrayList(apartadosList))
            startActivityForResult(intent, APARTADOS_REQUEST_CODE)
        }
    }

    override fun onApartarClick(producto: Product) {
        apartadosList.add(producto)
        Toast.makeText(this, "Producto apartado: ${producto.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onComprarClick(producto: Product) {
        Toast.makeText(this, "COMPRAAAAAA: ${producto.name}", Toast.LENGTH_SHORT).show()
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
