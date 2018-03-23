package com.shopapp.shopify.api.adapter

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.mock
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopify.buy3.Storefront
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner.Silent::class)
class ProductAdapterTest {

    companion object {
        const val DEFAULT_PAGINATION_VALUE = "default_pagination_value"
    }

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    private lateinit var shop: Storefront.Shop

    @Before
    fun setUp() {
        shop = StorefrontMockInstantiator.newShop()
    }

    @Test
    fun shouldAdaptFromProductStorefrontToProduct() {

        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        val product = ProductAdapter.adapt(shop, storefrontProduct, DEFAULT_PAGINATION_VALUE)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, product.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, product.title)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DESCRIPTION, product.productDescription)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DESCRIPTION, product.additionalDescription)
        assertEquals(StorefrontMockInstantiator.DEFAULT_CURRENCY_CODE.toString(), product.currency)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, product.price)
        assertEquals(StorefrontMockInstantiator.DEFAULT_VENDOR, product.vendor)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TYPE, product.type)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DATE.toDate(), product.createdAt)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DATE.toDate(), product.updatedAt)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_STRING, product.tags)
        assertEquals(DEFAULT_PAGINATION_VALUE, product.paginationValue)
        assertFalse(product.hasAlternativePrice)
        assertNotNull(product.options)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, product.options.size)
        assertNotNull(product.images)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, product.images.size)
        assertNotNull(product.variants)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, product.variants.size)
    }

    @Test
    fun shouldAdaptFromProductStorefrontToProductWithoutVariants() {
        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        val product = ProductAdapter.adapt(shop, storefrontProduct, DEFAULT_PAGINATION_VALUE, false)
        assertNotNull(product.variants)
        assertTrue(product.variants.isEmpty())
    }

    @Test
    fun shouldAdaptFromProductStorefrontToProductWithDifferentPrices() {

        val firstVariant = StorefrontMockInstantiator.newProductVariant()
        given(firstVariant.price).willReturn(BigDecimal.valueOf(100))
        val firstVariantEdge: Storefront.ProductVariantEdge = mock { on { node } doReturn firstVariant }

        val secondVariant = StorefrontMockInstantiator.newProductVariant()
        given(secondVariant.price).willReturn(BigDecimal.valueOf(150))
        val secondVariantEdge: Storefront.ProductVariantEdge = mock { on { node } doReturn secondVariant }

        val productConnection: Storefront.ProductVariantConnection = mock {
            val edgesMock = listOf(firstVariantEdge, secondVariantEdge)
            on { edges } doReturn edgesMock
        }

        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        given(storefrontProduct.variants).willReturn(productConnection)

        val product = ProductAdapter.adapt(shop, storefrontProduct)
        assertEquals(BigDecimal.valueOf(100), product.price)
        assertEquals(null, product.paginationValue)
        assertNotNull(product.variants)
        assertEquals(2, product.variants.size)
    }

    @Test
    fun shouldSetDefaultTitleToVariants() {
        val defaultValue = listOf(StorefrontMockInstantiator.DEFAULT_TITLE)
        val option: Storefront.ProductOption = StorefrontMockInstantiator.newProductOption()
        given(option.values).willReturn(defaultValue)

        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        given(storefrontProduct.options).willReturn(listOf(option))

        val product = ProductAdapter.adapt(shop, storefrontProduct)
        assertNotNull(product.options)
        assertEquals(1, product.options.size)
        assertNotNull(product.images)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, product.images.size)
        assertNotNull(product.variants)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, product.variants.size)
    }

    @Test
    fun shouldIgnoreNullVariants() {
        val variants: MutableList<Storefront.ProductVariantEdge?> = mutableListOf()
        variants.addAll(StorefrontMockInstantiator.newList(StorefrontMockInstantiator.newProductVariantEdge()))
        variants.add(null)

        val variantsConnection: Storefront.ProductVariantConnection = mock {
            on { edges } doReturn variants
        }

        val product = StorefrontMockInstantiator.newProduct()
        given(product.variants).willReturn(variantsConnection)

        val result = ProductAdapter.adapt(shop, product)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE + 1, product.variants.edges.size)
        assertNotNull(product.variants)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, result.variants.size)
    }

    @Test
    fun shouldSetDefaultPriceOnEmptyVariantList() {
        val variants: MutableList<Storefront.ProductVariantEdge?> = mutableListOf()
        val variantsConnection: Storefront.ProductVariantConnection = mock {
            on { edges } doReturn variants
        }

        val product = StorefrontMockInstantiator.newProduct()
        given(product.variants).willReturn(variantsConnection)

        val result = ProductAdapter.adapt(shop, product)
        assertEquals(BigDecimal.ZERO, result.price)
        assertFalse(result.hasAlternativePrice)
        assertTrue(result.variants.isEmpty())
    }

    @Test
    fun shouldSetDefaultPriceOnNullVariantItem() {
        val variants: MutableList<Storefront.ProductVariantEdge?> = mutableListOf()
        variants.add(null)

        val variantsConnection: Storefront.ProductVariantConnection = mock {
            on { edges } doReturn variants
        }

        val product = StorefrontMockInstantiator.newProduct()
        given(product.variants).willReturn(variantsConnection)

        val result = ProductAdapter.adapt(shop, product)
        assertEquals(BigDecimal.ZERO, result.price)
        assertFalse(result.hasAlternativePrice)
        assertTrue(result.variants.isEmpty())
    }
}