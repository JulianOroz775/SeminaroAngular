package ar.edu.unicen.seminario.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double,
    val genres: List<Genre>,
    val homepage: String?
)

data class Genre(
    val id: Int,
    val name: String
)
