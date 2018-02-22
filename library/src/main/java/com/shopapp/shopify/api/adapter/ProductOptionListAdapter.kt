package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.ProductOption
import com.shopify.buy3.Storefront

object ProductOptionListAdapter {

    fun adapt(adaptee: List<Storefront.ProductOption>?): List<ProductOption> =
        adaptee?.map { ProductOption(it.id.toString(), it.name, it.values) } ?: listOf()
}