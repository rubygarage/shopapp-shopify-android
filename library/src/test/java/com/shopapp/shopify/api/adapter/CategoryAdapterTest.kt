package com.shopapp.shopify.api.adapter

import com.nhaarman.mockito_kotlin.given
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.constant.Constant
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
        assertNotNull(category.image)
        assertNotNull(category.productList)
        assertNotNull(category.image)
    }


    @Test
    fun shouldSetEmptyAdditionalDescription() {
        val shop = StorefrontMockInstantiator.newShop()
        val collection = StorefrontMockInstantiator.newCollection()
        given(collection.descriptionHtml).willReturn(null)
        val category = CategoryAdapter.adapt(shop, collection)
        assertEquals(Constant.DEFAULT_STRING, category.additionalDescription)
    }

    @Test
    fun shouldAdaptAdditionalDescription() {
        val shop = StorefrontMockInstantiator.newShop()
        val collection = StorefrontMockInstantiator.newCollection()
        val category = CategoryAdapter.adapt(shop, collection)
        assertEquals(StorefrontMockInstantiator.DEFAULT_HTML, category.additionalDescription)
    }
}