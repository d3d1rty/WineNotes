package com.example.winenotes

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.winenotes.database.AppDatabase
import com.example.winenotes.database.Note
import com.example.winenotes.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private val notes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.notesList.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            applicationContext, layoutManager.orientation
        )
        binding.notesList.addItemDecoration(dividerItemDecoration)

        adapter = MainAdapter()
        binding.notesList.adapter = adapter

        loadAllNotes()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_item_add_note) {
            addNewNote()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val startForAddResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result : ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val noteId = intent!!.getLongExtra(
                getString(R.string.intent_key_note_id),
                -1L
            )

            CoroutineScope(Dispatchers.IO).launch {
                val note = AppDatabase.getDatabase(applicationContext).noteDao().getNote(noteId)
                notes.add(note)

                val position = notes.indexOf(note)
                withContext(Dispatchers.Main) {
                    adapter.notifyItemChanged(position)
                    binding.notesList.scrollToPosition(position)
                }
            }
        }
    }

    private val startForUpdateResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result : ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK) {
            loadAllNotes()
        }
    }

    private fun addNewNote() {
        val intent = Intent(applicationContext, NoteActivity::class.java)
        intent.putExtra(
            getString(R.string.intent_purpose_key),
            getString(R.string.intent_purpose_add_key)
        )
        startForAddResult.launch(intent)
    }

    private fun loadAllNotes() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.noteDao()
            val results = dao.getAllNotes()

            withContext(Dispatchers.Main) {
                notes.clear()
                notes.addAll(results)
                adapter.notifyDataSetChanged()
            }
        }
    }

    inner class MainViewHolder(val view : TextView) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            val intent = Intent(applicationContext, NoteActivity::class.java)
            intent.putExtra(
                getString(R.string.intent_purpose_key),
                getString(R.string.intent_purpose_update_key)
            )

            val note = notes[adapterPosition]
            intent.putExtra(
                getString(R.string.intent_key_note_id),
                note.id
            )

            startForUpdateResult.launch(intent)
        }

        override fun onLongClick(view: View?): Boolean {
            val note = notes[adapterPosition]
            val builder = AlertDialog.Builder(view!!.context)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this note?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { dialogInterface, whichButton ->
                    CoroutineScope(Dispatchers.IO).launch {
                        AppDatabase.getDatabase(applicationContext).noteDao().deleteNote(note)
                        loadAllNotes()
                    }
                }
            builder.show()
            return true
        }
    }

    inner class MainAdapter() : RecyclerView.Adapter<MainViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
           val view = LayoutInflater
               .from(parent.context)
               .inflate(R.layout.note_item_view, parent, false) as TextView

           return MainViewHolder(view)
        }

        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
            holder.view.text = notes[position].toString()
        }

        override fun getItemCount(): Int {
            return notes.size
        }
    }
}