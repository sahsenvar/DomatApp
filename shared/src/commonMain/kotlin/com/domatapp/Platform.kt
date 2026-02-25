package com.domatapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform