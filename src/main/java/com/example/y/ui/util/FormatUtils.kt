package com.example.y.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 金额格式化：分 → 元（带 ¥ 前缀）
 * 10050L → "¥100.50"
 */
fun Long.formatAmount(): String {
    val yuan = this / 100.0
    return "¥%.2f".format(yuan)
}

/**
 * 金额格式化：分 → 元（带正负号和 ¥ 前缀）
 * 10050L, isExpense=true → "-¥100.50"
 */
fun Long.formatSignedAmount(isExpense: Boolean): String {
    val prefix = if (isExpense) "-" else "+"
    return "$prefix${formatAmount()}"
}

/**
 * epoch millis → "2月6日" 或 "2026年2月6日"
 */
fun Long.formatDate(showYear: Boolean = false): String {
    val pattern = if (showYear) "yyyy年M月d日" else "M月d日"
    val sdf = SimpleDateFormat(pattern, Locale.CHINA)
    return sdf.format(Date(this))
}

/**
 * epoch millis → "周四" 等星期标签
 */
fun Long.formatDayOfWeek(): String {
    val sdf = SimpleDateFormat("E", Locale.CHINA)
    return sdf.format(Date(this))
}

/**
 * epoch millis → "2026年2月"
 */
fun Long.formatYearMonth(): String {
    val sdf = SimpleDateFormat("yyyy年M月", Locale.CHINA)
    return sdf.format(Date(this))
}

/**
 * 检查 epoch millis 是否是今天
 */
fun Long.isToday(): Boolean {
    val cal = Calendar.getInstance()
    val todayCal = Calendar.getInstance()
    cal.timeInMillis = this
    return cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
}

/**
 * 给定年月，获取 [月初0点, 下月初0点) 的 epoch millis 对
 */
fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis
    return start to end
}
