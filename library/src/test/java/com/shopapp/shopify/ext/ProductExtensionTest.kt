package com.shopapp.shopify.ext

import com.nhaarman.mockito_kotlin.given
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.api.ext.isSingleOptions
import com.shopify.buy3.Storefront
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class ProductExtensionTest {

    @Test
    fun shouldReturnFalse() {
        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        assertFalse(storefrontProduct.isSingleOptions())
    }

    @Test
    fun shouldReturnTrueIfOnlySingleOptionExists() {
        val defaultValue = listOf(StorefrontMockInstantiator.DEFAULT_TITLE)
        val option: Storefront.ProductOption = StorefrontMockInstantiator.newProductOption()
        given(option.values).willReturn(defaultValue)

        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        given(storefrontProduct.options).willReturn(listOf(option))
        assertTrue(storefrontProduct.isSingleOptions())
    }
}