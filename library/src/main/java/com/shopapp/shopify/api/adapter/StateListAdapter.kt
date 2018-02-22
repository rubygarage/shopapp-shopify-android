package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.State
import com.shopapp.shopify.api.entity.ApiState

object StateListAdapter {

    fun adapt(states: List<ApiState>?): List<State> =
        states?.map { StateAdapter.adapt(it) } ?: listOf()
}
