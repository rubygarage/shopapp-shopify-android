package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Address
import com.shopapp.gateway.entity.Country
import com.shopapp.gateway.entity.Customer
import com.shopapp.gateway.entity.Error
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.api.entity.ApiCountry
import com.shopapp.shopify.api.entity.ApiCountryResponse
import com.shopapp.shopify.api.retrofit.CountriesService
import com.shopapp.shopify.constant.Constant
import com.shopify.buy3.Storefront
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test
import org.mockito.BDDMockito
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShopifyApiAuthTest : BaseShopifyApiTest() {

    @Test
    fun signUpShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val customerCreatePayload: Storefront.CustomerCreatePayload = mock()
        BDDMockito.given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        BDDMockito.given(customerCreatePayload.customer).willReturn(customer)

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
        BDDMockito.given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        BDDMockito.given(customerCreatePayload.customer).willReturn(customer)

        mockFailureRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.signUp("", "", "", "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun signUpShouldReturnNonCriticalErrorWhenSignUpFailed() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val customerCreatePayload: Storefront.CustomerCreatePayload = mock()
        BDDMockito.given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        BDDMockito.given(customerCreatePayload.customer).willReturn(customer)

        val userError: com.shopify.graphql.support.Error = mock()
        BDDMockito.given(userError.message()).willReturn(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE)
        BDDMockito.given(graphResponse.errors()).willReturn(listOf(userError))

        val callback: ApiCallback<Unit> = mock()
        api.signUp("", "", "", "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun signUpShouldReturnContentErrorWhenSignUpFailed() {
        val (_, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnFailure()

        val customerCreatePayload: Storefront.CustomerCreatePayload = mock()
        BDDMockito.given(storefrontMutation.customerCreate).willReturn(customerCreatePayload)

        val customer = StorefrontMockInstantiator.newCustomer()
        BDDMockito.given(customerCreatePayload.customer).willReturn(customer)

        val callback: ApiCallback<Unit> = mock()
        api.signUp("", "", "", "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun signInShouldReturnContentErrorWhenTokenRequestFailed() {
        val (graphResponse, _) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        BDDMockito.given(mutationGraphCall.execute()).willReturn(graphResponse)
        BDDMockito.given(graphResponse.data()).willReturn(null)
        val callback: ApiCallback<Unit> = mock()
        api.signIn("", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
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
        BDDMockito.given(userError.message()).willReturn(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE)
        BDDMockito.given(graphResponse.errors()).willReturn(listOf(userError))

        val callback: ApiCallback<Unit> = mock()
        api.forgotPassword("", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun forgotPasswordShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, _) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()

        val customerRecoverPayload: Storefront.CustomerRecoverPayload = mock()
        BDDMockito.given(customerRecoverPayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(graphResponse.data()?.customerRecover).willReturn(customerRecoverPayload)

        val callback: ApiCallback<Unit> = mock()
        api.forgotPassword("", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
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

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCustomerShouldReturnCustomer() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val customer = StorefrontMockInstantiator.newCustomer()
        BDDMockito.given(storefrontQueryRoot.customer).willReturn(customer)

        mockSession(true)
        val callback: ApiCallback<Customer?> = mock()
        api.getCustomer(callback)

        argumentCaptor<Customer>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertNotNull(firstValue)
            Assert.assertEquals(customer.id.toString(), firstValue.id)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
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

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun createCustomerAddressShouldReturnCustomerId() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontAddress = StorefrontMockInstantiator.newAddress()
        val customerAddressCreatePayload: Storefront.CustomerAddressCreatePayload = mock()
        BDDMockito.given(customerAddressCreatePayload.customerAddress).willReturn(storefrontAddress)
        BDDMockito.given(storefrontMutation.customerAddressCreate).willReturn(customerAddressCreatePayload)

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<String> = mock()
        api.createCustomerAddress(address, callback)

        argumentCaptor<String>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertEquals(storefrontAddress.id.toString(), firstValue)
        }
    }

    @Test
    fun createCustomerAddressShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerAddressCreatePayload: Storefront.CustomerAddressCreatePayload = mock()
        BDDMockito.given(customerAddressCreatePayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(storefrontMutation.customerAddressCreate).willReturn(customerAddressCreatePayload)

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<String> = mock()
        api.createCustomerAddress(address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertTrue(firstValue is Error.NonCritical)
            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun editCustomerAddressShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontAddress = StorefrontMockInstantiator.newAddress()
        val customerAddressUpdatePayload: Storefront.CustomerAddressUpdatePayload = mock()
        BDDMockito.given(customerAddressUpdatePayload.customerAddress).willReturn(storefrontAddress)
        BDDMockito.given(storefrontMutation.customerAddressUpdate).willReturn(customerAddressUpdatePayload)

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
        BDDMockito.given(customerAddressUpdatePayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(storefrontMutation.customerAddressUpdate).willReturn(customerAddressUpdatePayload)

        mockSession(true)
        val address = stubAddress()
        val callback: ApiCallback<Unit> = mock()
        api.editCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, address, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertTrue(firstValue is Error.NonCritical)
            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun deleteCustomerAddressShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val customerAddressDeletePayload: Storefront.CustomerAddressDeletePayload = mock()
        BDDMockito.given(customerAddressDeletePayload.deletedCustomerAddressId).willReturn(StorefrontMockInstantiator.DEFAULT_ID)
        BDDMockito.given(storefrontMutation.customerAddressDelete).willReturn(customerAddressDeletePayload)

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
        BDDMockito.given(customerAddressDeletePayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(storefrontMutation.customerAddressDelete).willReturn(customerAddressDeletePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.deleteCustomerAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertTrue(firstValue is Error.NonCritical)
            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun setDefaultShippingAddressShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val customerDefaultAddressUpdatePayload: Storefront.CustomerDefaultAddressUpdatePayload = mock()
        BDDMockito.given(storefrontMutation.customerDefaultAddressUpdate).willReturn(customerDefaultAddressUpdatePayload)

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
        BDDMockito.given(customerDefaultAddressUpdatePayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(storefrontMutation.customerDefaultAddressUpdate).willReturn(customerDefaultAddressUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.setDefaultShippingAddress(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertTrue(firstValue is Error.NonCritical)
            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun editCustomerInfoShouldReturnCustomer() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCustomer = StorefrontMockInstantiator.newCustomer()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        BDDMockito.given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        BDDMockito.given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Customer> = mock()
        api.editCustomerInfo("firstName", "lastName", "phone", callback)

        argumentCaptor<Customer>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertEquals(storefrontCustomer.id.toString(), firstValue.id)
        }
    }

    @Test
    fun editCustomerInfoShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        BDDMockito.given(customerUpdatePayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Customer> = mock()
        api.editCustomerInfo("firstName", "lastName", "phone", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertTrue(firstValue is Error.NonCritical)
            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun changePasswordShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCustomer = StorefrontMockInstantiator.newCustomer()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        BDDMockito.given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        BDDMockito.given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

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
        BDDMockito.given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        BDDMockito.given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        mockFailureRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.changePassword("password", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
        }
    }

    @Test
    fun changePasswordShouldReturnNonCriticalErrorWithUserErrorsResponse() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        val mutationGraphCall = mockMutationGraphCallWithOnResponse(graphResponse)

        val userError = StorefrontMockInstantiator.newUserError()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        BDDMockito.given(customerUpdatePayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        mockSuccessRequestToken(graphResponse, mutationGraphCall, storefrontMutation)
        val callback: ApiCallback<Unit> = mock()
        api.changePassword("password", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertTrue(firstValue is Error.NonCritical)
            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
        }
    }

    @Test
    fun getCountriesShouldReturnCountriesListFromApi() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        BDDMockito.given(countriesService.getCountries()).willReturn(call)
        BDDMockito.given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        val response: Response<ApiCountryResponse> = mock()
        BDDMockito.given(response.isSuccessful).willReturn(true)
        val apiCountry = ApiCountry(123, "code", "name", listOf())
        val apiResponse = ApiCountryResponse(listOf(apiCountry))
        BDDMockito.given(response.body()).willReturn(apiResponse)

        BDDMockito.given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onResponse(call, response)
        })

        val callback: ApiCallback<List<Country>> = mock()
        api.getCountries(callback)

        argumentCaptor<List<Country>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertEquals(apiResponse.countries.size, firstValue.size)
        }
    }

    @Test
    fun getCountriesShouldReturnCountriesListFromJson() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        BDDMockito.given(countriesService.getCountries()).willReturn(call)
        BDDMockito.given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        val response: Response<ApiCountryResponse> = mock()
        BDDMockito.given(response.isSuccessful).willReturn(true)
        val apiCountry = ApiCountry(123, "code", Constant.REST_OF_WORLD, listOf())
        val apiResponse = ApiCountryResponse(listOf(apiCountry))
        BDDMockito.given(response.body()).willReturn(apiResponse)

        BDDMockito.given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onResponse(call, response)
        })

        val countryId = 38097911857L
        val countryName = "Afghanistan"
        BDDMockito.given(assetsReader.read(any(), any())).willReturn("[\n" +
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

            Assert.assertTrue(firstValue.isNotEmpty())
            Assert.assertEquals(countryId, firstValue.first().id)
            Assert.assertEquals(countryName, firstValue.first().name)
        }
    }

    @Test
    fun getCountriesShouldReturnContentErrorWhenResponseIsNotSuccessful() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        BDDMockito.given(countriesService.getCountries()).willReturn(call)
        BDDMockito.given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        val response: Response<ApiCountryResponse> = mock()
        BDDMockito.given(response.isSuccessful).willReturn(false)
        val responseBody: ResponseBody = mock()
        BDDMockito.given(response.errorBody()).willReturn(responseBody)

        BDDMockito.given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onResponse(call, response)
        })

        val callback: ApiCallback<List<Country>> = mock()
        api.getCountries(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCountriesShouldReturnContentError() {
        val countriesService: CountriesService = mock()
        val call: Call<ApiCountryResponse> = mock()
        BDDMockito.given(countriesService.getCountries()).willReturn(call)
        BDDMockito.given(retrofit.create(CountriesService::class.java)).willReturn(countriesService)

        BDDMockito.given(call.enqueue(any())).willAnswer({
            val callback = it.getArgument<Callback<ApiCountryResponse>>(0)
            callback.onFailure(call, Throwable())
        })

        val callback: ApiCallback<List<Country>> = mock()
        api.getCountries(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun updateCustomerSettingsShouldReturnUnit() {
        val (graphResponse, storefrontMutation) = mockMutationDataResponse()
        mockMutationGraphCallWithOnResponse(graphResponse)

        val storefrontCustomer = StorefrontMockInstantiator.newCustomer()
        val customerUpdatePayload: Storefront.CustomerUpdatePayload = mock()
        BDDMockito.given(customerUpdatePayload.customer).willReturn(storefrontCustomer)
        BDDMockito.given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

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
        BDDMockito.given(customerUpdatePayload.userErrors).willReturn(listOf(userError))
        BDDMockito.given(storefrontMutation.customerUpdate).willReturn(customerUpdatePayload)

        mockSession(true)
        val callback: ApiCallback<Unit> = mock()
        api.updateCustomerSettings(true, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
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

            Assert.assertTrue(firstValue is Error.Content)
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

            Assert.assertTrue(firstValue is Error.NonCritical)
            Assert.assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
        }
    }
}