package ar.edu.unicen.seminario.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unicen.seminario.model.MovieDetail
import ar.edu.unicen.seminario.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _movie = MutableLiveData<MovieDetail?>()
    val movie: LiveData<MovieDetail?> = _movie

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _movie.value = null

            try {
                repository.getMovieDetails(movieId)
                    .onSuccess { detail ->
                        _movie.value = detail
                    }
                    .onFailure { error ->
                        _errorMessage.value = when {
                            error is java.net.UnknownHostException ->
                                "No hay conexión a internet. Verificá tu conexión y reintentá."

                            error is java.net.SocketTimeoutException ->
                                "La solicitud tardó demasiado. Intentá nuevamente."

                            else ->
                                error.message ?: "No se pudo cargar el detalle."
                        }
                    }
            } catch (error: Exception) {
                _errorMessage.value =
                    error.message ?: "Ocurrió un error al cargar la película."
            } finally {
                _isLoading.value = false
            }
        }
    }
}