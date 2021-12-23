package com.example.latestmovies

import android.graphics.Bitmap
import android.util.LruCache
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.latestmovies.models.ListMovieModel
import com.example.latestmovies.models.MovieModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivityViewModel : ViewModel() {
    private var recycleViewLiveData: MutableLiveData<ListMovieModel> = MutableLiveData()
    private var errorLiveData = MutableLiveData<String?>()
    private var latestMoviesList: ListMovieModel = ListMovieModel(0, arrayListOf(), 0)
    private var upcomingMoviesList: ListMovieModel = ListMovieModel(0, arrayListOf(), 0)
    private var nowPlayingMoviesList: ListMovieModel = ListMovieModel(0, arrayListOf(), 0)
    private var topRatedMoviesList: ListMovieModel = ListMovieModel(0, arrayListOf(), 0)
    private var likedListMovieModel = ListMovieModel(0, arrayListOf(), 0)
    private var likedMoviesListLiveData: MutableLiveData<ListMovieModel> = MutableLiveData()
    private lateinit var movieFilter: MoviesEnum
    private var mMemoryCache: LruCache<String, Bitmap>? = null


    fun getRecyclerListObserver(
        input: MoviesEnum,
        callToApi: Boolean
    ): MutableLiveData<ListMovieModel> {
        if (callToApi) {
            apiCall(input, 1)
        }
        return recycleViewLiveData;
    }

    fun getErrorLiveData(): MutableLiveData<String?> {
        return errorLiveData
    }

    fun getLikedListObserver(): MutableLiveData<ListMovieModel> {
        return likedMoviesListLiveData;
    }

    fun setFilterType(filter: MoviesEnum) {
        this.movieFilter = filter
    }

    fun getFilterType(): MoviesEnum {
        return this.movieFilter
    }

    fun setMemoryCache(cache: LruCache<String, Bitmap>?) {
        this.mMemoryCache = cache
    }

    fun getMemoryCache(): LruCache<String, Bitmap>? {
        return mMemoryCache
    }

    //call to api by filter (input) and page number- until two pages for each filter
    private fun apiCall(input: MoviesEnum, pageNumber: Int) {
        setFilterType(input)
        viewModelScope.launch(Dispatchers.IO) {
            val retroInstance = RetroInstance.getRetroInstance()?.create(RetroService::class.java)
            when (input) {
                MoviesEnum.Latest -> {
                    retroInstance?.getLatestMoviesFromApi(pageNumber)?.enqueue(MovieCallback(input))
                }
                MoviesEnum.Top_Rated -> {
                    retroInstance?.getTopRatedMoviesFromApi(pageNumber)
                        ?.enqueue(MovieCallback(input))
                }
                MoviesEnum.Upcoming -> {
                    retroInstance?.getUpcomingMoviesFromApi(pageNumber)
                        ?.enqueue(MovieCallback(input))
                }
                MoviesEnum.Now_Playing -> {
                    retroInstance?.getNowPlayingMoviesFromApi(pageNumber)
                        ?.enqueue(MovieCallback(input))
                }
            }
        }
    }

    //if filter is clicked, if it is checked - add to movies list , else - remove the specific list from all movie list
    fun callToApiOrRemoveMovies(movieFilter: MoviesEnum, isChecked: Boolean) {
        if (isChecked) {
            apiCall(movieFilter, 1)
        } else {
            removeMoviesListFromAllMoviesList(movieFilter)
        }
    }


    private fun removeMoviesListFromAllMoviesList(movieFilter: MoviesEnum) {
        val oldList = recycleViewLiveData.value as ListMovieModel
        when (movieFilter) {
            MoviesEnum.Now_Playing -> {
                oldList.results.removeAll(nowPlayingMoviesList.results)
            }
            MoviesEnum.Top_Rated -> {
                oldList.results.removeAll(topRatedMoviesList.results)
            }
            MoviesEnum.Upcoming -> {
                oldList.results.removeAll(upcomingMoviesList.results)
            }
        }
        recycleViewLiveData.postValue(oldList)
    }


    inner class MovieCallback(input: MoviesEnum) : Callback<JsonObject> {
        private var inputType = input

        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
            if (response.isSuccessful) {
                response.body()?.let {
                    try {
                        val movieString = it.toString()
                        val gson = Gson()
                        val newMovieString = setMovieStringAsList(movieString)
                        val movieData: ListMovieModel =
                            gson.fromJson(newMovieString, ListMovieModel::class.java)
                        movieData.results.map { it.filter = inputType }

                        if (recycleViewLiveData.value != null) {
                            //add new list to all movies list
                            val oldList = recycleViewLiveData.value as ListMovieModel
                            oldList.results.addAll(movieData.results)
                            saveMovieList(inputType, movieData)
                            recycleViewLiveData.postValue(oldList)

                        } else { //initial screen
                            latestMoviesList = movieData
                            recycleViewLiveData.postValue(latestMoviesList)
                        }

                        //call to second page if exist
                        if (movieData.total_pages > 1 && movieData.page == 1) {
                            apiCall(inputType, 2)
                        }

                    } catch (e: JsonParseException) {
                        onFail(e.message)
                    }
                }
            } else {
                onFail(null)
            }
        }

        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            onFail(t.message)
        }
    }

    private fun onFail(message: String?) {
        errorLiveData.postValue(message)
    }

    private fun saveMovieList(inputType: MoviesEnum, movieData: ListMovieModel) {
        when (inputType) {
            MoviesEnum.Latest -> latestMoviesList.results.addAll(movieData.results)
            MoviesEnum.Upcoming -> upcomingMoviesList.results.addAll(movieData.results)
            MoviesEnum.Now_Playing -> nowPlayingMoviesList.results.addAll(movieData.results)
            MoviesEnum.Top_Rated -> topRatedMoviesList.results.addAll(movieData.results)
        }
    }

    //if api back one movie we need to wrap it in list
    private fun setMovieStringAsList(movieString: String): String {
        var newString = movieString
        if (!movieString.contains("results")) {
            newString = "{\"results\": [$movieString]}"
        }
        return newString
    }

    //update liked movies list - add or remove
    fun likedMovieClicked(movie: MovieModel, like: Boolean) {
        val exist = likedMoviesListLiveData.value?.results?.find { x -> x.id == movie.id }
        if (like) {
            if (exist == null) {
                likedListMovieModel.results.add(movie)
            }
        } else {
            likedListMovieModel.results.remove(movie)
        }
        likedMoviesListLiveData.postValue(likedListMovieModel)
    }


    fun getBitmapFromMemCache(key: String?): Bitmap? {
        return mMemoryCache?.get(key)
    }

    fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap?) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache?.put(key, bitmap)
        }
    }
}