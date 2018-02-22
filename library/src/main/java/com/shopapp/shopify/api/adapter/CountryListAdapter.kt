package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Country
import com.shopapp.shopify.api.entity.ApiCountry

object CountryListAdapter {

    fun adapt(countries: List<ApiCountry>?): List<Country> =
        countries?.map { CountryAdapter.adapt(it) } ?: listOf()

}
