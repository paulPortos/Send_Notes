package com.group1.notamonotako.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert


@Dao
interface NotesDao {
    @Upsert
    suspend fun UpsertNotes(notes: Notes): Long

    @Delete
    suspend fun DeleteNotes(notes: Notes)

    @Query("SELECT * FROM notes ORDER BY id")
    suspend fun getOfflineNotes(): List<Notes>

    // Delete a note by its ID
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int): Int
}