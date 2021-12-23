package com.example.latestmovies.models;

import com.example.latestmovies.MoviesEnum
import java.util.*
import kotlin.collections.ArrayList


data class ListMovieModel (
    var page: Int,
    var results: ArrayList<MovieModel>,
    var total_pages: Int
)

data class MovieModel (

    val title: String?,
    val poster_path: String?,
    val release_date: String?,
    val overview: String,
    val id: Int,
    val vote_average: Number,
    var filter: MoviesEnum,
    var isLiked: Boolean

){
    fun getPosterPath(): String {
        var path = ""
        if (poster_path != null && poster_path.isNotEmpty()){
            path = "https://image.tmdb.org/t/p/w154$poster_path"
        }
        return path;
    }
}
