package kuick.models

import kotlin.math.round
import kotlin.math.floor
import kotlin.math.roundToInt

const val K_LOCAL_DATE_FORMAT = "yyyy-MM-dd"
const val K_LOCAL_DATE_TIME_FORMAT = "YYYY-MM-DDTHH:mm:ss"
data class KLocalDate(val date: String): Comparable<KLocalDate> {
    override fun compareTo(other: KLocalDate): Int = date.compareTo(other.date)
    override fun toString(): String = date
}

data class KDateRange(val from: KLocalDate, val to: KLocalDate)


fun KLocalDate?.normalized(): KLocalDate? = if (this?.date == null) null else this

interface TimeService {

    fun systemTimeMillis(): Long

    fun now(): KLocalDate

    fun dayOfWeekIndex(date: KLocalDate): Int

    fun daysBetween(from: KLocalDate, to: KLocalDate): Int

    fun dayOfWeekName(day: KLocalDate): String

    fun addDays(since: KLocalDate, toLong: Long): KLocalDate

    fun currenyDayTime(): String

    fun currentYearDay(): String
}

fun TimeService.daysBetween(dateRange: KDateRange) = daysBetween(dateRange.from, dateRange.to)

fun secondsToHuman(_input: Int): String {
    val input = _input.toDouble()
    fun measure(amount: Double, unit: String): String {
        return if (amount == 0.0) "" else "" + round(amount)+" "+unit
    }
    fun measureFloor(amount: Double, unit: String): String {
        return if (amount == 0.0) "" else "" + floor(amount)+" "+unit
    }

    if (input == null) return "-"
    if (input == 0.0) return "0 seg"
    var lapse = ""
    if (input < 60) lapse = measure(input, "seg")
    else if (input >= 60 && input < 60*60) lapse = measure(input / 60, "min")
    else lapse = measureFloor(input / (60 * 60), "h") + " " + measureFloor((input % (60 * 60)) / 60, "min")

    return lapse
}

fun Double.toPercent(): String = "${(this * 100).roundToInt().toDouble()}%"