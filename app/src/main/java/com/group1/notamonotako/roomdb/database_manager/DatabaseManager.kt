package com.group1.notamonotako.roomdb.database_manager

import android.content.Context
import androidx.room.Room
import com.group1.notamonotako.roomdb.NotesDao
import com.group1.notamonotako.roomdb.NotesDatabase

object DatabaseManager {
    private var notesDatabase : NotesDatabase? = null

    fun getNotesDao(context: Context): NotesDao {
        if (notesDatabase == null){
            notesDatabase = Room.databaseBuilder(
                context.applicationContext,
                NotesDatabase::class.java,
                "notes_database"
            ).build()
        }
        return notesDatabase!!.dao
    }
}