package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Category
import com.shopapp.shopify.constant.Constant.DEFAULT_STRING
import com.shopify.buy3.Storefront

object CategoryAdapter {

    fun adapt(shop: Storefront.Shop, collection: Storefront.Collection): Category {
        return Category(
            collection.id.toString(),
            collection.title,
            collection.description,
            collection.descriptionHtml ?: DEFAULT_STRING,
            ImageAdapter.adapt(collection.image),
            collection.updatedAt.toDate(),
            ProductListAdapter.adapt(shop, collection.products)
        )
    }
}
