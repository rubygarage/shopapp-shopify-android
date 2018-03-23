package com.shopapp.shopify.api.adapter

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.mock
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopify.buy3.Storefront
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class OrderAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromOrderStorefrontToOrder() {
        val paginationValue = "pagination_value"
        val result = OrderAdapter.adapt(StorefrontMockInstantiator.newOrder(), paginationValue)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, result.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_CURRENCY_CODE.name, result.currency)
        assertEquals(StorefrontMockInstantiator.DEFAULT_EMAIL, result.email)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ORDER_NUMBER, result.orderNumber)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, result.totalPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, result.subtotalPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, result.totalShippingPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DATE.toDate(), result.processedAt)
        assertNotNull(result.orderProducts.first())
        assertEquals(paginationValue, result.paginationValue)
    }

    @Test
    fun shouldRemoveSingleOptions() {
        val variant = Storefront.ProductVariant(StorefrontMockInstantiator.newID())
        variant.selectedOptions = listOf(StorefrontMockInstantiator.newSelectedOption())
        variant.title = StorefrontMockInstantiator.DEFAULT_TITLE
        variant.price = StorefrontMockInstantiator.DEFAULT_PRICE
        variant.availableForSale = true
        variant.product = StorefrontMockInstantiator.newProduct()

        val order = StorefrontMockInstantiator.newOrder()
        val node = order.lineItems.edges.first().node
        given(node.variant).willReturn(variant)

        val product = node.variant.product
        val option = StorefrontMockInstantiator.newProductOption()
        given(product.options).willReturn(listOf(option))
        given(option.values).willReturn(listOf(""))

        val result = OrderAdapter.adapt(order)
        assertEquals(1, result.orderProducts.size)
        assertEquals(1, result.orderProducts.first().productVariant.selectedOptions.size)

        val resultWithoutOptions = OrderAdapter.adapt(order, isRemoveSingleOptions = true)
        assertEquals(1, resultWithoutOptions.orderProducts.size)
        assertEquals(0, resultWithoutOptions.orderProducts.first().productVariant.selectedOptions.size)
    }

    @Test
    fun shouldAdaptAddress() {
        val paginationValue = "pagination_value"
        val result = OrderAdapter.adapt(StorefrontMockInstantiator.newOrder(), paginationValue)
        assertNotNull(result.address)
    }

    @Test
    fun shouldReturnNullAddress() {
        val paginationValue = "pagination_value"
        val order = StorefrontMockInstantiator.newOrder()
        given(order.shippingAddress).willReturn(null)
        val result = OrderAdapter.adapt(order, paginationValue)
        assertNull(result.address)
    }

    @Test
    fun shouldIgnoreNullProductItems() {
        val paginationValue = "pagination_value"
        val product: Storefront.OrderLineItem? = null
        val productEdge: Storefront.OrderLineItemEdge = mock {
            on { node } doReturn product
        }

        val products: MutableList<Storefront.OrderLineItemEdge?> = mutableListOf()
        products.addAll(StorefrontMockInstantiator.newList(StorefrontMockInstantiator.newOrderLineItemEdge()))
        products.add(productEdge)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE + 1, products.size)

        val order = StorefrontMockInstantiator.newOrder()
        val connection: Storefront.OrderLineItemConnection = mock {
            on { edges } doReturn products
        }
        given(order.lineItems).willReturn(connection)

        val result = OrderAdapter.adapt(order, paginationValue)
        assertNotNull(result.orderProducts)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, result.orderProducts.size)
    }
}