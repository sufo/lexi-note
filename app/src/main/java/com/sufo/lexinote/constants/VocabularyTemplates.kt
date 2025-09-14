package com.sufo.lexinote.constants

import androidx.annotation.DrawableRes
import com.sufo.lexinote.R

data class VocTemplate(
    val key: String,
    val name: String,
    @DrawableRes val iconRes: Int,
    val wordCount: Int
)
object VocabularyTemplates {
    val all = listOf(
        VocTemplate(
            key = "zk",
            name = "中考",
            iconRes = R.drawable.ph_student,
            wordCount = 1384 // Placeholder
        ),
        VocTemplate(
            key = "cet4",
            name = "四级",
            iconRes = R.drawable.ph_four,
            wordCount = 3317 // Placeholder
        ),
        VocTemplate(
            key = "cet6",
            name = "六级",
            iconRes = R.drawable.ph_six,
            wordCount = 4599 // Placeholder
        ),
        VocTemplate(
            key = "toefl",
            name = "托福",
            iconRes = R.drawable.toefl,
            wordCount = 5653 // Placeholder
        ),
        VocTemplate(
            key = "ielts",
            name = "雅思",
            iconRes = R.drawable.ielts,
            wordCount = 4106 // Placeholder
        ),
        VocTemplate(
            key = "gre",
            name = "GRE",
            iconRes = R.drawable.gre,
            wordCount = 5916 // Placeholder
        )
    )
}
