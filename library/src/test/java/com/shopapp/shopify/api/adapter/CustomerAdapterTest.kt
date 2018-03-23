package com.shopapp.shopify.api.adapter

import com.nhaarman.mockito_kotlin.given
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.constant.Constant.DEFAULT_STRING
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CustomerAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptCustomerStorefrontToCustomer() {
        val result = CustomerAdapter.adapt(StorefrontMockInstantiator.newCustomer())
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, result.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_FIRST_NAME, result.firstName)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LAST_NAME, result.lastName)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PHONE, result.phone)
        assertEquals(StorefrontMockInstantiator.DEFAULT_EMAIL, result.email)
        assertFalse(result.isAcceptsMarketing)
        assertNotNull(result.defaultAddress)
        assertNotNull(result.addressList.first())
    }

    @Test
    fun shouldDefaultEmptyStringWhenFieldsIsNull() {
        val customer = StorefrontMockInstantiator.newCustomer()
        given(customer.firstName).willReturn(null)
        given(customer.lastName).willReturn(null)
        given(customer.phone).willReturn(null)
        val result = CustomerAdapter.adapt(customer)

        assertEquals(DEFAULT_STRING, result.firstName)
        assertEquals(DEFAULT_STRING, result.lastName)
        assertEquals(DEFAULT_STRING, result.phone)
    }

    @Test
    fun shouldSetNullAddress() {
        val customer = StorefrontMockInstantiator.newCustomer()
        given(customer.defaultAddress).willReturn(null)
        val result = CustomerAdapter.adapt(customer)
        assertNull(result.defaultAddress)
    }
}