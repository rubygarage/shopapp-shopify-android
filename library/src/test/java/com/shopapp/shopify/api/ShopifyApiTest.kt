package com.shopapp.shopify.api

import android.content.Context
import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.*
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.api.entity.ApiCountry
import com.shopapp.shopify.api.entity.ApiCountryResponse
import com.shopapp.shopify.api.retrofit.CountriesService
import com.shopapp.shopify.constant.Constant.REST_OF_WORLD
import com.shopapp.shopify.constant.Constant.UNAUTHORIZED_ERROR
import com.shopapp.shopify.util.AssetsReader
import com.shopify.buy3.*
import okhttp3.ResponseBody
import org.joda.time.DateTime
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.math.BigDecimal

class ShopifyApiTest {

    companion object {
        private const val BASE_URL = "base_url"
    }

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var graphClient: GraphClient

    @Mock
    private lateinit var retrofit: Retrofit

    @Mock
    private lateinit var cardClient: CardClient

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var assetsReader: AssetsReader

    private lateinit var api: ShopifyApi

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockSharedPreferences()
        api = ShopifyApi(context, graphClient, retrofit, cardClient, sharedPreferences, assetsReader, BASE_URL)
    }

    @Test
    fun getProductShouldReturnProduct() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        given(storefrontQueryRoot.node).willReturn(storefrontProduct)

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Product>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontProduct.id.toString(), firstValue.id)
            assertEquals(storefrontShop.paymentSettings.currencyCode.name, firstValue.currency)
        }
    }

    @Test
    fun getProductShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getProductShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getProductListWithFullArgumentsShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val storefrontProducts = StorefrontMockInstantiator.newProductConnection()
        given(storefrontShop.products).willReturn(storefrontProducts)
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Product>> = mock()
        api.getProductList(any(), null, null, null, null, callback)

        argumentCaptor<List<Product>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            val product = firstValue.first()
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, product.id)
            assertEquals(storefrontShop.paymentSettings.currencyCode.name, product.currency)
        }
    }

    @Test
    fun getProductListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Product>> = mock()
        api.getProductList(any(), null, null, "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getProductListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Product>> = mock()
        api.getProductList(any(), null, null, "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun searchProductListShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val storefrontProducts = StorefrontMockInstantiator.newProductConnection()
        given(storefrontShop.products).willReturn(storefrontProducts)
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProductList(any(), null, "", callback)

        argumentCaptor<List<Product>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            val product = firstValue.first()
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, product.id)
            assertEquals(storefrontShop.paymentSettings.currencyCode.name, product.currency)
        }
    }

    @Test
    fun searchProductListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProductList(any(), null, "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun searchProductListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProductList(any(), null, "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCategoryListShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Category>> = mock()
        api.getCategoryList(any(), null, callback)

        argumentCaptor<List<Category>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            val category = firstValue.first()
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, category.id)
        }
    }

    @Test
    fun getCategoryListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Category>> = mock()
        api.getCategoryList(any(), null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getCategoryListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Category>> = mock()
        api.getCategoryList(any(), null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCategoryDetailsShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontCollection = StorefrontMockInstantiator.newCollection()
        given(storefrontQueryRoot.node).willReturn(storefrontCollection)
        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<Category> = mock()
        api.getCategoryDetails(StorefrontMockInstantiator.DEFAULT_ID, any(), null, null, callback)

        argumentCaptor<Category>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, firstValue.id)
            assertTrue(firstValue.productList.isNotEmpty())
        }
    }

    @Test
    fun getCategoryDetailsShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Category> = mock()
        api.getCategoryDetails(StorefrontMockInstantiator.DEFAULT_ID, any(), null, null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getCategoryDetailsShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Category> = mock()
        api.getCategoryDetails(StorefrontMockInstantiator.DEFAULT_ID, any(), null, null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getShopInfoShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<Shop> = mock()
        api.getShopInfo(callback)

        argumentCaptor<Shop>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(StorefrontMockInstantiator.DEFAULT_SHOP_NAME, firstValue.name)
        }
    }

    @Test
    fun getShopInfoShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Shop> = mock()
        api.getShopInfo(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getShopInfoShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Shop> = mock()
        api.getShopInfo(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getArticleShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontArticle = StorefrontMockInstantiator.newArticle()
        given(storefrontQueryRoot.node).willReturn(storefrontArticle)

        val callback: ApiCallback<Pair<Article, String>> = mock()
        api.getArticle(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Pair<Article, String>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            val (article, url) = firstValue
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, article.id)
            assertEquals(BASE_URL, url)
        }
    }

    @Test
    fun getArticleShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Pair<Article, String>> = mock()
        api.getArticle(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getArticleShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Pair<Article, String>> = mock()
        api.getArticle(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getArticleListShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontArticleEdges = listOf(StorefrontMockInstantiator.newArticleEdge())
        val storefrontArticleConnection: Storefront.ArticleConnection = mock()
        given(storefrontArticleConnection.edges).willReturn(storefrontArticleEdges)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)
        given(storefrontShop.articles).willReturn(storefrontArticleConnection)

        val callback: ApiCallback<List<Article>> = mock()
        api.getArticleList(any(), null, null, false, callback)

        argumentCaptor<List<Article>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, firstValue.first().id)
        }
    }

    @Test
    fun getArticleListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Article>> = mock()
        api.getArticleList(any(), null, null, false, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getArticleListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Article>> = mock()
        api.getArticleList(any(), null, null, false, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun signUpShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val customerCreatePayload: Storefront.CustomerCreatePayload = mock()
        given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        given(customerCreatePayload.customer).willReturn(customer)

        mockSuccessRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.signUp("", "", "", "", "", callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun signUpShouldReturnNonCriticalErrorWhenTokenRequestFailed() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val customerCreatePayload: Storefront.CustomerCreatePayload = mock()
        given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        given(customerCreatePayload.customer).willReturn(customer)

        mockFailureRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.signUp("", "", "", "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun signUpShouldReturnNonCriticalErrorWhenSignUpFailed() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val customerCreatePayload: Storefront.CustomerCreatePayload = mock()
        given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        given(customerCreatePayload.customer).willReturn(customer)

        val userError: com.shopify.graphql.support.Error = mock()
        given(userError.message()).willReturn(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE)
        given(graphResponse.errors()).willReturn(listOf(userError))

        val callback: ApiCallback<Unit> = mock()
        api.signUp("", "", "", "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun signUpShouldReturnContentErrorWhenSignUpFailed() {
        val (_, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        val customerCreatePayload: Storefront.CustomerCreatePayload = mock()
        given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        given(customerCreatePayload.customer).willReturn(customer)

        val callback: ApiCallback<Unit> = mock()
        api.signUp("", "", "", "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun signInShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        mockSuccessRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.signIn("", "", callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun signInShouldReturnNonCriticalErrorWhenTokenRequestFailed() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        mockFailureRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.signIn("", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun signInShouldReturnContentErrorWhenTokenRequestFailed() {
        val (graphResponse, _) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        given(mutationGraphCall.execute()).willReturn(graphResponse)
        given(graphResponse.data()).willReturn(null)
        val callback: ApiCallback<Unit> = mock()
        api.signIn("", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun signOutShouldReturnUnit() {
        val callback: ApiCallback<Unit> = mock()
        api.signOut(callback)

        verify(sharedPreferences.edit(), times(3)).remove(any())
        verify(callback).onResult(any())
    }

    @Test
    fun isLoggedInShouldReturnTrueWhenSessionValid() {
        mockSession(true)
        val callback: ApiCallback<Boolean> = mock()
        api.isLoggedIn(callback)

        verify(callback).onResult(true)
    }

    @Test
    fun isLoggedInShouldReturnFalseWhenSessionInvalid() {
        mockSession(false)
        val callback: ApiCallback<Boolean> = mock()
        api.isLoggedIn(callback)

        verify(callback).onResult(false)
    }

    @Test
    fun forgotPasswordShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        mockSuccessRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.signIn("", "", callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun forgotPasswordShouldReturnNonCriticalErrorWithErrorResponse() {
        val (graphResponse, _) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError: com.shopify.graphql.support.Error = mock()
        given(userError.message()).willReturn(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE)
        given(graphResponse.errors()).willReturn(listOf(userError))

        val callback: ApiCallback<Unit> = mock()
        api.forgotPassword("", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun forgotPasswordShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, _) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()

        val customerRecoverPayload: Storefront.CustomerRecoverPayload = mock()
        given(customerRecoverPayload.userErrors).willReturn(listOf(userError))
        given(graphResponse.data()?.customerRecover).willReturn(customerRecoverPayload)

        val callback: ApiCallback<Unit> = mock()
        api.forgotPassword("", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun forgotPasswordShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        val callback: ApiCallback<Unit> = mock()
        api.forgotPassword("", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCustomerShouldReturnCustomer() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val customer = StorefrontMockInstantiator.newCustomer()
        given(storefrontQueryRoot.customer).willReturn(customer)

        mockSession(true)
        val callback: ApiCallback<Customer?> = mock()
        api.getCustomer(callback)

        argumentCaptor<Customer>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertNotNull(firstValue)
            assertEquals(customer.id.toString(), firstValue.id)
        }
    }

    @Test
    fun getCustomerShouldReturnContentErrorAndCleanSession() {
        val (graphResponse, _) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        mockSession(true)
        val callback: ApiCallback<Customer?> = mock()
        api.getCustomer(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())
            verify(sharedPreferences.edit(), times(3)).remove(any())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCustomerShouldReturnNonCriticalWhenUserNotAuthorized() {
        mockSession(false)
        val callback: ApiCallback<Customer?> = mock()
        api.getCustomer(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getCustomerShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<Customer?> = mock()
        api.getCustomer(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun createCustomerAddressShouldReturnCustomerId() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontAddress = StorefrontMockInstantiator.newAddress()
        val customerAddressCreatePayload: Storefront.CustomerAddressCreatePayload = mock()
        given(customerAddressCreatePayload.customerAddress).willReturn(storefrontAddress)
        given(storefrontMutation.customerAddressCreate).willReturn(customerAddressCreatePayload)

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<String> = mock()
        api.createCustomerAddress(address, callback)

        argumentCaptor<String>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontAddress.id.toString(), firstValue)
        }
    }

    @Test
    fun createCustomerAddressShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerAddressCreatePayload: Storefront.CustomerAddressCreatePayload = mock()
        given(customerAddressCreatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerAddressCreate).willReturn(customerAddressCreatePayload)

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<String> = mock()
        api.createCustomerAddress(address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun createCustomerAddressShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<String> = mock()
        api.createCustomerAddress(address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun createCustomerAddressShouldReturnContentErrorWhenSessionInvalid() {
        mockSession(false)
        val address: Address = mock()
        val callback: ApiCallback<String> = mock()
        api.createCustomerAddress(address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun editCustomerAddressShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontAddress = StorefrontMockInstantiator.newAddress()
        val customerAddressUpdatePayload: Storefront.CustomerAddressUpdatePayload = mock()
        given(customerAddressUpdatePayload.customerAddress).willReturn(storefrontAddress)
        given(storefrontMutation.customerAddressUpdate).willReturn(customerAddressUpdatePayload)

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<Unit> = mock()
        api.editCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun editCustomerAddressShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerAddressUpdatePayload: Storefront.CustomerAddressUpdatePayload = mock()
        given(customerAddressUpdatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerAddressUpdate).willReturn(customerAddressUpdatePayload)

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<Unit> = mock()
        api.editCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun editCustomerAddressShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<Unit> = mock()
        api.editCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun editCustomerAddressShouldReturnContentErrorWhenSessionInvalid() {
        mockSession(false)
        val address: Address = mock()
        val callback: ApiCallback<Unit> = mock()
        api.editCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun deleteCustomerAddressShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val customerAddressDeletePayload: Storefront.CustomerAddressDeletePayload = mock()
        given(customerAddressDeletePayload.deletedCustomerAddressId).willReturn(StorefrontMockInstantiator.DEFAULT_ID)
        given(storefrontMutation.customerAddressDelete).willReturn(customerAddressDeletePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.deleteCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun deleteCustomerAddressShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerAddressDeletePayload: Storefront.CustomerAddressDeletePayload = mock()
        given(customerAddressDeletePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerAddressDelete).willReturn(customerAddressDeletePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.deleteCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun deleteCustomerAddressShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.deleteCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun deleteCustomerAddressShouldReturnContentErrorWhenSessionInvalid() {
        mockSession(false)
        val callback: ApiCallback<Unit> = mock()
        api.deleteCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun setDefaultShippingAddressShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val customerDefaultAddressUpdatePayload: Storefront.CustomerDefaultAddressUpdatePayload = mock()
        given(storefrontMutation.customerDefaultAddressUpdate).willReturn(customerDefaultAddressUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.setDefaultShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun setDefaultShippingAddressShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerDefaultAddressUpdatePayload: Storefront.CustomerDefaultAddressUpdatePayload = mock()
        given(customerDefaultAddressUpdatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerDefaultAddressUpdate).willReturn(customerDefaultAddressUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.setDefaultShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun setDefaultShippingAddressShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.setDefaultShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun setDefaultShippingAddressShouldReturnContentErrorWhenSessionInvalid() {
        mockSession(false)
        val callback: ApiCallback<Unit> = mock()
        api.setDefaultShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

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
    fun getCheckoutShouldReturnProductList() {
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
        api.selectShippingRate(StorefrontMockInstantiator.DEFAULT_ID, shippingRate, callback)

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
        api.selectShippingRate(StorefrontMockInstantiator.DEFAULT_ID, shippingRate, callback)

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
        api.selectShippingRate(StorefrontMockInstantiator.DEFAULT_ID, shippingRate, callback)

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
    fun getOrdersShouldReturnOrderList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontOrderConnection = StorefrontMockInstantiator.newOrderConnection()
        val customer = StorefrontMockInstantiator.newCustomer()
        given(storefrontQueryRoot.customer).willReturn(customer)
        given(customer.orders).willReturn(storefrontOrderConnection)

        mockSession(true)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<List<Order>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
        }
    }

    @Test
    fun getOrdersShouldReturnNonCriticalError() {
        val graphResponse = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        mockSession(true)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getOrdersShouldReturnNonCriticalWhenUserNotAuthorized() {
        mockSession(false)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getOrdersShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getOrderShouldReturnOrder() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontOrder = StorefrontMockInstantiator.newOrder()
        given(storefrontQueryRoot.node).willReturn(storefrontOrder)

        val callback: ApiCallback<Order> = mock()
        api.getOrder(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Order>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontOrder.id.toString(), firstValue.id)
        }
    }

    @Test
    fun getOrderShouldReturnNonCriticalError() {
        val graphResponse = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val callback: ApiCallback<Order> = mock()
        api.getOrder(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getOrderShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Order> = mock()
        api.getOrder(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun editCustomerInfoShouldReturnCustomer() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCustomer = StorefrontMockInstantiator.newCustomer()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Customer> = mock()
        api.editCustomerInfo("firstName", "lastName", "phone", callback)

        argumentCaptor<Customer>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontCustomer.id.toString(), firstValue.id)
        }
    }

    @Test
    fun editCustomerInfoShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        given(customerUpdatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Customer> = mock()
        api.editCustomerInfo("firstName", "lastName", "phone", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun editCustomerInfoShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<Customer> = mock()
        api.editCustomerInfo("firstName", "lastName", "phone", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun editCustomerInfoShouldReturnContentErrorWhenSessionInvalid() {
        mockSession(false)
        val callback: ApiCallback<Customer> = mock()
        api.editCustomerInfo("firstName", "lastName", "phone", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun changePasswordShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCustomer = StorefrontMockInstantiator.newCustomer()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        mockSuccessRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.changePassword("password", callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun changePasswordShouldReturnUserErrorWhenSessionRequestFailed() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCustomer = StorefrontMockInstantiator.newCustomer()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        mockFailureRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.changePassword("password", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun changePasswordShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        given(customerUpdatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        mockSuccessRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.changePassword("password", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun changePasswordShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.changePassword("password", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun changePasswordShouldReturnContentErrorWhenSessionInvalid() {
        mockSession(false)
        val callback: ApiCallback<Unit> = mock()
        api.changePassword("password", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun updateCustomerSettingsShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCustomer = StorefrontMockInstantiator.newCustomer()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.updateCustomerSettings(true, callback)

        verify(callback).onResult(Unit)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun updateCustomerSettingsShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        given(customerUpdatePayload.userErrors).willReturn(listOf(userError))
        given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.updateCustomerSettings(true, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun updateCustomerSettingsShouldReturnContentError() {
        mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.updateCustomerSettings(true, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun updateCustomerSettingsShouldReturnContentErrorWhenSessionInvalid() {
        mockSession(false)
        val callback: ApiCallback<Unit> = mock()
        api.updateCustomerSettings(true, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun getCountriesShouldReturnCountriesListFromApi() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        given(countriesService.getCountries()).willReturn(call)
        given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        val response: Response<ApiCountryResponse> = mock()
        given(response.isSuccessful).willReturn(true)
        val apiCountry = ApiCountry(123, "code", "name", listOf())
        val apiResponse = ApiCountryResponse(listOf(apiCountry))
        given(response.body()).willReturn(apiResponse)

        given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onResponse(call, response)
        })

        val callback: ApiCallback<List<Country>> = mock()
        api.getCountries(callback)

        argumentCaptor<List<Country>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(apiResponse.countries.size, firstValue.size)
        }
    }

    @Test
    fun getCountriesShouldReturnCountriesListFromJson() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        given(countriesService.getCountries()).willReturn(call)
        given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        val response: Response<ApiCountryResponse> = mock()
        given(response.isSuccessful).willReturn(true)
        val apiCountry = ApiCountry(123, "code", REST_OF_WORLD, listOf())
        val apiResponse = ApiCountryResponse(listOf(apiCountry))
        given(response.body()).willReturn(apiResponse)

        given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onResponse(call, response)
        })

        val countryId = 38097911857L
        val countryName = "Afghanistan"
        given(assetsReader.read(any(), any())).willReturn("[\n" +
                "  {\n" +
                "    \"id\": $countryId,\n" +
                "    \"name\": \"$countryName\",\n" +
                "    \"tax\": 0,\n" +
                "    \"code\": \"AF\",\n" +
                "    \"tax_name\": \"VAT\",\n" +
                "    \"provinces\": []\n" +
                "  }" +
                "]")

        val callback: ApiCallback<List<Country>> = mock()
        api.getCountries(callback)

        argumentCaptor<List<Country>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            assertEquals(countryId, firstValue.first().id)
            assertEquals(countryName, firstValue.first().name)
        }
    }

    @Test
    fun getCountriesShouldReturnContentErrorWhenResponseIsNotSuccessful() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        given(countriesService.getCountries()).willReturn(call)
        given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        val response: Response<ApiCountryResponse> = mock()
        given(response.isSuccessful).willReturn(false)
        val responseBody: ResponseBody = mock()
        given(response.errorBody()).willReturn(responseBody)

        given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onResponse(call, response)
        })

        val callback: ApiCallback<List<Country>> = mock()
        api.getCountries(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCountriesShouldReturnContentError() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        given(countriesService.getCountries()).willReturn(call)
        given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onFailure(call, Throwable())
        })

        val callback: ApiCallback<List<Country>> = mock()
        api.getCountries(callback)

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

    private fun mockSharedPreferences() {
        given(context.getSharedPreferences(any(), any())).willReturn(sharedPreferences)
        val editor: SharedPreferences.Editor = mock()
        given(sharedPreferences.edit()).willReturn(editor)
        given(editor.putString(any(), any())).willReturn(editor)
        given(editor.putLong(any(), any())).willReturn(editor)
        given(editor.remove(any())).willReturn(editor)
    }

    private fun mockSession(isSessionValid: Boolean) {
        given(sharedPreferences.getString(any(), anyOrNull()))
            .willReturn(if (isSessionValid) "data" else null)
        given(sharedPreferences.getLong(any(), any()))
            .willReturn(if (isSessionValid) System.currentTimeMillis() * 2 else 0)
    }

    private fun mockSuccessRequestToken(
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

    private fun mockFailureRequestToken(
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

    private fun mockMutationGraphCallWithOnResponse(response: GraphResponse<Storefront.Mutation>): MutationGraphCall {
        val mutationGraphCall: MutationGraphCall = mock()
        given(graphClient.mutateGraph(any())).willReturn(mutationGraphCall)

        given(mutationGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.Mutation>>(0)
            graphCallback.onResponse(response)
            mutationGraphCall
        })

        return mutationGraphCall
    }

    private fun mockMutationGraphCallWithOnFailure(): MutationGraphCall {
        val mutationGraphCall: MutationGraphCall = mock()
        given(graphClient.mutateGraph(any())).willReturn(mutationGraphCall)

        given(mutationGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.Mutation>>(0)
            graphCallback.onFailure(StorefrontMockInstantiator.newGraphError())
            mutationGraphCall
        })

        return mutationGraphCall
    }

    private fun mockQueryGraphCallWithOnResponse(response: GraphResponse<Storefront.QueryRoot>): QueryGraphCall {
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onResponse(response)
            queryGraphCall
        })

        return queryGraphCall
    }

    private fun mockQueryGraphCallWithOnFailure() {
        val queryGraphCall: QueryGraphCall = mock()
        given(graphClient.queryGraph(any())).willReturn(queryGraphCall)

        given(queryGraphCall.enqueue(any())).willAnswer({
            val graphCallback = it.getArgument<GraphCall.Callback<Storefront.QueryRoot>>(0)
            graphCallback.onFailure(StorefrontMockInstantiator.newGraphError())
            queryGraphCall
        })
    }

    private fun mockMutationDataResponse(): Pair<GraphResponse<Storefront.Mutation>, Storefront.Mutation> {
        val graphResponse: GraphResponse<Storefront.Mutation> = mock()
        val storefrontQueryRoot: Storefront.Mutation = mock()
        given(graphResponse.data()).willReturn(storefrontQueryRoot)
        return graphResponse to storefrontQueryRoot
    }

    private fun mockDataResponse(): Pair<GraphResponse<Storefront.QueryRoot>, Storefront.QueryRoot> {
        val graphResponse: GraphResponse<Storefront.QueryRoot> = mock()
        val storefrontQueryRoot: Storefront.QueryRoot = mock()
        given(graphResponse.data()).willReturn(storefrontQueryRoot)
        return graphResponse to storefrontQueryRoot
    }

    private fun mockErrorResponse(): GraphResponse<Storefront.QueryRoot> {
        val error: com.shopify.graphql.support.Error = mock {
            on { message() }.doReturn(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE)
        }
        val graphResponse: GraphResponse<Storefront.QueryRoot> = mock()
        given(graphResponse.errors()).willReturn(listOf(error))
        return graphResponse
    }

    private fun stubAddress() =
        Address(address = "address", secondAddress = "secondAddress", city = "city",
            country = "country", state = "state", firstName = "firstName",
            lastName = "lastName", zip = "zip", phone = "phone")

    private fun stubCard() = Card("firstName", "lastName", "cardNumber",
        "expireMonth", "expireYear", "123")

    private fun stubCheckout() = Checkout("", "", false,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", stubAddress(), null)

}