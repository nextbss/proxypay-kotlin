package co.ao.box

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

fun validDate(date: String): Boolean {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
    return try {
        fmt.parseLocalDate(date)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}

fun isAfterToday(date: String): Boolean {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
    val formattedDateTime: DateTime = fmt.parseDateTime(date)
    return when(DateTime.now().isAfter(formattedDateTime)) {
        true ->  true
        false -> false
    }
}