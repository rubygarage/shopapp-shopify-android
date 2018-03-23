package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ArticleAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromArticleStorefrontToArticle() {
        val paginationValue = "pagination_value"
        val result = ArticleAdapter.adapt(StorefrontMockInstantiator.newArticle(), paginationValue)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, result.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, result.title)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, result.blogTitle)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, result.blogId)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DESCRIPTION, result.content)
        assertEquals(StorefrontMockInstantiator.DEFAULT_HTML, result.contentHTML)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DATE.toDate(), result.publishedAt)
        assertNotNull(result.author)
        assertNotNull(result.image)
        assertNotNull(result.tags)
        assertEquals(paginationValue, result.paginationValue)
    }
}