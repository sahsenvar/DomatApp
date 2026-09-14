package com.domatapp.core.mapping

import com.domatapp.core.mapping.converter.TypeConverterRegistry
import com.domatapp.core.mapping.converter.builtin.FloatToDoubleConverter
import com.domatapp.core.mapping.converter.builtin.IntToDoubleConverter
import com.domatapp.core.mapping.converter.builtin.IntToFloatConverter
import com.domatapp.core.mapping.converter.builtin.IntToLongConverter
import com.domatapp.core.mapping.converter.builtin.LongToDoubleConverter
import com.domatapp.core.mapping.converter.builtin.LongToFloatConverter
import com.domatapp.core.mapping.converter.builtin.LongToInstantConverter
import com.domatapp.core.mapping.converter.builtin.StringToBooleanConverter
import com.domatapp.core.mapping.converter.builtin.StringToDoubleConverter
import com.domatapp.core.mapping.converter.builtin.StringToFloatConverter
import com.domatapp.core.mapping.converter.builtin.StringToInstantConverter
import com.domatapp.core.mapping.converter.builtin.StringToIntConverter
import com.domatapp.core.mapping.converter.builtin.StringToLongConverter
import com.domatapp.core.mapping.converter.platform.NSURLToStringConverter

/**
 * iOS-specific converter registration.
 */
actual object PlatformConverters {
    actual fun register() {
        // Register String converters
        TypeConverterRegistry.register(StringToIntConverter)
        TypeConverterRegistry.register(StringToLongConverter)
        TypeConverterRegistry.register(StringToFloatConverter)
        TypeConverterRegistry.register(StringToDoubleConverter)
        TypeConverterRegistry.register(StringToBooleanConverter)

        // Register numeric converters
        TypeConverterRegistry.register(IntToLongConverter)
        TypeConverterRegistry.register(IntToFloatConverter)
        TypeConverterRegistry.register(IntToDoubleConverter)
        TypeConverterRegistry.register(LongToFloatConverter)
        TypeConverterRegistry.register(LongToDoubleConverter)
        TypeConverterRegistry.register(FloatToDoubleConverter)

        // Register date/time converters
        TypeConverterRegistry.register(StringToInstantConverter)
        TypeConverterRegistry.register(LongToInstantConverter)

        // Register iOS-specific converters
        TypeConverterRegistry.register(NSURLToStringConverter)
    }
}
