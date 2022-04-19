package com.example.winenotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.winenotes.database.AppDatabase
import com.example.winenotes.database.Note
import com.example.winenotes.databinding.ActivityNoteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityNoteBinding

    private var purpose : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = getIntent()
        purpose = intent.getStringExtra(
            getString(R.string.intent_purpose_key)
        )

        if (purpose.isNullOrBlank()) {
            finish()
        }

        setTitle("${purpose} Note")

        binding.btnSave.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val title = binding.etTitle.getText().toString().trim()
        if (title.isEmpty()) {
            Toast
                .makeText(applicationContext, "Title cannot be empty", Toast.LENGTH_LONG)
                .show()
            return
        }

        val notes = binding.etNotes.getText().toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            val noteDao = AppDatabase.getDatabase(applicationContext).noteDao()
            val noteId : Long
            val lastModified = getLastModified()

            if (purpose.equals(getString(R.string.intent_purpose_add_key))) {
                val note = Note(null, title, notes, lastModified)
                noteId = noteDao.addNote(note)
            } else {
                TODO("Need to implement update")
            }

            val intent = Intent()
            intent.putExtra(
                getString(R.string.intent_key_note_id),
                noteId
            )

            withContext(Dispatchers.Main) {
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun getLastModified() : String {
        val now : Date = Date()
        val databaseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        databaseDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return databaseDateFormat.format(now)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}