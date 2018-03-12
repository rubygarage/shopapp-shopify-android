package com.shopapp.shopify.api.ext

inline fun <T, R : Any> Iterable<T>.notNullMap(transform: (T) -> R): List<R> {
    return filter { it != null }.map(transform)
}