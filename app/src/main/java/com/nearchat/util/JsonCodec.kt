package com.nearchat.util

import com.google.gson.Gson

object JsonCodec {
    private val gson = Gson()

    fun encode(value: Any): String = gson.toJson(value)

    fun <T> decode(value: String, clazz: Class<T>): T = gson.fromJson(value, clazz)
}
