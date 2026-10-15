package ar.edu.unicen.seminario

import android.content.Context

class WishlistManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "movie_wishlist",
        Context.MODE_PRIVATE
    )

    fun getWishlist(): Set<Int> {
        return preferences.getStringSet("movie_ids", emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }

    fun isFavorite(movieId: Int): Boolean {
        return getWishlist().contains(movieId)
    }

    fun toggleFavorite(movieId: Int): Boolean {
        val updated = getWishlist().toMutableSet()

        val isFavorite = if (updated.contains(movieId)) {
            updated.remove(movieId)
            false
        } else {
            updated.add(movieId)
            true
        }

        preferences.edit()
            .putStringSet(
                "movie_ids",
                updated.map { it.toString() }.toSet()
            )
            .apply()

        return isFavorite
    }
}