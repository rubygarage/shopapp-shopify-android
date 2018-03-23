package com.shopapp.shopify.api

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.entity.Address
import com.shopapp.gateway.entity.Card
import com.shopapp.gateway.entity.Checkout
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.util.AssetsReader
import com.shopify.buy3.*
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.Retrofit
import java.math.BigDecimal

abstract class BaseShopifyApiTest {

    companion object {
        const val BASE_URL = "base_url"
    }

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Mock
    protected lateinit var context: Context

    @Mock
    protected lateinit var graphClient: GraphClient

    @Mock
    protected lateinit var retrofit: Retrofit

    @Mock
    protected lateinit var cardClient: CardClient

    @Mock
    protected lateinit var sharedPreferences: SharedPreferences

    @Mock
    protected lateinit var assetsReader: AssetsReader

    protected lateinit var api: ShopifyApi

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockSharedPreferences()
        api = ShopifyApi(context, graphClient, retrofit, cardClient, sharedPreferences, assetsReader, BASE_URL)
    }

    protected fun mockSharedPreferences() {
        given(context.getSharedPreferences(any(), any())).willReturn(sharedPreferences)
        val editor: SharedPreferences.Editor = mock()
        given(sharedPreferences.edit()).willReturn(editor)
        given(editor.putString(any(), any())).willReturn(editor)
        given(editor.putLong(any(), any())).willReturn(editor)
        given(editor.remove(any())).willReturn(editor)
    }

    protected fun mockSession(isSessionValid: Boolean) {
        given(sharedPreferences.getString(any(), anyOrNull()))
            .willReturn(if (isSessionValid) "data" else null)
        given(sharedPreferences.getLong(any(), any()))
            .willReturn(if (isSessionValid) System.currentTimeMillis() * 2 else 0)
    }

    protected fun mockSuccessRequestToken(
        graphResponse: GraphResponse<Storefront.Mutation>,
        mutationGraphCall: MutationGraphCall,
        storefrontMutation: Storefront.Mutation
    ) {
        given(mutationGraphCall.execute()).willReturn(graphResponse)

        val customerAccessToken: Storefront.CustomerAccessToken = mock()
        given(customerAccessToken.accessToken).willReturn("123")
        given(customerAccessToken.expiresAt).willReturn(DateTime())

        val customerAccessTokenCreatePayload: Storefront.CustomerAccessTokenCreatePayload = mock()
        given(customerAccessTokenCreatePayload.customerAccessToken).willReturn(customerAccessToken)
        given(storefrontMutation.customerAccessTokenCreate).willReturn(customerAccessTokenCreatePayload)
        given(graphResponse.data()).willReturn(storefrontMutation)
    }

    protected fun mockFailureRequestToken(
        graphResponse: GraphResponse<Storefront.Mutation>,
        mutationGraphCall: MutationGraphCall,
        storefrontMutation: Storefront.Mutation
    ) {
        given(mutationGraphCall.execute()).willReturn(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()

        val customerAccessTokenCreatePayload: Storefront.CustomerAccessTokenCreatePayload = mock()
        given(customerAccessTokenCreatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerAccessTokenCreate).willReturn(customerAccessTokenCreatePayload)
        given(graphResponse.data()).willReturn(storefrontMutation)
    }

    protected fun mockMutationGraphCallWithOnResponse(response: GraphResponse<Storefront.Mutation>): MutationGraphCall {
        val mutationGraphCall: MutationGraphCall = mock()
        given(graphClient.mutateGraph(any())).willReturn(mutationGraphCall)

        given(mutationGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.Mutation>>(0)
            graphCallback.onResponse(response)
            mutationGraphCall
        })

        return mutationGraphCall
    }

    protected fun mockMutationGraphCallWithOnFailure(): MutationGraphCall {
        val mutationGraphCall: MutationGraphCall = mock()
        given(graphClient.mutateGraph(any())).willReturn(mutationGraphCall)

        given(mutationGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.Mutation>>(0)
            graphCallback.onFailure(StorefrontMockInstantiator.newGraphError())
            mutationGraphCall
        })

        return mutationGraphCall
    }

    protected fun mockQueryGraphCallWithOnResponse(response: GraphResponse<Storefront.QueryRoot>): QueryGraphCall {
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onResponse(response)
            queryGraphCall
        })

        return queryGraphCall
    }

    protected fun mockQueryGraphCallWithOnFailure() {
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onFailure(StorefrontMockInstantiator.newGraphError())
            queryGraphCall
        })
    }

    protected fun mockMutationDataResponse(): Pair<GraphResponse<Storefront.Mutation>, Storefront.Mutation> {
        val graphResponse: GraphResponse<Storefront.Mutation> = mock()
        val storefrontQueryRoot: Storefront.Mutation = mock()
        given(graphResponse.data()).willReturn(storefrontQueryRoot)
        return graphResponse to storefrontQueryRoot
    }

    protected fun mockDataResponse(): Pair<GraphResponse<Storefront.QueryRoot>, Storefront.QueryRoot> {
        val graphResponse: GraphResponse<Storefront.QueryRoot> = mock()
        val storefrontQueryRoot: Storefront.QueryRoot = mock()
        given(graphResponse.data()).willReturn(storefrontQueryRoot)
        return graphResponse to storefrontQueryRoot
    }

    protected fun mockErrorResponse(): GraphResponse<Storefront.QueryRoot> {
        val error: com.shopify.graphql.support.Error = mock {
            on { message() }.doReturn(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE)
        }
        val graphResponse: GraphResponse<Storefront.QueryRoot> = mock()
        given(graphResponse.errors()).willReturn(listOf(error))
        return graphResponse
    }

    protected fun stubAddress() =
        Address(address = "address", secondAddress = "secondAddress", city = "city",
            country = "country", state = "state", firstName = "firstName",
            lastName = "lastName", zip = "zip", phone = "phone")

    protected fun stubCard() = Card("firstName", "lastName", "cardNumber",
        "expireMonth", "expireYear", "123")

    protected fun stubCheckout() = Checkout("", "", false,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", stubAddress(), null)
}