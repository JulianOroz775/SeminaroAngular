
package ar.edu.unicen.seminario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.seminario.model.Movie
import ar.edu.unicen.seminario.network.RetrofitInstance
import ar.edu.unicen.seminario.repository.MovieRepository
import ar.edu.unicen.seminario.ui.movies.MovieAdapter
import kotlinx.coroutines.launch

class WishlistActivity : AppCompatActivity() {

    private lateinit var wishlistManager: WishlistManager
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerWishlist: RecyclerView
    private lateinit var textWishlistEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        wishlistManager = WishlistManager(this)

        recyclerWishlist = findViewById(R.id.recyclerWishlist)
        textWishlistEmpty = findViewById(R.id.textWishlistEmpty)

        recyclerWishlist.layoutManager = LinearLayoutManager(this)

        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val intent = Intent(this, MovieDetailActivity::class.java)
                intent.putExtra("movie_id", movie.id)
                startActivity(intent)
            },
            showRemoveButton = true,
            onRemoveMovie = { movie ->
                wishlistManager.toggleFavorite(movie.id)
                movieAdapter.removeMovie(movie)

                if (wishlistManager.getWishlist().isEmpty()) {
                    recyclerWishlist.visibility = View.GONE
                    textWishlistEmpty.text = "Todavía no guardaste películas."
                    textWishlistEmpty.visibility = View.VISIBLE
                }
            }
        )

        recyclerWishlist.adapter = movieAdapter
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }

    private fun loadWishlist() {
        val movieIds = wishlistManager.getWishlist().toList()

        if (movieIds.isEmpty()) {
            recyclerWishlist.visibility = View.GONE
            textWishlistEmpty.visibility = View.VISIBLE
            return
        }

        recyclerWishlist.visibility = View.VISIBLE
        textWishlistEmpty.visibility = View.GONE

        lifecycleScope.launch {
            val repository = MovieRepository(
                RetrofitInstance.movieApi,
                BuildConfig.TMDB_READ_ACCESS_TOKEN
            )

            val movies = mutableListOf<Movie>()

            for (id in movieIds) {
                repository.getMovieDetails(id).onSuccess { detail ->
                    movies.add(
                        Movie(
                            id = detail.id,
                            title = detail.title,
                            overview = detail.overview,
                            poster_path = detail.poster_path,
                            vote_average = detail.vote_average,
                            genre_ids = detail.genres.map { it.id }
                        )
                    )
                }
            }

            // Actualizamos la lista con las películas encontradas.
            movieAdapter.updateMovies(movies)

            if (movies.isEmpty()) {
                recyclerWishlist.visibility = View.GONE
                textWishlistEmpty.text =
                    "No se pudieron cargar las películas guardadas."
                textWishlistEmpty.visibility = View.VISIBLE
            }
        }
    }
}
