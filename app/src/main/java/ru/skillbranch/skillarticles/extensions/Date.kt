package ru.skillbranch.skillarticles.extensions

import android.text.format.DateUtils
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*


fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy") =
    SimpleDateFormat(pattern, Locale("ru")).format(this)

enum class TimeUnits {
    SECOND {
        override fun plural(value: Int): String = when (abs(value) % 10) {
            1 -> "$value секунду"
            in 2..4 -> "$value секунды"
            else -> "$value секунд"
        }

    },
    MINUTE {
        override fun plural(value: Int): String = when (abs(value) % 10) {
            1 -> "$value минуту"
            in 2..4 -> "$value минуты"
            else -> "$value минут"
        }
    },
    HOUR {
        override fun plural(value: Int): String = when (abs(value) % 10) {
            1 -> "$value час"
            in 2..4 -> "$value часа"
            else -> "$value часов"
        }
    },
    DAY {
        override fun plural(value: Int): String = when (abs(value) % 10) {
            1 -> "$value день"
            in 2..4 -> "$value дня"
            else -> "$value дней"
        }
    };
    abstract fun plural(value: Int): String
}

fun Date.add(value: Int, units: TimeUnits): Date = this.apply {
    time += when (units) {
        TimeUnits.SECOND -> value * DateUtils.SECOND_IN_MILLIS
        TimeUnits.MINUTE -> value * DateUtils.MINUTE_IN_MILLIS
        TimeUnits.HOUR -> value * DateUtils.HOUR_IN_MILLIS
        TimeUnits.DAY -> value * DateUtils.DAY_IN_MILLIS
    }
}

const val MINUTE_SECONDS = DateUtils.MINUTE_IN_MILLIS/1000
const val HOUR_SECONDS = DateUtils.HOUR_IN_MILLIS/1000
const val DAY_SECONDS = DateUtils.DAY_IN_MILLIS/1000


fun Date.humanizeDiff(date: Date = Date()): String = let {
    val diff = (this.time - date.time) / 1000
    val absDiff = abs(diff)
    val result = when (absDiff) {
        in 0L..1L -> "только что"
        in 1L..45L -> "несколько секунд"
        in 45L..75L -> "минуту"
        in 75L..45L * MINUTE_SECONDS -> TimeUnits.MINUTE.plural((absDiff / MINUTE_SECONDS).toInt())
        in 45 * MINUTE_SECONDS..75 * MINUTE_SECONDS -> "час"
        in 75 * MINUTE_SECONDS..22 * HOUR_SECONDS -> TimeUnits.HOUR.plural((absDiff / HOUR_SECONDS).toInt())
        in 22 * HOUR_SECONDS..26 * HOUR_SECONDS -> "день"
        in 26 * HOUR_SECONDS..360 * DAY_SECONDS -> TimeUnits.DAY.plural((absDiff / DAY_SECONDS).toInt())
        else -> if (diff < 0) "более года назад" else "более чем через год"
    }
    if (absDiff in 2L..360 * DAY_SECONDS)
        when {
            diff > 0 -> "через " + result
            diff < 0 -> result + " назад"
            else -> result
        }
    else
        result
}

fun Date.shortFormat(): String? {
    val pattern = if (this.isSameDay(Date())) "HH:mm" else "dd.MM.yy"
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}


fun Date.isSameDay(date: Date): Boolean {
    return this.time / DateUtils.DAY_IN_MILLIS == date.time / DateUtils.DAY_IN_MILLIS;
}