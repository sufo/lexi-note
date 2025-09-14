package com.sufo.lexinote.data.local.db

import android.database.Cursor
import com.sufo.lexinote.data.local.db.entity.DictWord

fun Cursor.getStringOrNull(columnName: String): String? {
    val index = getColumnIndex(columnName)
    return if (index != -1 && !isNull(index)) getString(index) else null
}

fun Cursor.getIntOrNull(columnName: String): Int? {
    val index = getColumnIndex(columnName)
    return if (index != -1 && !isNull(index)) getInt(index) else null
}

fun Cursor.toDictWord(sourceDictionary: String?=null): DictWord {
    return DictWord(
        id = getIntOrNull("id") ?: 0,
        word = getStringOrNull("word") ?: "",
        phonetic = getStringOrNull("phonetic"),
        translation = getStringOrNull("translation"),
        frq = getIntOrNull("frq"),
        exchange = getStringOrNull("exchange"),
        tag = getStringOrNull("tag"),
        imageUrl = getStringOrNull("imageUrl"),
        sourceDictionary = sourceDictionary
    )
}
