package com.crazy_coder.everfit_wear.data.repo

import com.crazy_coder.everfit_wear.data.remote.api.HazmatApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class AuthRepository @Inject constructor(
    private val api: HazmatApi
) {

    fun login(username: String, password: String): Flow<Result<String>> {
        return suspend { runCatching {api.login(username, password)} }.asFlow()
    }

    fun saveCurrentUser(result: Result<String>) {

    }
}