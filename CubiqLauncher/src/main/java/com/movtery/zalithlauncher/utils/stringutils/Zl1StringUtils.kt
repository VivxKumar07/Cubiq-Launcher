package com.movtery.zalithlauncher.utils.stringutils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.math.min

class SortStrings {
    companion object {
        @JvmStatic
        fun compareChar(thisName: String, otherName: String): Int {
            val firstLength = thisName.length
            val secondLength = otherName.length

            for (i in 0 until min(firstLength, secondLength)) {
                val firstChar = thisName[i].lowercaseChar()
                val secondChar = otherName[i].lowercaseChar()
                val compare = firstChar.compareTo(secondChar)
                if (compare != 0) return compare
            }
            return firstLength.compareTo(secondLength)
        }
    }
}

class StringFilter {
    companion object {
        @JvmStatic
        fun containsSubstring(input: String, substring: String, caseSensitive: Boolean): Boolean {
            val adjustedInput = if (caseSensitive) input else input.lowercase(Locale.getDefault())
            val adjustedSubstring = if (caseSensitive) substring else substring.lowercase(Locale.getDefault())
            val pattern = Pattern.compile(Pattern.quote(adjustedSubstring))
            return pattern.matcher(adjustedInput).find()
        }
    }
}

object StringUtils {
    @JvmStatic
    fun insertSpace(prefixString: Any?, vararg suffixString: Any?): String {
        val sb = StringBuilder()
        if (prefixString != null) sb.append(prefixString.toString())
        for (item in suffixString) {
            if (sb.isNotEmpty()) sb.append(' ')
            sb.append(item?.toString() ?: "")
        }
        return sb.toString()
    }

    @JvmStatic
    fun formatDate(date: Date, locale: Locale, timeZone: TimeZone): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", locale)
        sdf.timeZone = timeZone
        return sdf.format(date)
    }

    @JvmStatic
    fun isEmptyOrBlank(value: String?): Boolean = value.isNullOrBlank()
}

