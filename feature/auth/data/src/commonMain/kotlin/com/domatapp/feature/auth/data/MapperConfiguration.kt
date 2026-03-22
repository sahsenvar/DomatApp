package com.domatapp.feature.auth.data

import com.domatapp.core.mapping.annotations.KMapperConfiguration
import com.domatapp.core.mapping.startKMapper
import com.domatapp.feature.auth.data.converter.AnotherIntToStringConverter
import com.domatapp.feature.auth.data.converter.CustomIntToStringConverter

/**
 * Auth module mapper configuration.
 *
 * Custom converters registered here will override built-in converters
 * and be used in all generated mapping code within this module.
 *
 * Priority: First registered converter has highest priority.
 */
@KMapperConfiguration
val authMappers = startKMapper {
    // First registered = highest priority
    registerGlobalTypeConverter(CustomIntToStringConverter)

    // This will be ignored (CustomIntToStringConverter has priority)
    registerGlobalTypeConverter(AnotherIntToStringConverter)
}
