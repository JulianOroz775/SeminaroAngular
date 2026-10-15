package ar.edu.unicen.seminario.repository

import ar.edu.unicen.seminario.model.Movie
import ar.edu.unicen.seminario.network.MovieApi
import ar.edu.unicen.seminario.model.MovieDetail
import ar.edu.unicen.seminario.model.MoviePage

class MovieRepository(
    private val movieApi: MovieApi,
    private val token: String
) {

    suspend fun getPopularMovies(
        language: String = "es-AR",
        page: Int = 1
    ): Result<MoviePage> {
        return try {
            val response = movieApi.getPopularMovies(
                token = "Bearer $token",
                language = language,
                page = page
            )

            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(
                        Exception("La respuesta no contiene películas")
                    )

                Result.success(
                    MoviePage(
                        movies = body.results,
                        totalPages = body.total_pages
                    )
                )
            } else {
                Result.failure(
                    Exception("Error del servidor: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetails(
        movieId: Int,
        language: String = "es-AR"
    ): Result<MovieDetail> {
        return try {
            val response = movieApi.getMovieDetails(
                movieId = movieId,
                token = "Bearer $token",
                language = language
            )

            if (response.isSuccessful) {
                val detail = response.body()
                    ?: return Result.failure(
                        Exception("La respuesta no contiene el detalle")
                    )

                Result.success(detail)
            } else {
                Result.failure(
                    Exception("Error del servidor: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(
        query: String,
        language: String = "es-AR"
    ): Result<List<Movie>> {
        return try {
            val response = movieApi.searchMovies(
                query = query,
                token = "Bearer $token",
                language = language
            )

            if (response.isSuccessful) {
                val movies = response.body()?.results
                    ?: return Result.failure(
                        Exception("La respuesta no contiene películas")
                    )

                Result.success(movies)
            } else {
                Result.failure(
                    Exception("Error del servidor: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
