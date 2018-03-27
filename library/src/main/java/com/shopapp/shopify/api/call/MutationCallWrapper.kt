package com.shopapp.shopify.api.call

import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Error
import com.shopapp.shopify.api.adapter.ErrorAdapter
import com.shopify.buy3.GraphCall
import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront

abstract class MutationCallWrapper<out T>(private val callback: ApiCallback<T>) : GraphCall.Callback<Storefront.Mutation> {

    internal abstract fun adapt(data: Storefront.Mutation?): AdapterResult<T>?

    override fun onResponse(response: GraphResponse<Storefront.Mutation>) {
        val error = ErrorAdapter.adaptErrors(response.errors())
        val result = adapt(response.data())
        when {
            error != null -> callback.onFailure(error)
            result is AdapterResult.ErrorResult -> callback.onFailure(result.error)
            result is AdapterResult.DataResult -> callback.onResult(result.data)
            else -> callback.onFailure(Error.Content())
        }
    }

    override fun onFailure(graphError: GraphError) {
        callback.onFailure(ErrorAdapter.adapt(graphError))
    }
}