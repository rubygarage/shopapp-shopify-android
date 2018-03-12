package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class CategoryAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromCollectionStorefrontToCategory() {
        val shop = StorefrontMockInstantiator.newShop()
        val collection = StorefrontMockInstantiator.newCollection()
        val category = CategoryAdapter.adapt(shop, collection)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, category.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, category.title)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DATE.toDate(), category.updatedAt)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DESCRIPTION, category.categoryDescription)
        assertEquals(StorefrontMockInstantiator.DEFAULT_HTML, category.additionalDescription)
        assertNotNull(category.image)
        assertNotNull(category.productList)
        assertNotNull(category.image)
    }
}