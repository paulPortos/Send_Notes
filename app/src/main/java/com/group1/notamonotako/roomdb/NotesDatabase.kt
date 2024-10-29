package com.group1.notamonotako.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Notes::class],
    version = 1
)
abstract class NotesDatabase: RoomDatabase() {

    abstract val dao: NotesDao
}