package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.ShippingRate
import com.shopify.buy3.Storefront

object ShippingRateAdapter {

    fun adapt(adaptee: Storefront.ShippingRate): ShippingRate {
        return ShippingRate(
            adaptee.title,
            adaptee.price,
            adaptee.handle
        )
    }
}