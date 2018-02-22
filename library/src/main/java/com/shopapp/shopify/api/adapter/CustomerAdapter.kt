package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Customer
import com.shopapp.shopify.constant.Constant.DEFAULT_STRING
import com.shopify.buy3.Storefront

object CustomerAdapter {

    fun adapt(adaptee: Storefront.Customer): Customer {
        return Customer(
            adaptee.id.toString(),
            adaptee.email,
            adaptee.firstName ?: DEFAULT_STRING,
            adaptee.lastName ?: DEFAULT_STRING,
            adaptee.phone ?: DEFAULT_STRING,
            adaptee.acceptsMarketing,
            AddressAdapter.adapt(adaptee.addresses),
            adaptee.defaultAddress?.let { AddressAdapter.adapt(it) }
        )
    }
}