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
class ShopAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromQueryRootStorefrontToShop() {
        val shop = ShopAdapter.adapt(StorefrontMockInstantiator.newQueryRoot())
        assertEquals(StorefrontMockInstantiator.DEFAULT_SHOP_NAME, shop.name)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DESCRIPTION, shop.description)
        assertNotNull(shop.privacyPolicy)
        assertNotNull(shop.refundPolicy)
        assertNotNull(shop.termsOfService)
    }
}