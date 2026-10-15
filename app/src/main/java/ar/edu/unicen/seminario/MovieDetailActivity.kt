package ar.edu.unicen.seminario

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import ar.edu.unicen.seminario.network.RetrofitInstance
import ar.edu.unicen.seminario.repository.MovieRepository
import ar.edu.unicen.seminario.viewmodel.MovieDetailViewModel
import ar.edu.unicen.seminario.viewmodel.MovieDetailViewModelFactory

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: MovieDetailViewModel
    private var movieId: Int = -1

    private lateinit var wishlistManager: WishlistManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)
        wishlistManager = WishlistManager(this)
        val buttonWishlist = findViewById<Button>(R.id.buttonWishlist)

        movieId = intent.getIntExtra("movie_id", -1)

        if (movieId == -1) {
            finish()
            return
        }
        fun updateWishlistButton() {
            buttonWishlist.text = if (wishlistManager.isFavorite(movieId)) {
                "♥ Quitar de deseados"
            } else {
                "♡ Agregar a deseados"
            }
        }

        updateWishlistButton()

        buttonWishlist.setOnClickListener {
            wishlistManager.toggleFavorite(movieId)
            updateWishlistButton()
        }

        val imagePoster = findViewById<ImageView>(R.id.imageDetailPoster)
        val textTitle = findViewById<TextView>(R.id.textDetailTitle)
        val textRating = findViewById<TextView>(R.id.textDetailRating)
        val textGenres = findViewById<TextView>(R.id.textDetailGenres)
        val textOverview = findViewById<TextView>(R.id.textDetailOverview)
        val progress = findViewById<ProgressBar>(R.id.progressDetailLoading)
        val layoutError = findViewById<View>(R.id.layoutDetailError)
        val textError = findViewById<TextView>(R.id.textDetailError)
        val buttonRetry = findViewById<Button>(R.id.buttonDetailRetry)

        val token = BuildConfig.TMDB_READ_ACCESS_TOKEN
        val repository = MovieRepository(RetrofitInstance.movieApi, token)
        val factory = MovieDetailViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)
            .get(MovieDetailViewModel::class.java)

        viewModel.movie.observe(this) { movie ->
            if (movie != null) {
                textTitle.text = movie.title
                textRating.text = "Calificación: %.1f/10".format(movie.vote_average)
                textGenres.text = "Géneros: " +
                        movie.genres.joinToString(", ") { it.name }
                textOverview.text = movie.overview.ifBlank {
                    "No hay sinopsis disponible."
                }

                val posterUrl = movie.poster_path?.let {
                    "https://image.tmdb.org/t/p/w500$it"
                }

                Glide.with(this)
                    .load(posterUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imagePoster)

                layoutError.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                textError.text = message
                layoutError.visibility = View.VISIBLE
            } else {
                layoutError.visibility = View.GONE
            }
        }

        buttonRetry.setOnClickListener {
            viewModel.loadMovieDetails(movieId)
        }

        viewModel.loadMovieDetails(movieId)
    }
}
