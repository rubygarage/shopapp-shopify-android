package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.api.entity.ApiState
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VariantOptionListAdapterTest {

    @Test
    fun shouldAdaptFromOptionListStorefrontToVariantOptionList() {
        val resultList = VariantOptionListAdapter.adapt(listOf(StorefrontMockInstantiator.newSelectedOption()))
        val result = resultList.first()
        assertEquals(StorefrontMockInstantiator.DEFAULT_NAME, result.name)
        assertEquals(StorefrontMockInstantiator.DEFAULT_VALUE, result.value)
    }

    @Test
    fun shouldReturnEmptyList() {
        val resultList = VariantOptionListAdapter.adapt(null)
        assertNotNull(resultList)
        assertTrue(resultList.isEmpty())
    }
}