package com.example.latestmovies.Views

import android.content.Context
import android.os.Bundle
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
import com.example.latestmovies.databinding.FragmentLikesMoviesBinding
import com.example.latestmovies.models.ListMovieModel


class LikesMoviesFragment : Fragment() {
    private lateinit var binding: FragmentLikesMoviesBinding
    private lateinit var recycleAdapter: LikeMoviesRecycleViewAdapter
    private val viewModel: MainActivityViewModel by activityViewModels()
    private var memoryListener: MovieRecycleViewAdapter.imageFromCacheListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        binding = FragmentLikesMoviesBinding.inflate(inflater, container, false)
        initRecyclerView()
        initViewModel()

        return binding.root
    }


    private fun initRecyclerView() {
        val recyclerView = binding.likeMoviesRecycler
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(decoration)
        recycleAdapter = LikeMoviesRecycleViewAdapter()
        recycleAdapter.setImageFromCacheListener(memoryListener)
        recyclerView.adapter = recycleAdapter
    }

    private fun initViewModel() {
        viewModel.getLikedListObserver().observe(viewLifecycleOwner, Observer<ListMovieModel> {
            if (it != null) {
                recycleAdapter.setUpdateData(it.results)
            } else {
                Toast.makeText(activity, "Error In Like Movies Page", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = LikesMoviesFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        memoryListener = context as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        memoryListener = null
    }
}