package com.example.latestmovies.Views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.latestmovies.MainActivityViewModel
import com.example.latestmovies.R
import com.example.latestmovies.databinding.FragmentMovieSummaryBinding
import com.example.latestmovies.models.MovieModel
import com.google.android.material.transition.platform.MaterialFadeThrough
import java.time.LocalDate


class MovieSummaryFragment : Fragment() {
    private lateinit var binding: FragmentMovieSummaryBinding
    private var memoryListener: MovieRecycleViewAdapter.imageFromCacheListener? = null
    private val viewModel: MainActivityViewModel by activityViewModels()

    companion object {
        const val TITLE = "title"
        const val DATE = "date"
        const val DESCRIPTION = "description"
        const val RATING = "rating"
        const val ID = "id"

        fun newInstance(movie: MovieModel): MovieSummaryFragment {
            val fragment = MovieSummaryFragment()
            val args = Bundle()
            args.putString(ID, movie.id.toString())
            args.putString(TITLE, movie.title)
            args.putString(DATE, movie.release_date)
            args.putString(DESCRIPTION, movie.overview)
            args.putString(RATING, movie.vote_average.toString())
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            binding.title.text = it.getString(TITLE)
            val id = it.getString(ID)
            setImage(id)
            binding.description.text = it.getString(DESCRIPTION)
            binding.rating.text = it.getString(RATING)
            it.getString(DATE)?.let { it1 -> setYear(it1) }
        }
    }

    private fun setImage( id: String?) {

        binding.image.setImageBitmap(null)

        if (id != null) {
            val imageBitmap = memoryListener?.getBitmapFromMemCache(id)
            binding.image.setImageBitmap(imageBitmap)
        } else {
            binding.image.setImageResource(R.drawable.baseline_hide_image_blue_grey_400_24dp)
        }
    }

    private fun setYear(date: String) {
        if (date.isNotEmpty()) {
            val ld: LocalDate? = LocalDate.parse(date)
            binding.year.text = ld?.year.toString()
        }
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