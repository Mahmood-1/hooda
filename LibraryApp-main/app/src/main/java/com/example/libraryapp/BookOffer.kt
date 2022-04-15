package com.example.libraryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BookOffer : AppCompatActivity() {
    private var db: FirebaseFirestore? = null
    private var adapter: FirestoreRecyclerAdapter<Book, BookViewHolder>? = null
    private lateinit var addBooks: FloatingActionButton
    private lateinit var rebook: RecyclerView
    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_books)
        addBooks = findViewById(R.id.add_Books)
        db = Firebase.firestore
        getAllBook()
        addBooks.setOnClickListener {
            startActivity(Intent(this, AddBook::class.java))
        }
    }
    private fun getAllBook() {
        rebook = findViewById(R.id.rvBook)
        val query = db!!.collection("Books")
        val options = FirestoreRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

        adapter = object : FirestoreRecyclerAdapter<Book, BookViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
                val view = LayoutInflater.from(this@BookOffer).inflate(
                    R.layout.book_item,
                    parent,
                    false
                )
                return BookViewHolder(view)
            }
            override fun onBindViewHolder(holder: BookViewHolder, position: Int, model: Book) {
                holder.launchYear.text = model.Launch_Year
                holder.bookReview.text = model.Book_Review
                holder.bookPrice.text = model.Price_Book
                holder.bookName.text = model.Name_Book
                holder.bookAuthor.text = model.Name_Author
                Glide.with(this@BookOffer).load(model.Image_Book).into(holder.imageBook)
                holder.editBook.setOnClickListener {
                    intent(model.id, model.Name_Book, model.Name_Author, model.Launch_Year, model.Image_Book, model.Book_Review.toFloat(), model.Price_Book)
                }
                count++
            }
        }
        rebook.layoutManager = LinearLayoutManager(this)
        rebook.adapter = adapter
    }

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageBook = view.findViewById<ImageView>(R.id.image_book)!!
        var bookReview = view.findViewById<TextView>(R.id.book_review)!!
        var bookPrice = view.findViewById<TextView>(R.id.book_price)!!
        val editBook = view.findViewById<Button>(R.id.editBtn)!!
        var bookName = view.findViewById<TextView>(R.id.book_name)!!
        var bookAuthor = view.findViewById<TextView>(R.id.book_author)!!
        var launchYear = view.findViewById<TextView>(R.id.launch_year)!!
    }
    fun intent(id: String, Name_Book: String, Name_Author: String, Launch_Year: String, Image_Book: String, Book_Review: Float, Price_Book: String) {
        val i = Intent(this, EditingBook::class.java)
        i.putExtra("id", id)
        i.putExtra("Name_Book", Name_Book)
        i.putExtra("Name_Author", Name_Author)
        i.putExtra("Launch_Year", Launch_Year)
        i.putExtra("Image_Book", Image_Book)
        i.putExtra("Book_Review", Book_Review)
        i.putExtra("Price_Book", Price_Book)
        startActivity(i)
    }
    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }
}