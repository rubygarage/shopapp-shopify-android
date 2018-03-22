package com.shopapp.shopify.util

import android.content.Context

class AssetsReader {

    fun read(filename: String, context: Context): String {
        val input = context.assets.open(filename)
        val byteArray = ByteArray(input.available())
        input.read(byteArray)
        input.close()
        return String(byteArray)
    }
}