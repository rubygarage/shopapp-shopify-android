package com.shopapp.shopify.api.call

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Error
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MutationCallWrapperTest {

    private lateinit var callback: ApiCallback<String>
    private lateinit var adapter: TestAdapter<String>
    private lateinit var mutationCallWrapper: TestMutationCallWrapper

    @Before
    fun setUp() {
        callback = mock()
        adapter = mock()
        mutationCallWrapper = TestMutationCallWrapper(callback, adapter)
    }

    @Test
    fun shouldReturnData() {
        val data: Storefront.Mutation = mock()
        val response: GraphResponse<Storefront.Mutation> = mock {
            on { data() } doReturn data
        }
        val result = "result"
        given(adapter.adapt(any())).willReturn(AdapterResult.DataResult(result))
        mutationCallWrapper.onResponse(response)

        verify(adapter).adapt(data)
        verify(callback).onResult(result)
        verify(callback, never()).onFailure(any())
    }

    @Test
    fun shouldReturnNonCriticalErrorWhenResponseContainsError() {
        val errorMessage = "error message"
        val response: GraphResponse<Storefront.Mutation> = mock {
            on { errors() } doReturn listOf(com.shopify.graphql.support.Error(errorMessage))
        }
        given(adapter.adapt(any())).willReturn(AdapterResult.ErrorResult(Error.Critical()))
        mutationCallWrapper.onResponse(response)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.NonCritical)
            assertEquals(errorMessage, firstValue.message)
        }
    }

    @Test
    fun shouldReturnCriticalErrorWhenAdapterReturnsError() {
        val data: Storefront.Mutation = mock()
        val response: GraphResponse<Storefront.Mutation> = mock {
            on { data() } doReturn data
        }
        given(adapter.adapt(any())).willReturn(AdapterResult.ErrorResult(Error.Critical()))
        mutationCallWrapper.onResponse(response)

        argumentCaptor<Error>().apply {
            verify(adapter).adapt(data)
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Critical)
        }
    }

    @Test
    fun shouldReturnContentErrorWhenReceivedDataIsNull() {
        val response: GraphResponse<Storefront.Mutation> = mock()
        given(adapter.adapt(any())).willReturn(AdapterResult.ErrorResult(Error.Critical()))
        mutationCallWrapper.onResponse(response)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    private class TestMutationCallWrapper(
        callback: ApiCallback<String>,
        val adapter: TestAdapter<String>
    ) : MutationCallWrapper<String>(callback) {

        override fun adapt(data: Storefront.Mutation?): AdapterResult<String>? = adapter.adapt(data)
    }

    interface TestAdapter<out T> {

        fun adapt(data: Storefront.Mutation?): AdapterResult<T>
    }
}