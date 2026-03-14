package com.domatapp.core.local.database.factory

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object DatabaseContextHolder {
    lateinit var context: Context
}
