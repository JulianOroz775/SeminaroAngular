package ar.edu.unicen.seminario.network

import ar.edu.unicen.seminario.model.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import ar.edu.unicen.seminario.model.MovieDetail
import retrofit2.http.Path

interface MovieApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Header("Authorization") token: String,
        @Query("language") language: String = "es-AR",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Header("Authorization") token: String,
        @Query("language") language: String = "es-AR"
    ): Response<MovieDetail>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Header("Authorization") token: String,
        @Query("language") language: String = "es-AR",
        @Query("page") page: Int = 1
    ): Response<MovieResponse>
}
