package com.shopapp.shopify.api

import com.shopapp.gateway.entity.SortType
import com.shopapp.shopify.constant.Constant.ITEMS_COUNT
import com.shopify.buy3.Storefront

internal object QueryHelper {

    fun getDefaultProductQuery(productQuery: Storefront.ProductQuery): Storefront.ProductQuery {
        return productQuery.title()
            .description()
            .descriptionHtml()
            .vendor()
            .productType()
            .createdAt()
            .updatedAt()
            .tags()
            .options({ args -> args.first(ITEMS_COUNT) }, { optionsQuery ->
                optionsQuery
                    .name()
                    .values()
            })
    }

    fun getDefaultProductVariantQuery(productVariantQuery: Storefront.ProductVariantQuery): Storefront.ProductVariantQuery {
        return productVariantQuery
            .title()
            .price()
            .weight()
            .weightUnit()
            .availableForSale()
            .selectedOptions({ optionsQuery -> optionsQuery.name().value() })
            .image({ imageQuery ->
                imageQuery
                    .id()
                    .src()
                    .altText()
            })
            .product({ productQuery ->
                productQuery
                    .images({ it.first(1) }, { imageConnectionQuery ->
                        imageConnectionQuery.edges({ imageEdgeQuery ->
                            imageEdgeQuery.node({ imageQuery ->
                                imageQuery
                                    .id()
                                    .src()
                                    .altText()
                            })
                        })
                    })
                    .options({ optionsQuery -> optionsQuery.name().values() })
            })
    }

    fun getDefaultArticleQuery(articleQuery: Storefront.ArticleQuery): Storefront.ArticleQuery {
        return articleQuery.title()
            .content()
            .contentHtml()
            .tags()
            .publishedAt()
            .url()
            .image({
                it.id()
                    .src()
                    .altText()
            })
            .author {
                it.firstName()
                    .lastName()
                    .name()
                    .email()
                    .bio()
            }
            .blog({ it.title() })
    }

    fun getDefaultCheckoutQuery(checkoutQuery: Storefront.CheckoutQuery): Storefront.CheckoutQuery {
        return checkoutQuery.webUrl()
            .email()
            .requiresShipping()
            .totalPrice()
            .subtotalPrice()
            .totalTax()
            .currencyCode()
            .shippingAddress { getDefaultAddressQuery(it) }
            .shippingLine { getDefaultShippingRateQuery(it) }
    }

    fun getDefaultAddressQuery(addressQuery: Storefront.MailingAddressQuery): Storefront.MailingAddressQuery {
        return addressQuery.country()
            .firstName()
            .lastName()
            .address1()
            .address2()
            .city()
            .province()
            .zip()
            .phone()
    }

    fun getDefaultShippingRateQuery(shippingRateQuery: Storefront.ShippingRateQuery): Storefront.ShippingRateQuery {
        return shippingRateQuery.handle()
            .price()
            .title()
    }

    fun getDefaultCustomerQuery(customerQuery: Storefront.CustomerQuery): Storefront.CustomerQuery {
        return customerQuery.id()
            .defaultAddress { getDefaultAddressQuery(it) }
            .addresses({ it.first(ITEMS_COUNT) }, {
                it.edges {
                    it.cursor().node {
                        getDefaultAddressQuery(it)
                    }
                }
            })
            .firstName()
            .lastName()
            .email()
            .phone()
            .acceptsMarketing()
    }

    fun getDefaultUserErrors(userErrorQuery: Storefront.UserErrorQuery): Storefront.UserErrorQuery {
        return userErrorQuery
            .field()
            .message()
    }

    fun getDefaultCustomerUpdateMutationQuery(token: String, customerInput: Storefront.CustomerUpdateInput?): Storefront.MutationQuery? {
        return Storefront.mutation {
            it.customerUpdate(token, customerInput, {
                it.customer { getDefaultCustomerQuery(it) }
                it.userErrors { getDefaultUserErrors(it) }
            })
        }
    }

    fun getDefaultOrderQuery(orderQuery: Storefront.OrderQuery): Storefront.OrderQuery {
        return orderQuery
            .orderNumber()
            .totalPrice()
            .email()
            .currencyCode()
            .processedAt()
            .lineItems({ it.first(ITEMS_COUNT) }) { lineItemsQuery ->
                lineItemsQuery.edges { productVariantConnectionQuery ->
                    productVariantConnectionQuery.node { node ->
                        node
                            .title()
                            .quantity()
                            .variant { productVariantQuery ->
                                getDefaultProductVariantQuery(productVariantQuery)
                            }
                    }
                }
            }
    }

    fun getProductSortKey(sortType: SortType?): Storefront.ProductSortKeys? {
        if (sortType != null) {
            return when (sortType) {
                SortType.NAME -> Storefront.ProductSortKeys.TITLE
                SortType.RECENT -> Storefront.ProductSortKeys.CREATED_AT
                SortType.RELEVANT -> Storefront.ProductSortKeys.RELEVANCE
                SortType.TYPE -> Storefront.ProductSortKeys.PRODUCT_TYPE
                else -> null
            }
        }
        return null
    }

    fun getCollectionSortKey(sortType: SortType?): Storefront.CollectionSortKeys? {
        if (sortType != null) {
            return when (sortType) {
                SortType.NAME -> Storefront.CollectionSortKeys.TITLE
                SortType.RECENT -> Storefront.CollectionSortKeys.UPDATED_AT
                SortType.RELEVANT -> Storefront.CollectionSortKeys.RELEVANCE
                else -> null
            }
        }
        return null
    }

    fun getProductCollectionSortKey(sortType: SortType?): Storefront.ProductCollectionSortKeys? {
        if (sortType != null) {
            return when (sortType) {
                SortType.NAME -> Storefront.ProductCollectionSortKeys.TITLE
                SortType.RECENT -> Storefront.ProductCollectionSortKeys.CREATED
                SortType.RELEVANT -> Storefront.ProductCollectionSortKeys.RELEVANCE
                SortType.PRICE_HIGH_TO_LOW,
                SortType.PRICE_LOW_TO_HIGH -> Storefront.ProductCollectionSortKeys.PRICE
                else -> null
            }
        }
        return null
    }

    fun getArticleSortKey(sortType: SortType?): Storefront.ArticleSortKeys? {
        if (sortType != null) {
            return when (sortType) {
                SortType.NAME -> Storefront.ArticleSortKeys.TITLE
                SortType.RECENT -> Storefront.ArticleSortKeys.UPDATED_AT
                SortType.RELEVANT -> Storefront.ArticleSortKeys.RELEVANCE
                else -> null
            }
        }
        return null
    }
}