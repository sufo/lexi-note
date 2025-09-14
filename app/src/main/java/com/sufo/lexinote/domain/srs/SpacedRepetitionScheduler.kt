package com.sufo.lexinote.domain.srs

import com.sufo.lexinote.data.local.db.entity.Word
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

/**
 * Implements the SM-2 spaced repetition algorithm.
 */
object SpacedRepetitionScheduler {

    /**
     * Calculates the next review date for a word based on the user's recall quality.
     *
     * @param word The word that was reviewed.
     * @param quality The user's assessment of their recall (0-5).
     *        5: perfect response
     *        4: correct response after a hesitation
     *        3: correct response with serious difficulty
     *        2: incorrect response; where the correct one seemed easy to recall
     *        1: incorrect response; the correct one remembered
     *        0: complete blackout.
     * @return The updated Word object with new SRS data.
     */
    fun schedule(word: Word, quality: Int): Word {
        if (quality < 3) {
            // If the quality is low, reset the repetition count.
            return word.copy(
                repetitions = 0,
                interval = 1, // Reset interval to 1 day
                nextReviewDate = calculateNextReviewDate(1)
            )
        }

        val newRepetitions = word.repetitions + 1
        val newEasinessFactor = calculateNewEasinessFactor(word.easinessFactor, quality)

        val newInterval = when (newRepetitions) {
            1 -> 1
            2 -> 6
            else -> (word.interval * newEasinessFactor).roundToInt()
        }

        val nextReviewDate = calculateNextReviewDate(newInterval)

        return word.copy(
            repetitions = newRepetitions,
            easinessFactor = newEasinessFactor,
            interval = newInterval,
            nextReviewDate = nextReviewDate
        )
    }

    private fun calculateNewEasinessFactor(oldEf: Float, quality: Int): Float {
        val newEf = oldEf + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)).toFloat()
        return maxOf(1.3f, newEf) // Easiness factor should not be less than 1.3
    }

    private fun calculateNextReviewDate(intervalInDays: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, intervalInDays)
        return calendar.time
    }
}
