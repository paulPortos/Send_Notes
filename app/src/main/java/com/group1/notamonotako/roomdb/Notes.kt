package com.group1.notamonotako.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Notes (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var contents: String,
    var toPublic: Boolean,
    var isPublic: Boolean  // Renamed from "public" to "isPublic"
)