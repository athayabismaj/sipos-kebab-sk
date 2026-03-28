package com.sipos.kebabsk.feature.menu.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MenuApiService {
    @GET("menus")
    suspend fun getMenus(
        @Header("Authorization") authorization: String,
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Long? = null
    ): Response<MenusResponse>
}
