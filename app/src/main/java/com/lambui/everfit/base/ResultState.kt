package com.lambui.everfit.base

sealed interface ResultState<out T> {
    class Loading<T> : ResultState<T>
    class Initial<T> : ResultState<T>
    data class Result<T>(val result: kotlin.Result<T>) : ResultState<T>

    companion object {
        fun <T> ResultState<T>.getResult() = (this as? Result<T>)?.result
        fun <T> ResultState<T>.isLoading() = (this is Loading)
        fun <T> loading(): ResultState<T> = Loading<T>()
        fun <T> initial(): ResultState<T> = Initial<T>()
        fun <T> kotlin.Result<T>.state(): ResultState<T> = Result(this)
    }
}

