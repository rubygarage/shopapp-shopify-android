package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Country
import com.shopapp.shopify.api.entity.ApiCountry

object CountryAdapter {

    fun adapt(data: ApiCountry): Country {
        return Country(
            id = data.id,
            code = data.code,
            name = data.name,
            states = StateListAdapter.adapt(data.states)
        )
    }
}