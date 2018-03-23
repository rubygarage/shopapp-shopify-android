package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Image
import com.shopapp.gateway.entity.Product
import com.shopapp.gateway.entity.ProductVariant
import com.shopapp.shopify.api.ext.isSingleOptions
import com.shopapp.shopify.constant.Constant.DEFAULT_STRING
import com.shopify.buy3.Storefront
import java.math.BigDecimal

object ProductAdapter {

    private val DEFAULT_PRICE = BigDecimal.ZERO

    fun adapt(shopAdaptee: Storefront.Shop,
              productAdaptee: Storefront.Product,
              paginationValue: String? = null,
              isConvertVariants: Boolean = true): Product {
        return if (isConvertVariants) {
            adapt(
                shopAdaptee,
                productAdaptee,
                paginationValue,
                productAdaptee.variants.edges.filterNotNull().map { ProductVariantAdapter.adapt(it.node) }
            )
        } else {
            adapt(
                shopAdaptee,
                productAdaptee,
                paginationValue,
                listOf()
            )
        }
    }

    private fun adapt(shopAdaptee: Storefront.Shop,
                      productAdaptee: Storefront.Product,
                      paginationValue: String? = null,
                      variants: List<ProductVariant>): Product {

        val productImages = convertImage(productAdaptee)

        if (productAdaptee.isSingleOptions()) {
            productAdaptee.variants.edges.forEach { it.node.title = DEFAULT_STRING }
        }

        val pricePair = convertPrice(productAdaptee)
        return Product(
            id = productAdaptee.id.toString(),
            title = productAdaptee.title,
            productDescription = productAdaptee.description,
            additionalDescription = productAdaptee.descriptionHtml,
            vendor = productAdaptee.vendor,
            currency = shopAdaptee.paymentSettings.currencyCode.toString(),
            type = productAdaptee.productType,
            tags = productAdaptee.tags,
            createdAt = productAdaptee.createdAt.toDate(),
            updatedAt = productAdaptee.updatedAt.toDate(),
            price = pricePair.first,
            hasAlternativePrice = pricePair.first != pricePair.second,
            images = productImages,
            options = ProductOptionListAdapter.adapt(productAdaptee.options),
            variants = variants,
            paginationValue = paginationValue
        )
    }

    private fun convertPrice(productAdaptee: Storefront.Product): Pair<BigDecimal, BigDecimal> {
        val variantsList = productAdaptee.variants.edges
        return if (variantsList.size > 0) {
            val mappedList = variantsList.filterNotNull().map { it.node.price }
            Pair(mappedList.min() ?: DEFAULT_PRICE, mappedList.max() ?: DEFAULT_PRICE)
        } else {
            Pair(DEFAULT_PRICE, DEFAULT_PRICE)
        }
    }

    private fun convertImage(productAdaptee: Storefront.Product): List<Image> =
        productAdaptee.images.edges.filterNotNull().mapNotNull { ImageAdapter.adapt(it.node) }

}
