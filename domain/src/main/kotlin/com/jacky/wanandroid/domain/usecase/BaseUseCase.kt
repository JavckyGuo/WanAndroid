package com.jacky.wanandroid.domain.usecase

abstract class BaseUseCase<in P, out R> {
    suspend operator fun invoke(params: P): Result<R> = runCatching { execute(params) }
    protected abstract suspend fun execute(params: P): R
}

object NoParams
