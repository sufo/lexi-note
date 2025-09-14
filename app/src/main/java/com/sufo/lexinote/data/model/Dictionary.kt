package com.sufo.lexinote.data.model

import java.io.File

data class Dictionary(
    val name: String,
    val path: String,
    val ifoFile: File,
    val idxFile: File,
    val dictFile: File
)