package com.group1.notamonotako.roomdb.database_manager


import com.group1.notamonotako.roomdb.Notes
import com.group1.notamonotako.roomdb.NotesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class NotesRepository(private val dao: NotesDao) {
    suspend fun fetchOfflineNotes(): List<Notes> {
        return withContext(Dispatchers.IO) {
            dao.getOfflineNotes()
        }
    }

    suspend fun deleteOfflineNotes(noteId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteNoteById(noteId)
        }
    }

}
