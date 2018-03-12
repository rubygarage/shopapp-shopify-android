package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AuthorAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromAuthorStorefrontToAuthor() {
        val result = AuthorAdapter.adapt(StorefrontMockInstantiator.newAuthor())
        assertEquals(StorefrontMockInstantiator.DEFAULT_FIRST_NAME, result.firstName)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LAST_NAME, result.lastName)
        assertEquals(StorefrontMockInstantiator.DEFAULT_NAME, result.fullName)
        assertEquals(StorefrontMockInstantiator.DEFAULT_BIO, result.bio)
        assertEquals(StorefrontMockInstantiator.DEFAULT_EMAIL, result.email)
    }
}