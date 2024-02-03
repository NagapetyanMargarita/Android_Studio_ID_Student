package com.example.myapplication.api

import retrofit2.Call
import com.example.myapplication.data.Faculty
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ServerAPI {
    @GET(".")
    suspend fun getUniversity(): Response<UniversityNet>//ответ от сервера. если +, то объект,иначе ошибка

    @POST(".")
    //принимает объект UN, который отправляется на сервер в теле Post запроса
    suspend fun postUniversity(@Body university: UniversityNet): Response<UniversityNet>
}