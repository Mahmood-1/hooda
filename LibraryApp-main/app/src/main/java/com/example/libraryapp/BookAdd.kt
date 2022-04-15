package com.example.libraryapp

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class BookAdd : AppCompatActivity() {
    private lateinit var addnamebook: EditText
    private lateinit var addauther: EditText
    private lateinit var addbook: Button
    private lateinit var uploadbook: ImageView
    private lateinit var addrating: RatingBar
    private lateinit var addyear: EditText
    private lateinit var addprice: EditText
    private var db: FirebaseFirestore? = null
    private val Pick_IMAGE_REQUEST = 111
    var imageURI: Uri? = null
    private var progressDialog: ProgressDialog? = null
    private var x = 0f
    private var y = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)
        uploadbook = findViewById(R.id.uploadBookCover)
        addrating = findViewById(R.id.addRatingBar)
        addnamebook = findViewById(R.id.addNameBook)
        addauther = findViewById(R.id.addNameAuthor)
        addbook = findViewById(R.id.addBook)
        addyear = findViewById(R.id.addLaunchYear)
        addprice = findViewById(R.id.addPrice)
        db = Firebase.firestore
        val id = System.currentTimeMillis()
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("book img")

        addyear.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val day = currentDate.get(Calendar.DAY_OF_MONTH)
            val month = currentDate.get(Calendar.MONTH)
            val year = currentDate.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this, { _, y, m, d -> addyear.setText("$y / ${m + 1} / $d") }, year, month, day
            )
            picker.show()
        }

        uploadbook.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "image/*"

            startActivityForResult(intent, Pick_IMAGE_REQUEST)
            uploadbook.setBackgroundResource(0)
            y = 1
        }
        addrating.setOnRatingBarChangeListener { _, xx, _ ->
            x = xx
        }
        addbook.setOnClickListener {
            if (addnamebook.text.isEmpty() || addauther.text.isEmpty()
                || addyear.text.isEmpty() || addprice.text.isEmpty() || y == 0
            ) {
                Toast.makeText(this, "fill ", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("add book")
                builder.setMessage("do you add book?")
                builder.setPositiveButton("true") { _, _ ->

                    showDialog()
                    val bitmap = (uploadbook.drawable as BitmapDrawable).bitmap
                    val arr = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, arr)
                    val data = arr.toByteArray()
                    val childRef = imageRef.child(System.currentTimeMillis().toString() + ".png")
                    var uploadTask = childRef.putBytes(data)
                    uploadTask.addOnFailureListener { exception ->
                        hideDialog()
                    }.addOnSuccessListener {
                        childRef.downloadUrl.addOnSuccessListener { uri ->
                            addBook(
                                id.toString(),
                                addnamebook.text.toString(),
                                addprice.text.toString(),
                                uri.toString(),x.toString()
                                addauther.text.toString(),
                                addyear.text.toString(),

                            )
                            hideDialog()
                            Toast.makeText(this, "added sucsess", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ViewBooks::class.java))
                        }


                    }
                }
                builder.setNegativeButton("No") { d, _ ->
                    d.dismiss()
                }
                builder.create().show()
            }
        }
    }

    private fun addBook(
        id: String,
        nameBook: String,
        nameAuthor: String,
        launchYear: String,
        price: String,
        image: String,
        bookReview: String
    ) {
        val book = hashMapOf(
            "id" to id,
            "Name_Book" to nameBook,
            "Name_Author" to nameAuthor,
            "Launch_Year" to launchYear,
            "Price_Book" to price,
            "Image_Book" to image,
            "Book_Review" to bookReview
        )db!!.collection("Books").add(book)
    }
    private fun showDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Uploading ...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }
    private fun hideDialog() {
        if (progressDialog!!.isShowing)
            progressDialog!!.dismiss()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Pick_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageURI = data!!.data
            uploadbook.setImageURI(imageURI)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Back -> startActivity(Intent(this, ViewBooks::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}
