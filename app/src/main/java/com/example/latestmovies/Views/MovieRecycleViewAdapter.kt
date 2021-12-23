package com.example.latestmovies.Views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.latestmovies.R.drawable
import com.example.latestmovies.databinding.FragmentMovieItemListBinding
import com.example.latestmovies.models.MovieModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL


class MovieRecycleViewAdapter : RecyclerView.Adapter<MovieRecycleViewAdapter.MyViewHolder>() {
    private var movieList: ArrayList<MovieModel> = ArrayList()
    private var mListener: onItemClickListener? = null
    private var imageMemoryListenerListener: imageFromCacheListener? = null

    interface onItemClickListener {
        fun onMovieItemClick(movie: MovieModel)
        fun onLikeClick(movie: MovieModel, like: Boolean)
    }

    interface imageFromCacheListener {
        fun getBitmapFromMemCache(imageKey: String): Bitmap?
        fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap?)
    }

    fun setUpdateData(items: ArrayList<MovieModel>) {
        this.movieList = items
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: onItemClickListener?) {
        mListener = listener
    }

    fun setImageFromCacheListener(listener: imageFromCacheListener?) {
        imageMemoryListenerListener = listener
    }

    inner class MyViewHolder(private val viewBind: FragmentMovieItemListBinding, context: Context) :
        RecyclerView.ViewHolder(viewBind.root), View.OnClickListener {

        init {
            viewBind.like.setOnClickListener(this)
        }

        fun bind(movie: MovieModel, listener: onItemClickListener?) {
            viewBind.apply {
                title.text = movie.title
                filter.text = movie.filter.name.replace("_", " ")
                image.setImageBitmap(null)
                setMovieImage(movie)

                movieItem.setOnClickListener {
                    listener?.onMovieItemClick(movie)
                }

                setLikeIcon(movie.isLiked)
            }
        }

        //get image from cache if exist,
        //                      else, set image url as bitmap by httpConnection
        //save image in cache
        private fun FragmentMovieItemListBinding.setMovieImage(movie: MovieModel) {
            if (movie.poster_path != null) {

                val imageFromCache =
                    imageMemoryListenerListener?.getBitmapFromMemCache(movie.id.toString())
                if (imageFromCache == null) {
                    GlobalScope.launch(Dispatchers.IO) {

                        val imageUrl = URL(movie.getPosterPath())

                        val httpConnection = imageUrl.openConnection() as HttpURLConnection
                        httpConnection.doInput = true
                        httpConnection.connect()

                        val inputStream = httpConnection.inputStream
                        val bitmapImage = BitmapFactory.decodeStream(inputStream)
                        imageMemoryListenerListener?.addBitmapToMemoryCache(
                            movie.id.toString(),
                            bitmapImage
                        )
                        launch(Dispatchers.Main) {
                            image.setImageBitmap(bitmapImage)
                        }
                    }
                } else {
                    image.setImageBitmap(imageFromCache)
                }

            } else {
                image.setImageResource(drawable.baseline_hide_image_blue_grey_400_24dp)
            }
        }

        override fun onClick(v: View?) {
            val movieLiked = !movieList[adapterPosition].isLiked
            movieList[adapterPosition].isLiked = movieLiked
            setLikeIcon(movieLiked)
            mListener?.onLikeClick(movieList[adapterPosition], movieLiked)

        }

        private fun setLikeIcon(liked: Boolean) {
            if (liked) {
                viewBind.like.setImageResource(drawable.baseline_favorite_red_a700_18dp)
            } else {
                viewBind.like.setImageResource(drawable.baseline_favorite_border_red_a700_18dp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentMovieItemListBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(movieList[position], mListener)
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return movieList.size
    }


}