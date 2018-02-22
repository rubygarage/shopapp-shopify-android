package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Address
import com.shopapp.shopify.constant.Constant
import com.shopify.buy3.Storefront

object AddressAdapter {

    fun adapt(adaptee: Storefront.MailingAddress): Address {
        return Address(
            id = adaptee.id.toString(),
            address = adaptee.address1,
            secondAddress = adaptee.address2,
            city = adaptee.city,
            state = adaptee.province,
            country = adaptee.country,
            firstName = adaptee.firstName,
            lastName = adaptee.lastName,
            zip = adaptee.zip ?: Constant.DEFAULT_STRING,
            phone = adaptee.phone
        )
    }

    fun adapt(adaptee: Storefront.MailingAddressConnection?): List<Address> {
        return adaptee?.edges?.map { adapt(it.node) } ?: listOf()
    }
}