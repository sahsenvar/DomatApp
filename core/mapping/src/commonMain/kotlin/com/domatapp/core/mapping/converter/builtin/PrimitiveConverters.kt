package com.domatapp.core.mapping.converter.builtin

import com.domatapp.core.mapping.converter.MapTypeConverter

/**
 * Converts String to Int.
 */
object StringToIntConverter : MapTypeConverter<String, Int>(String::class, Int::class) {
    override fun convertToNonNull(value: String): Int = value.toInt()
    override fun convertFromNonNull(value: Int): String = value.toString()
}

/**
 * Converts String to Long.
 */
object StringToLongConverter : MapTypeConverter<String, Long>(String::class, Long::class) {
    override fun convertToNonNull(value: String): Long = value.toLong()
    override fun convertFromNonNull(value: Long): String = value.toString()
}

/**
 * Converts String to Double.
 */
object StringToDoubleConverter : MapTypeConverter<String, Double>(String::class, Double::class) {
    override fun convertToNonNull(value: String): Double = value.toDouble()
    override fun convertFromNonNull(value: Double): String = value.toString()
}

/**
 * Converts String to Float.
 */
object StringToFloatConverter : MapTypeConverter<String, Float>(String::class, Float::class) {
    override fun convertToNonNull(value: String): Float = value.toFloat()
    override fun convertFromNonNull(value: Float): String = value.toString()
}

/**
 * Converts String to Boolean.
 */
object StringToBooleanConverter : MapTypeConverter<String, Boolean>(String::class, Boolean::class) {
    override fun convertToNonNull(value: String): Boolean = value.toBoolean()
    override fun convertFromNonNull(value: Boolean): String = value.toString()
}

/**
 * Converts Int to Long.
 */
object IntToLongConverter : MapTypeConverter<Int, Long>(Int::class, Long::class) {
    override fun convertToNonNull(value: Int): Long = value.toLong()
    override fun convertFromNonNull(value: Long): Int = value.toInt()
}

/**
 * Converts Int to Float.
 */
object IntToFloatConverter : MapTypeConverter<Int, Float>(Int::class, Float::class) {
    override fun convertToNonNull(value: Int): Float = value.toFloat()
    override fun convertFromNonNull(value: Float): Int = value.toInt()
}

/**
 * Converts Int to Double.
 */
object IntToDoubleConverter : MapTypeConverter<Int, Double>(Int::class, Double::class) {
    override fun convertToNonNull(value: Int): Double = value.toDouble()
    override fun convertFromNonNull(value: Double): Int = value.toInt()
}

/**
 * Converts Long to Float.
 */
object LongToFloatConverter : MapTypeConverter<Long, Float>(Long::class, Float::class) {
    override fun convertToNonNull(value: Long): Float = value.toFloat()
    override fun convertFromNonNull(value: Float): Long = value.toLong()
}

/**
 * Converts Long to Double.
 */
object LongToDoubleConverter : MapTypeConverter<Long, Double>(Long::class, Double::class) {
    override fun convertToNonNull(value: Long): Double = value.toDouble()
    override fun convertFromNonNull(value: Double): Long = value.toLong()
}

/**
 * Converts Float to Double.
 */
object FloatToDoubleConverter : MapTypeConverter<Float, Double>(Float::class, Double::class) {
    override fun convertToNonNull(value: Float): Double = value.toDouble()
    override fun convertFromNonNull(value: Double): Float = value.toFloat()
}
