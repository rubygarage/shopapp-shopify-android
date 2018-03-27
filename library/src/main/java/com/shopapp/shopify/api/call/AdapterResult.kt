package com.shopapp.shopify.api.call

import com.shopapp.gateway.entity.Error

sealed class AdapterResult<out T> {

    class ErrorResult<out T>(val error: Error) : AdapterResult<T>()
    class DataResult<out T>(val data: T) : AdapterResult<T>()
}