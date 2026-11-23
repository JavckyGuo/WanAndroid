package com.jacky.wanandroid.core

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
}

fun <T> Resource<T>.getOrNull(): T? = if (this is Resource.Success) data else null

fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Loading -> Resource.Loading
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> Resource.Error(message, cause)
}
