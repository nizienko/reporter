package core

import com.vaadin.ui.Notification
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Boolean.status(): String = if (this) {
    "passed"
} else {
    "failed"
}

fun Long.toReadableDate(): String =
        Instant.ofEpochMilli(this)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

fun Long.toReadableTime(): String =
        Instant.ofEpochMilli(this)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"))

fun showError(text: String) {
    Notification.show(text, Notification.Type.ERROR_MESSAGE)
}