package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.OrderProduct
import com.shopify.buy3.Storefront

object OrderProductAdapter {

    fun adapt(orderAdaptee: Storefront.OrderLineItem): OrderProduct {
        return OrderProduct(
            title = orderAdaptee.title,
            productVariant = orderAdaptee.variant?.let { ProductVariantAdapter.adapt(it) },
            quantity = orderAdaptee.quantity
        )
    }

}
