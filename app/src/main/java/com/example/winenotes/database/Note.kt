package com.example.winenotes.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
class Note(
    @PrimaryKey(autoGenerate = true) val id : Long?,
    @ColumnInfo val title : String,
    @ColumnInfo val notes : String,
    @ColumnInfo(name = "last_modified") val lastModified : String
) {
    override fun toString(): String {
        return "${readableLastModified()} | ${title}"
    }

    private fun readableLastModified() : String {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        parser.setTimeZone(TimeZone.getTimeZone("UTC"))

        val dateInDatabase : Date = parser.parse(lastModified)
        val displayFormat = SimpleDateFormat("HH:mm a MM/yyyy")
        return displayFormat.format(dateInDatabase)
    }
}