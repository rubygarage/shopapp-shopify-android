package com.shopapp.shopify.api.adapter

import com.shopapp.gateway.entity.CardType
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopify.buy3.Storefront
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CardAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromStorefrontCardBrandToCardType() {
        assertEquals(CardType.AMERICAN_EXPRESS, CardAdapter.adapt(Storefront.CardBrand.AMERICAN_EXPRESS))
        assertEquals(CardType.DINERS_CLUB, CardAdapter.adapt(Storefront.CardBrand.DINERS_CLUB))
        assertEquals(CardType.DISCOVER, CardAdapter.adapt(Storefront.CardBrand.DISCOVER))
        assertEquals(CardType.JCB, CardAdapter.adapt(Storefront.CardBrand.JCB))
        assertEquals(CardType.MASTER_CARD, CardAdapter.adapt(Storefront.CardBrand.MASTERCARD))
        assertEquals(CardType.VISA, CardAdapter.adapt(Storefront.CardBrand.VISA))
        assertNull(CardAdapter.adapt(Storefront.CardBrand.UNKNOWN_VALUE))
    }

    @Test
    fun shouldAdaptFromListStorefrontCardBrandsToListCardType() {
        val srcList = listOf(Storefront.CardBrand.VISA, Storefront.CardBrand.MASTERCARD, Storefront.CardBrand.UNKNOWN_VALUE)
        val result = CardAdapter.adapt(srcList)
        assertEquals(2, result.size)
        assertEquals(CardType.VISA, result[0])
        assertEquals(CardType.MASTER_CARD, result[1])
    }
}