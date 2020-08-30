package com.example.devlife

import com.example.devlife.model.ResponsePost
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface JsonPlaceholderApi {
    @GET("{section}/{page_number}?json=true")
    fun get_gifPost(@Path("section") section: String,
                    @Path("page_number") page_number: Int): Call<ResponsePost>
}