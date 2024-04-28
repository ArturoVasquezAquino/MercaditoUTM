package com.example.mercadoutm

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class NuevoProducto : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var descripcionEditText: EditText
    private lateinit var cantidadEditText: EditText
    private lateinit var precioEditText: EditText
    private lateinit var selectImageButton: ImageButton
    private lateinit var selectInternalImageButton: ImageButton
    private lateinit var guardarButton: Button
    private lateinit var imagenProducto: ImageView
    private var selectedImageUri: Uri? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 124
    private val CAMERA_CAPTURE_REQUEST_CODE = 125
    private val STORAGE_PERMISSION_REQUEST_CODE = 126
    private val GALLERY_REQUEST_CODE = 127

    private val storageRef = FirebaseStorage.getInstance().reference
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("productos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_producto)

        nombreEditText = findViewById(R.id.nombreEditText)
        descripcionEditText = findViewById(R.id.descripcionEditText)
        cantidadEditText = findViewById(R.id.cantidadEditText)
        precioEditText = findViewById(R.id.precioEditText)
        selectImageButton = findViewById(R.id.selectImageButton)
        selectInternalImageButton = findViewById(R.id.selectLocalIButton)
        guardarButton = findViewById(R.id.guardarButton)
        imagenProducto = findViewById(R.id.imagePreview)

        selectImageButton.setOnClickListener {
            requestCameraPermission()
        }

        guardarButton.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val descripcion = descripcionEditText.text.toString()
            val cantidad= cantidadEditText.text.toString()
            val precio = precioEditText.text.toString()

            if (selectedImageUri != null) {
                guardarProducto(nombre, descripcion, cantidad, precio, selectedImageUri!!)
            } else {
                Toast.makeText(this, "Por favor, selecciona una imagen", Toast.LENGTH_SHORT).show()
            }
        }
        selectInternalImageButton.setOnClickListener {
            openGalleryWithoutPermission()
        }

    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            // Guardar la imagen en la galería
            val savedUri = saveImageToGallery(imageBitmap)
            if (savedUri != null) {
                // La imagen se guardó correctamente, ahora puedes usar savedUri para mostrarla o subirla a Firebase Storage
                selectedImageUri = savedUri
                imagenProducto.setImageBitmap(imageBitmap)
            } else {
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        // Obtener la URI de la imagen guardada
        val contentResolver = contentResolver
        val imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        // Insertar la imagen en la galería
        val uri = contentResolver.insert(imageUri, contentValues)
        uri?.let { imageUri ->
            try {
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                return imageUri
            } catch (e: IOException) {
                Log.e("SaveImage", "Error al guardar la imagen", e)
            }
        }
        return null
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun guardarProducto(nombre: String, cantidad: String, descripcion: String, precio: String, imageUri: Uri?) {
        val key = myRef.push().key
        if (key != null) {

            val producto = Product(key, nombre, cantidad, descripcion, precio, "")

            myRef.child(key).setValue(producto)

            imageUri?.let { uri ->
                val imageRef = storageRef.child("images/${key}.jpg")
                imageRef.putFile(uri)
                    .addOnSuccessListener { _ ->
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Guardar solo la URL de la imagen en la base de datos
                            myRef.child(key).child("imagenUrl").setValue(downloadUri.toString())
                            Toast.makeText(this, "Imagen subida con éxito", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirebaseStorage", "Error al subir la imagen", exception)
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
            }

        } else {
            // Si key es null, se asigna una URL predeterminada para la imagen
            val defaultImageUrl = "gs://productosutm.appspot.com/default-image-5-1.jpg"
            myRef.child(key ?: "").child("imagenUrl").setValue(defaultImageUrl)
            Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido, abrir la galería
                openGallery()
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openGalleryWithoutPermission() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            selectedImageUri = imageUri
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imagenProducto.setImageBitmap(bitmap)
                imagenProducto.setImageBitmap(bitmap) // Mostrar la vista previa en el ImageView

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }



}
