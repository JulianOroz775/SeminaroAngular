package ar.edu.unicen.seminario.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unicen.seminario.model.Movie
import ar.edu.unicen.seminario.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _movies = MutableLiveData<List<Movie>>(emptyList())
    val movies: LiveData<List<Movie>> = _movies

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private var currentPage = 0
    private var totalPages = Int.MAX_VALUE
    private var isLoadingMore = false
    private var isSearching = false

    fun loadPopularMovies() {
        isSearching = false
        currentPage = 0
        totalPages = Int.MAX_VALUE
        _movies.value = emptyList()
        loadPopularPage(1, append = false)
    }

    fun loadNextPopularPage() {
        if (isSearching || isLoadingMore) return
        if (currentPage >= totalPages) return

        loadPopularPage(currentPage + 1, append = true)
    }

    private fun loadPopularPage(page: Int, append: Boolean) {
        if (isLoadingMore) return

        isLoadingMore = true
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                repository.getPopularMovies(page = page)
                    .onSuccess { result ->
                        _movies.value = if (append) {
                            _movies.value.orEmpty() + result.movies
                        } else {
                            result.movies
                        }

                        currentPage = page
                        totalPages = result.totalPages
                    }
                    .onFailure { error ->
                        _errorMessage.value =
                            error.message
                                ?: "No se pudieron cargar las películas"
                    }
            } finally {
                isLoadingMore = false
                _isLoading.value = false
            }
        }
    }

    fun searchMovies(query: String) {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            clearSearch()
            return
        }

        isSearching = true
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                repository.searchMovies(cleanQuery)
                    .onSuccess { result ->
                        _movies.value = result

                        if (result.isEmpty()) {
                            _errorMessage.value =
                                "No hay resultados para la búsqueda."
                        }
                    }
                    .onFailure { error ->
                        _errorMessage.value =
                            error.message
                                ?: "No se pudo realizar la búsqueda."
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearSearch() {
        isSearching = false
        loadPopularMovies()
    }
}