package com.shopapp.shopify.api.retrofit

import com.shopapp.shopify.api.entity.ApiCountryResponse
import retrofit2.Call
import retrofit2.http.GET

interface CountriesService {

    @GET("/admin/countries.json")
    fun recentDrives(): Call<ApiCountryResponse>

}