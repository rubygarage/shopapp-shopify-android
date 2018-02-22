package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.State
import com.shopapp.shopify.api.entity.ApiState

object StateAdapter {

    fun adapt(data: ApiState): State {
        return State(
            id = data.id,
            countryId = data.countryId,
            name = data.name,
            code = data.code
        )
    }
}