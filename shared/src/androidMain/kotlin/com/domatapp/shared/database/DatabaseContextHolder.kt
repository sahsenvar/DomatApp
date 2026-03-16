package com.domatapp.shared.database

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object DatabaseContextHolder {
    lateinit var context: Context
}
