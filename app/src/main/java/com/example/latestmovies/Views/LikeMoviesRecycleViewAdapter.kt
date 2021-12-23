package com.example.latestmovies.Views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.latestmovies.R
import com.example.latestmovies.databinding.FragmentMovieItemListBinding
import com.example.latestmovies.models.MovieModel


class LikeMoviesRecycleViewAdapter :
    RecyclerView.Adapter<LikeMoviesRecycleViewAdapter.ViewHolder>() {
    private var likesMovieList: ArrayList<MovieModel> = ArrayList()
    private var imageMemoryListenerListener: MovieRecycleViewAdapter.imageFromCacheListener? = null

    fun setImageFromCacheListener(listener: MovieRecycleViewAdapter.imageFromCacheListener?) {
        imageMemoryListenerListener = listener
    }

    fun setUpdateData(items: ArrayList<MovieModel>) {
        this.likesMovieList = items;
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val viewBind: FragmentMovieItemListBinding) :
        RecyclerView.ViewHolder(viewBind.root) {

        fun bind(movie: MovieModel) {
            viewBind.apply {
                title.text = movie.title
                filter.text = movie.filter.name.replace("_", " ")
                image.setImageBitmap(null)
                if (movie.poster_path != null) {
                    val imageBitmap =
                        imageMemoryListenerListener?.getBitmapFromMemCache(movie.id.toString())
                    image.setImageBitmap(imageBitmap)
                } else {
                    image.setImageResource(R.drawable.baseline_hide_image_blue_grey_400_24dp)
                }
                like.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LikeMoviesRecycleViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentMovieItemListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikeMoviesRecycleViewAdapter.ViewHolder, position: Int) {
        holder.bind(likesMovieList[position])
    }

    override fun getItemCount(): Int {
        return likesMovieList.size
    }
}
