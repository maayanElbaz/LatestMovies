package com.example.latestmovies

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroService {

    @GET("latest?api_key=5c2e383ba8e359f9a53b12721e7ee200&page=1")
    fun getLatestMoviesFromApi(@Query("page") page: Int) : Call<JsonObject>

    @GET("top_rated?api_key=5c2e383ba8e359f9a53b12721e7ee200&page=1")
    fun getTopRatedMoviesFromApi(@Query("page") page: Int) : Call<JsonObject>

    @GET("upcoming?api_key=5c2e383ba8e359f9a53b12721e7ee200&page=1")
    fun getUpcomingMoviesFromApi(@Query("page") page: Int) : Call<JsonObject>

    @GET("now_playing?api_key=5c2e383ba8e359f9a53b12721e7ee200")
    fun getNowPlayingMoviesFromApi(@Query("page") page: Int) : Call<JsonObject>
}