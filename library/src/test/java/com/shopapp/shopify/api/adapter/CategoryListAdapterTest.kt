package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class CategoryListAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromListCollectionStorefrontToListCategory() {
        val shop = StorefrontMockInstantiator.newShop()
        val categoryList = CategoryListAdapter.adapt(shop)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, categoryList.size)
    }
}