package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.Error
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopify.buy3.GraphError
import com.shopify.buy3.GraphNetworkError
import com.shopify.buy3.Storefront
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ErrorAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromGraphNetworkErrorToContentError() {
        val actual = ErrorAdapter.adapt(GraphNetworkError())
        assertTrue(actual is Error.Content)
        val error = actual as Error.Content
        assertTrue(error.isNetworkError)
    }

    @Test
    fun shouldAdaptFromGraphErrorToContentError() {
        val actual = ErrorAdapter.adapt(GraphError())
        assertTrue(actual is Error.Content)
        val error = actual as Error.Content
        assertFalse(error.isNetworkError)
    }

    @Test
    fun shouldAdaptShopifyErrorToNonCriticalError() {
        val errors = listOf(com.shopify.graphql.support.Error(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE))
        val actual = ErrorAdapter.adaptErrors(errors)
        assertTrue(actual is Error.NonCritical)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, actual?.message)
    }

    @Test
    fun shouldReturnNullOnNullErrorList() {
        val errors: List<com.shopify.graphql.support.Error>? = null
        val actual = ErrorAdapter.adaptErrors(errors)
        assertNull(actual)
    }

    @Test
    fun shouldAdaptStorefrontUserErrorToNonCriticalError() {
        val errors = listOf(StorefrontMockInstantiator.newUserError())
        val actual = ErrorAdapter.adaptUserError(errors)
        assertTrue(actual is Error.NonCritical)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, actual?.message)
    }

    @Test
    fun shouldReturnNullOnNullUserErrorList() {
        val errors: List<Storefront.UserError>? = null
        val actual = ErrorAdapter.adaptUserError(errors)
        assertNull(actual)
    }
}