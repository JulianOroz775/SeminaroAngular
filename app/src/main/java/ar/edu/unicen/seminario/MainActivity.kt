package ar.edu.unicen.seminario

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.seminario.network.RetrofitInstance
import ar.edu.unicen.seminario.repository.MovieRepository
import ar.edu.unicen.seminario.ui.movies.MovieAdapter
import ar.edu.unicen.seminario.viewmodel.MovieViewModel
import ar.edu.unicen.seminario.viewmodel.MovieViewModelFactory


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MovieViewModel
    private lateinit var movieAdapter: MovieAdapter

    private lateinit var recyclerMovies: RecyclerView
    private lateinit var progressLoading: ProgressBar
    private lateinit var layoutError: View
    private lateinit var textError: TextView
    private lateinit var buttonRetry: Button
    private lateinit var editSearchMovie: EditText

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonOpenWishlist = findViewById<Button>(R.id.buttonOpenWishlist)

        buttonOpenWishlist.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        recyclerMovies = findViewById(R.id.recyclerMovies)
        editSearchMovie = findViewById(R.id.editSearchMovie)
        progressLoading = findViewById(R.id.progressLoading)
        layoutError = findViewById(R.id.layoutError)
        textError = findViewById(R.id.textError)
        buttonRetry = findViewById(R.id.buttonRetry)

        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val intent = Intent(this, MovieDetailActivity::class.java)
                intent.putExtra("movie_id", movie.id)
                startActivity(intent)
            }
        )

        val layoutManager = LinearLayoutManager(this)

        recyclerMovies.layoutManager = layoutManager
        recyclerMovies.adapter = movieAdapter

        recyclerMovies.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0) {
                        val totalItems = layoutManager.itemCount
                        val lastVisibleItem =
                            layoutManager.findLastVisibleItemPosition()

                        if (lastVisibleItem >= totalItems - 3) {
                            viewModel.loadNextPopularPage()
                        }
                    }
                }
            }
        )

        val token = BuildConfig.TMDB_READ_ACCESS_TOKEN

        if (token.isBlank()) {
            showError("Falta configurar el token de TMDB en local.properties.")
            return
        }

        val repository = MovieRepository(
            RetrofitInstance.movieApi,
            token
        )

        val factory = MovieViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)
            .get(MovieViewModel::class.java)

        observeViewModel()
        setupSearch()

        buttonRetry.setOnClickListener {
            val query = editSearchMovie.text.toString().trim()

            if (query.isBlank()) {
                viewModel.loadPopularMovies()
            } else {
                viewModel.searchMovies(query)
            }
        }

        viewModel.loadPopularMovies()
    }

    private fun setupSearch() {
        editSearchMovie.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchRunnable?.let {
                    searchHandler.removeCallbacks(it)
                }

                val query = s?.toString().orEmpty().trim()

                searchRunnable = Runnable {
                    if (query.isBlank()) {
                        viewModel.clearSearch()
                    } else {
                        viewModel.searchMovies(query)
                    }
                }

                searchRunnable?.let {
                    searchHandler.postDelayed(it, 500)
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun observeViewModel() {
        viewModel.movies.observe(this) { movies ->
            movieAdapter.updateMovies(movies)

            recyclerMovies.visibility =
                if (movies.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressLoading.visibility =
                if (isLoading) View.VISIBLE else View.GONE

            if (isLoading) {
                layoutError.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                showError(message)
            } else {
                layoutError.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        textError.text = message
        layoutError.visibility = View.VISIBLE
        recyclerMovies.visibility = View.GONE
        progressLoading.visibility = View.GONE
    }

    override fun onDestroy() {
        searchRunnable?.let {
            searchHandler.removeCallbacks(it)
        }
        super.onDestroy()
    }
}