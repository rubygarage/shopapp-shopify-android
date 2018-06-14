package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.*
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopify.buy3.CreditCardVaultCall
import com.shopify.buy3.GraphCall
import com.shopify.buy3.QueryGraphCall
import com.shopify.buy3.Storefront
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

class ShopifyApiCheckoutTest : BaseShopifyApiTest() {

    @Test
    fun createCheckoutShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCheckout = StorefrontMockInstantiator.newCheckout()
        val checkoutCreatePayload: Storefront.CheckoutCreatePayload = mock()
        given(checkoutCreatePayload.checkout).willReturn(storefrontCheckout)
        given(storefrontMutation.checkoutCreate).willReturn(checkoutCreatePayload)

        val callback: ApiCallback<Checkout> = mock()
        api.createCheckout(listOf(), callback)

        argumentCaptor<Checkout>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontCheckout.id.toString(), firstValue.checkoutId)
        }
    }

    @Test
    fun createCheckoutShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val checkoutCreatePayload: Storefront.CheckoutCreatePayload = mock()
        given(checkoutCreatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.checkoutCreate).willReturn(checkoutCreatePayload)

        val callback: ApiCallback<Checkout> = mock()
        api.createCheckout(listOf(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun createCheckoutShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        val callback: ApiCallback<Checkout> = mock()
        api.createCheckout(listOf(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCheckoutShouldReturnCheckout() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontCheckout = StorefrontMockInstantiator.newCheckout()
        given(storefrontQueryRoot.node).willReturn(storefrontCheckout)

        val callback: ApiCallback<Checkout> = mock()
        api.getCheckout(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Checkout>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontCheckout.id.toString(), firstValue.checkoutId)
        }
    }

    @Test
    fun getCheckoutShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Checkout> = mock()
        api.getCheckout(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getCheckoutShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Checkout> = mock()
        api.getCheckout(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCheckoutShouldReturnCriticalError() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        given(storefrontQueryRoot.node).willReturn(null)

        val callback: ApiCallback<Checkout> = mock()
        api.getCheckout(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Critical)
        }
    }

    @Test
    fun setShippingAddressShouldReturnCheckout() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCheckout = StorefrontMockInstantiator.newCheckout()
        val checkoutShippingAddressUpdatePayload: Storefront.CheckoutShippingAddressUpdatePayload = mock()
        given(checkoutShippingAddressUpdatePayload.checkout).willReturn(storefrontCheckout)
        given(storefrontMutation.checkoutShippingAddressUpdate).willReturn(checkoutShippingAddressUpdatePayload)

        val address = stubAddress()
        val callback: ApiCallback<Checkout> = mock()
        api.setShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        argumentCaptor<Checkout>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontCheckout.id.toString(), firstValue.checkoutId)
        }
    }

    @Test
    fun setShippingAddressShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val checkoutCreatePayload: Storefront.CheckoutShippingAddressUpdatePayload = mock()
        given(checkoutCreatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.checkoutShippingAddressUpdate).willReturn(checkoutCreatePayload)

        val address = stubAddress()
        val callback: ApiCallback<Checkout> = mock()
        api.setShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun setShippingAddressShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        val address = stubAddress()
        val callback: ApiCallback<Checkout> = mock()
        api.setShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getShippingRatesShouldReturnShippingRates() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any(), anyOrNull(), any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onResponse(graphResponse)
            queryGraphCall
        })

        val storefrontCheckout = StorefrontMockInstantiator.newCheckout()
        given(storefrontQueryRoot.node).willReturn(storefrontCheckout)

        val callback: ApiCallback<List<ShippingRate>> = mock()
        api.getShippingRates(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<List<ShippingRate>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())
        }
    }

    @Test
    fun getShippingRatesShouldReturnContentError() {
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any(), anyOrNull(), anyOrNull())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onFailure(StorefrontMockInstantiator.newGraphError())
            queryGraphCall
        })

        val callback: ApiCallback<List<ShippingRate>> = mock()
        api.getShippingRates(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun selectShippingRateShouldReturnCheckout() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCheckout = StorefrontMockInstantiator.newCheckout()
        val checkoutShippingLineUpdatePayload: Storefront.CheckoutShippingLineUpdatePayload = mock()
        given(checkoutShippingLineUpdatePayload.checkout).willReturn(storefrontCheckout)
        given(storefrontMutation.checkoutShippingLineUpdate).willReturn(checkoutShippingLineUpdatePayload)

        val shippingRate = ShippingRate("title", BigDecimal.ZERO, "handle")
        val callback: ApiCallback<Checkout> = mock()
        api.setShippingRate(StorefrontMockInstantiator.DEFAULT_ID, shippingRate, callback)

        argumentCaptor<Checkout>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontCheckout.id.toString(), firstValue.checkoutId)
        }
    }

    @Test
    fun selectShippingRateShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val checkoutShippingLineUpdatePayload: Storefront.CheckoutShippingLineUpdatePayload = mock()
        given(checkoutShippingLineUpdatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.checkoutShippingLineUpdate).willReturn(checkoutShippingLineUpdatePayload)

        val shippingRate = ShippingRate("title", BigDecimal.ZERO, "handle")
        val callback: ApiCallback<Checkout> = mock()
        api.setShippingRate(StorefrontMockInstantiator.DEFAULT_ID, shippingRate, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun selectShippingRateShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        val shippingRate = ShippingRate("title", BigDecimal.ZERO, "handle")
        val callback: ApiCallback<Checkout> = mock()
        api.setShippingRate(StorefrontMockInstantiator.DEFAULT_ID, shippingRate, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getAcceptedCardTypesShouldReturnCardTypes() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontCardBrand: Storefront.CardBrand = mock()
        val storefrontShop = StorefrontMockInstantiator.newShop()
        val paymentSettings = storefrontShop.paymentSettings
        given(paymentSettings.acceptedCardBrands).willReturn(listOf(storefrontCardBrand))
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<CardType>> = mock()
        api.getAcceptedCardTypes(callback)

        argumentCaptor<List<CardType>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
        }
    }

    @Test
    fun getAcceptedCardTypesShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<CardType>> = mock()
        api.getAcceptedCardTypes(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getAcceptedCardTypesShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<CardType>> = mock()
        api.getAcceptedCardTypes(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCardTokenShouldReturnString() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val paymentSettings = storefrontShop.paymentSettings
        given(paymentSettings.cardVaultUrl).willReturn("url")
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val token = "test token"
        val creditCardVaultCall: CreditCardVaultCall = mock()
        given(creditCardVaultCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<CreditCardVaultCall.Callback>(0)
            graphCallback.onResponse(token)
            creditCardVaultCall
        })
        given(cardClient.vault(any(), any())).willReturn(creditCardVaultCall)

        val card = stubCard()
        val callback: ApiCallback<String> = mock()
        api.getCardToken(card, callback)

        argumentCaptor<String>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(token, firstValue)
        }
    }

    @Test
    fun getCardTokenShouldReturnContentErrorWhenCardVaultingFailed() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val paymentSettings = storefrontShop.paymentSettings
        given(paymentSettings.cardVaultUrl).willReturn("url")
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val creditCardVaultCall: CreditCardVaultCall = mock()
        given(creditCardVaultCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<CreditCardVaultCall.Callback>(0)
            graphCallback.onFailure(IOException())
            creditCardVaultCall
        })
        given(cardClient.vault(any(), any())).willReturn(creditCardVaultCall)

        val card = stubCard()
        val callback: ApiCallback<String> = mock()
        api.getCardToken(card, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCardTokenShouldReturnNonCriticalError() {
        val graphResponse = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val card = stubCard()
        val callback: ApiCallback<String> = mock()
        api.getCardToken(card, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getCardTokenShouldReturnContentError() {
        mockDataResponse()
        mockQueryGraphCallWithOnFailure()

        val card = stubCard()
        val callback: ApiCallback<String> = mock()
        api.getCardToken(card, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun completeCheckoutByCardShouldReturnOrder() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCheckout = StorefrontMockInstantiator.newCheckout()
        given(storefrontCheckout.ready).willReturn(true)
        val checkoutCompleteWithCreditCardPayload: Storefront.CheckoutCompleteWithCreditCardPayload = mock()
        given(checkoutCompleteWithCreditCardPayload.checkout).willReturn(storefrontCheckout)
        given(storefrontMutation.checkoutCompleteWithCreditCard).willReturn(checkoutCompleteWithCreditCardPayload)

        val (graphQueryResponse, storefrontQueryRoot) = mockDataResponse()
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any(), anyOrNull(), anyOrNull())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onResponse(graphQueryResponse)
            queryGraphCall
        })

        given(storefrontQueryRoot.node).willReturn(storefrontCheckout)

        val checkout = stubCheckout()
        val callback: ApiCallback<Order> = mock()
        api.completeCheckoutByCard(checkout, "", stubAddress(), "", callback)

        argumentCaptor<Order>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontCheckout.order.id.toString(), firstValue.id)
        }
    }

    @Test
    fun completeCheckoutByCardShouldReturnContentErrorWhenCompleteCheckoutFailed() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCheckout = StorefrontMockInstantiator.newCheckout()
        given(storefrontCheckout.ready).willReturn(true)
        val checkoutCompleteWithCreditCardPayload: Storefront.CheckoutCompleteWithCreditCardPayload = mock()
        given(checkoutCompleteWithCreditCardPayload.checkout).willReturn(storefrontCheckout)
        given(storefrontMutation.checkoutCompleteWithCreditCard).willReturn(checkoutCompleteWithCreditCardPayload)

        mockDataResponse()
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any(), anyOrNull(), anyOrNull())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onFailure(StorefrontMockInstantiator.newGraphError())
            queryGraphCall
        })

        val checkout = stubCheckout()
        val callback: ApiCallback<Order> = mock()
        api.completeCheckoutByCard(checkout, "", stubAddress(), "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun completeCheckoutShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val checkoutCompleteWithCreditCardPayload: Storefront.CheckoutCompleteWithCreditCardPayload = mock()
        given(checkoutCompleteWithCreditCardPayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.checkoutCompleteWithCreditCard).willReturn(checkoutCompleteWithCreditCardPayload)

        val checkout = stubCheckout()
        val callback: ApiCallback<Order> = mock()
        api.completeCheckoutByCard(checkout, "", stubAddress(), "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun completeCheckoutShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        val checkout = stubCheckout()
        val callback: ApiCallback<Order> = mock()
        api.completeCheckoutByCard(checkout, "", stubAddress(), "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }
}