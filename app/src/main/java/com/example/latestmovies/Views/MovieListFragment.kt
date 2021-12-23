package com.example.latestmovies.Views

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.latestmovies.MainActivityViewModel
import com.example.latestmovies.MoviesEnum
import com.example.latestmovies.databinding.FragmentMovieListBinding
import com.example.latestmovies.models.ListMovieModel
import com.google.android.material.transition.MaterialFadeThrough


class MovieListFragment : Fragment() {
    private lateinit var binding: FragmentMovieListBinding
    private lateinit var recycleAdapter: MovieRecycleViewAdapter
    private var listener: MovieRecycleViewAdapter.onItemClickListener? = null
    private var memoryListener: MovieRecycleViewAdapter.imageFromCacheListener? = null
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var memoryCache: LruCache<String, Bitmap>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialFadeThrough() //for animation

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieListBinding.inflate(inflater, container, false)
        startLoader()
        initRecyclerView()
        initViewModel(savedInstanceState == null) // if configuration change and we have movies list stored
        initFilterButtons()
        return binding.root
    }

    private fun initRecyclerView() {
        val recyclerView = binding.recycler
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(decoration)
        recycleAdapter = MovieRecycleViewAdapter()
        recycleAdapter.setOnItemClickListener(listener)
        recycleAdapter.setImageFromCacheListener(memoryListener)
        recyclerView.adapter = recycleAdapter
    }

    private fun initViewModel(callToApi: Boolean) {
        viewModel.setMemoryCache(memoryCache)
        viewModel.setFilterType(MoviesEnum.Latest)
        viewModel.getRecyclerListObserver(MoviesEnum.Latest, callToApi)
            .observe(viewLifecycleOwner, Observer<ListMovieModel> {
                if (it != null) {
                    recycleAdapter.setUpdateData(it.results)

                } else {
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                }
                stopLoader()
            })
    }

    private fun initFilterButtons() {
        binding.TopRated.setOnCheckedChangeListener { chip, isChecked ->
            startLoader()
            viewModel.callToApiOrRemoveMovies(MoviesEnum.Top_Rated, isChecked)
        }

        binding.Upcoming.setOnCheckedChangeListener { chip, isChecked ->
            startLoader()
            viewModel.callToApiOrRemoveMovies(MoviesEnum.Upcoming, isChecked)
        }

        binding.NowPlaying.setOnCheckedChangeListener { chip, isChecked ->
            startLoader()
            viewModel.callToApiOrRemoveMovies(MoviesEnum.Now_Playing, isChecked)
        }
    }

    private fun startLoader() {
        binding.loader.visibility = View.VISIBLE
    }

    private fun stopLoader() {
        binding.loader.visibility = View.GONE
    }

    companion object {

        @JvmStatic
        fun newInstance() = MovieListFragment()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as MainActivity
        memoryListener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        memoryListener = null
    }
}