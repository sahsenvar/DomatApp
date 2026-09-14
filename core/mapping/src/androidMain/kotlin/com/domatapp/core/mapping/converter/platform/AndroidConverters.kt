package com.domatapp.core.mapping.converter.platform

import android.net.Uri
import com.domatapp.core.mapping.converter.MapTypeConverter

/**
 * Android-specific converter: Uri to String.
 */
object UriToStringConverter : MapTypeConverter<Uri, String>(Uri::class, String::class) {
    override fun convertToNonNull(value: Uri): String = value.toString()
    override fun convertFromNonNull(value: String): Uri = Uri.parse(value)
}
