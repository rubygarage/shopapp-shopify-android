package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Author
import com.shopify.buy3.Storefront

object AuthorAdapter {

    fun adapt(adaptee: Storefront.ArticleAuthor): Author {
        return Author(
            adaptee.firstName,
            adaptee.lastName,
            adaptee.name,
            adaptee.email,
            adaptee.bio)
    }
}