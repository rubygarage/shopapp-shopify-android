package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Checkout
import com.shopify.buy3.Storefront

object CheckoutAdapter {

    fun adapt(adaptee: Storefront.Checkout): Checkout {
        return Checkout(
            adaptee.id.toString(),
            adaptee.webUrl,
            adaptee.requiresShipping,
            adaptee.subtotalPrice,
            adaptee.totalPrice,
            adaptee.totalTax,
            adaptee.currencyCode.name,
            adaptee.shippingAddress?.let { AddressAdapter.adapt(it) },
            adaptee.shippingLine?.let { ShippingRateAdapter.adapt(it) }
        )
    }
}