package com.group1.notamonotako.roomdb

sealed interface NotesEvent{
    data object SaveNotes: NotesEvent
    data class SetTitle(val title: String): NotesEvent
    data class SetContents(val contents: String): NotesEvent
    data class SetPublic(val public: Boolean): NotesEvent
    data class SetToPublic(val toPublic: Boolean): NotesEvent
}