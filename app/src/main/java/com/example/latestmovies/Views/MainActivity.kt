package com.example.latestmovies.Views

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.latestmovies.MainActivityViewModel
import com.example.latestmovies.R
import com.example.latestmovies.databinding.ActivityMainBinding
import com.example.latestmovies.models.MovieModel
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity(), MovieRecycleViewAdapter.onItemClickListener,
    MovieRecycleViewAdapter.imageFromCacheListener {
    private lateinit var binding: ActivityMainBinding
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    private var viewModel: MainActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawLayout()
        binding.tryAgain.setOnClickListener {
            drawLayout()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun drawLayout() {
        binding.noInternetLayout.visibility = View.GONE
        if (isNetworkAvailable()) {
            init()
        } else {
            binding.noInternetLayout.visibility = View.VISIBLE
        }
    }

    private fun init() {

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding.fragmentContainer.visibility = View.GONE

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

        val adapter = MyAdapter(this, supportFragmentManager, tabLayout!!.tabCount)
        viewPager!!.adapter = adapter

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout!!))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        viewModel?.getErrorLiveData()?.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    class MyAdapter(private val myContext: Context,fm: FragmentManager, private var totalTabs: Int
    ) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int {
            return totalTabs
        }

        // this is for fragment tabs
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    MovieListFragment()
                }
                1 -> {
                    LikesMoviesFragment()
                }
                else -> MovieListFragment()
            }
        }
    }

    override fun onMovieItemClick(movie: MovieModel) {
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.viewPager.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val fragment = MovieSummaryFragment.newInstance(
                movie
            )
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.viewPager.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.VISIBLE
    }

    override fun onLikeClick(movie: MovieModel, like: Boolean) {
        viewModel?.likedMovieClicked(movie, like)
    }

    override fun getBitmapFromMemCache(imageKey: String): Bitmap? {
        return viewModel?.getBitmapFromMemCache(imageKey)
    }

    override fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap?) {
        viewModel?.addBitmapToMemoryCache(key, bitmap)
    }

}