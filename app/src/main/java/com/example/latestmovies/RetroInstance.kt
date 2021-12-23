package com.example.latestmovies

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
class RetroInstance {

    //create retrofit instance - singleton
    companion object{
        var retrofit : Retrofit? = null
        val BaseURL= "https://api.themoviedb.org/3/movie/"
        fun getRetroInstance(): Retrofit? {
            if (retrofit == null){
                retrofit =   Retrofit.Builder()
                                    .baseUrl(BaseURL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
            }
            return retrofit
        }
    }
}