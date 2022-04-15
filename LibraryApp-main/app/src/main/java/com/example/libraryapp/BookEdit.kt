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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class BookEdit : AppCompatActivity() {
    private lateinit var editrating: RatingBar
    private lateinit var editbookc: ImageView
    private lateinit var editbook: Button
    private lateinit var delebook: Button
    private lateinit var editname: EditText
    private lateinit var editauther: EditText
    private lateinit var edityear: EditText
    private lateinit var rditprice: EditText
    private var db: FirebaseFirestore? = null
    private var progressDialog: ProgressDialog? = null
    private val PICK_IMAGE_REQUEST = 111
    var imageURI: Uri? = null
    private var x = 0f
    private var y = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing_book)
        editbookc = findViewById(R.id.editBookCover)
        editrating = findViewById(R.id.editRatingBar)
        editbook = findViewById(R.id.editBook)
        delebook = findViewById(R.id.deleteBook)
        editname = findViewById(R.id.editNameBook)
        editauther = findViewById(R.id.editNameAuthor)
        edityear = findViewById(R.id.editLaunchYear)
        rditprice = findViewById(R.id.editPrice)
        db = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("book img")
        editname.setText(intent.getStringExtra("Name_Book").toString())
        editauther.setText(intent.getStringExtra("Name_Author").toString())
        edityear.setText(intent.getStringExtra("Launch_Year").toString())
        rditprice.setText(intent.getStringExtra("Price_Book").toString())
        editrating.rating = intent.getFloatExtra("Book_Review", 0f)

        edityear.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val day = currentDate.get(Calendar.DAY_OF_MONTH)
            val month = currentDate.get(Calendar.MONTH)
            val year = currentDate.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this, { _, y, m, d -> edityear.setText("$y / ${m + 1} / $d") }, year, month, day
            )
            picker.show()
        }
        editbookc.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            editbookc.setBackgroundResource(0)
            y = 1
        }
        editrating.setOnRatingBarChangeListener { _, xx, _ ->
            x = xx
        }

        delebook.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Book")
            builder.setMessage("Do you want to Delete the Book?")
            builder.setPositiveButton("Yes") { _, _ ->
                deleteBook()
                Toast.makeText(this, "Delete Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ViewBooks::class.java))
            }
            builder.setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            builder.create().show()
        }

        editbook.setOnClickListener {
            if (editname.text.isEmpty() || editauther.text.isEmpty() || edityear.text.isEmpty() || rditprice.text.isEmpty()) {
                Toast.makeText(this, "fill ", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Edit Book")
                builder.setMessage("do you edit book?")
                builder.setPositiveButton("Yes") { _, _ ->
                    showDialog()
                    if (y == 1) {
                        val bitmap = (editbookc.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        val data = baos.toByteArray()
                        val childRef =
                            imageRef.child(System.currentTimeMillis().toString() + ".png")
                        var uploadTask = childRef.putBytes(data)
                        uploadTask.addOnFailureListener { exception ->
                            hideDialog()
                        }.addOnSuccessListener {
                            childRef.downloadUrl.addOnSuccessListener { uri ->
                                editBook(y, uri.toString())
                                hideDialog()
                                Toast.makeText(this, "Edit Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, ViewBooks::class.java))
                            }
                        }
                    } else {
                        editBook(y, "")
                        hideDialog()
                        Toast.makeText(this, "Edit Successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ViewBooks::class.java))
                    }
                }
                builder.setNegativeButton("No") { d, _ ->
                    d.dismiss()
                }
                builder.create().show()
            }
        }
    }

    private fun editBook(edo: Int, Image_Book: String) {
        db!!.collection("Books").get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                document.toObject<Book>()
                if (document.get("id") == intent.getStringExtra("id")) {
                    if (edo == 1) {
                        db!!.collection("Books").document(document.id)
                            .update("Image_Book", Image_Book)
                    }
                    db!!.collection("Books").document(document.id)
                        .update("Name_Book", editname.text.toString())
                    db!!.collection("Books").document(document.id)
                        .update("Name_Author", editauther.text.toString())
                    db!!.collection("Books").document(document.id)
                        .update("Launch_Year", edityear.text.toString())
                    db!!.collection("Books").document(document.id)
                        .update("Price_Book", rditprice.text.toString())
                    db!!.collection("Books").document(document.id)
                        .update("Book_Review", x.toString())
                }
            }
        }
    }
    private fun deleteBook() {
        db!!.collection("Books").get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                document.toObject<Book>()
                if (document.get("id") == intent.getStringExtra("id")) {
                    db!!.collection("Books").document(document.id).delete()
                }
            }
        }
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageURI = data!!.data
            editbookc.setImageURI(imageURI)
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
