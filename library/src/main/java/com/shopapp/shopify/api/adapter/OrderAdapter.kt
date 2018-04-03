package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Order
import com.shopapp.shopify.api.ext.isSingleOptions
import com.shopify.buy3.Storefront

object OrderAdapter {

    fun adapt(orderAdaptee: Storefront.Order, paginationValue: String? = null, isRemoveSingleOptions: Boolean = false): Order {

        if (isRemoveSingleOptions) {
            orderAdaptee.lineItems.edges.forEach {
                val variant = it.node.variant
                if (variant != null && variant.product.isSingleOptions()) {
                    variant.selectedOptions = null
                }
            }
        }

        return Order(
            id = orderAdaptee.id.toString(),
            currency = orderAdaptee.currencyCode.toString(),
            email = orderAdaptee.email,
            orderNumber = orderAdaptee.orderNumber,
            totalPrice = orderAdaptee.totalPrice,
            subtotalPrice = orderAdaptee.subtotalPrice,
            totalShippingPrice = orderAdaptee.totalShippingPrice,
            address = orderAdaptee.shippingAddress?.let {
                AddressAdapter.adapt(it)
            },
            processedAt = orderAdaptee.processedAt.toDate(),
            orderProducts = orderAdaptee.lineItems.edges
                .map { it.node }
                .filter { it != null }
                .map { OrderProductAdapter.adapt(it) },
            paginationValue = paginationValue
        )
    }

}
