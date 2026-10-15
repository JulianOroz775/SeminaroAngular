package ar.edu.unicen.seminario.ui.movies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ar.edu.unicen.seminario.R
import ar.edu.unicen.seminario.model.Movie
import com.bumptech.glide.Glide

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val showRemoveButton: Boolean = false,
    private val onRemoveMovie: ((Movie) -> Unit)? = null
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val movies = mutableListOf<Movie>()

    fun updateMovies(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    fun removeMovie(movie: Movie) {
        val position = movies.indexOfFirst { it.id == movie.id }
        if (position != -1) {
            movies.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)

        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MovieViewHolder,
        position: Int
    ) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    inner class MovieViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val poster: ImageView =
            itemView.findViewById(R.id.imageMoviePoster)

        private val title: TextView =
            itemView.findViewById(R.id.textMovieTitle)

        private val rating: TextView =
            itemView.findViewById(R.id.textMovieRating)

        private val buttonRemove: Button =
            itemView.findViewById(R.id.buttonRemoveWishlist)

        fun bind(movie: Movie) {
            title.text = movie.title
            rating.text = "⭐ %.1f".format(movie.vote_average)

            val posterUrl = movie.poster_path?.let {
                "https://image.tmdb.org/t/p/w500$it"
            }

            Glide.with(itemView)
                .load(posterUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(poster)

            itemView.setOnClickListener {
                onMovieClick(movie)
            }

            buttonRemove.visibility =
                if (showRemoveButton) View.VISIBLE else View.GONE

            buttonRemove.setOnClickListener {
                onRemoveMovie?.invoke(movie)
            }
        }
    }
}
