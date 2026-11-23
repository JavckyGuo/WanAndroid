package com.jacky.wanandroid.domain.usecase

/**
 * 用例统一模板：捕获执行异常并以 [Result] 暴露给 ViewModel。
 */
abstract class BaseUseCase<in P, out R> {
    suspend operator fun invoke(params: P): Result<R> = runCatching { execute(params) }
    protected abstract suspend fun execute(params: P): R
}

/** 无入参用例的类型安全占位对象。 */
object NoParams
