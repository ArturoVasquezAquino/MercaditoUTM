package com.example.mercadoutm

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mercadoutm.R.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest


class CrearUsuario : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var nombreEditText: EditText
    private lateinit var direccionEditText: EditText
    private lateinit var correo: EditText
    private lateinit var contraseñaEditText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_crear_usuario)

        database = FirebaseDatabase.getInstance()
        nombreEditText = findViewById(R.id.nombreEditText)
        direccionEditText = findViewById(R.id.direccionEditText)
        correo = findViewById(R.id.email)
        contraseñaEditText = findViewById(R.id.contraseñaEditText)
        val createUserButton : Button = findViewById(R.id.createUserButton)

        createUserButton.setOnClickListener {
            val name = nombreEditText.text.toString()
            val address = direccionEditText.text.toString()
            val email = correo.text.toString()
            val password = contraseñaEditText.text.toString()
            saveUserToDatabase(name, address, email, password)
        }
    }


    private fun saveUserToDatabase(name: String, address: String, email: String, password: String) {

        val usersRef = database.getReference("users")
        val nextUidRef = usersRef.child("nextUid")

        // Verificar si el correo electrónico ya está registrado
        usersRef.child("usersList").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // El correo electrónico ya está registrado, mostrar una alerta
                    val alertDialogBuilder = AlertDialog.Builder(this@CrearUsuario)
                    alertDialogBuilder.setTitle("Correo Electrónico Existente")
                    alertDialogBuilder.setMessage("El correo electrónico ingresado ya está registrado.")
                    alertDialogBuilder.setPositiveButton("Aceptar", null)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else {
                    // Obtener el próximo UID disponible
                    nextUidRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val nextUid = dataSnapshot.getValue(Int::class.java) ?: 1

                            // Crear un nuevo usuario con el UID y guardarlo en la base de datos
                            val newUser = User(nextUid.toString(), name, address, email, hashPassword(password))
                            val userRef = usersRef.child("usersList").child(nextUid.toString())
                            userRef.setValue(newUser)
                                .addOnSuccessListener {
                                    Log.d("FirebaseDatabase", "Usuario guardado en la base de datos")
                                    // Incrementar el próximo UID disponible
                                    nextUidRef.setValue(nextUid + 1)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirebaseDatabase", "Error al guardar usuario en la base de datos: $e")
                                }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("FirebaseDatabase", "Error al obtener el próximo UID: ${databaseError.message}")
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseDatabase", "Error al verificar correo electrónico: ${databaseError.message}")
            }
        })
    }


    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString(separator = "") { "%02x".format(it) }
    }

}
data class User(
    val uid: String = "",
    val name: String = "",
    val address: String = "",
    val email:String="",
    val password: String = ""
)