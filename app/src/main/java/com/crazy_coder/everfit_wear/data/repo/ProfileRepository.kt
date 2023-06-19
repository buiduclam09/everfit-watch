package com.crazy_coder.everfit_wear.data.repo

import com.crazy_coder.everfit_wear.data.remote.api.HazmatApi
import com.crazy_coder.everfit_wear.data.mapper.mapToUser
import com.crazy_coder.everfit_wear.data.model.User
import com.crazy_coder.everfit_wear.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: HazmatApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getUsersActive(): Flow<List<User>> {
        return flow { emit(api.getAllUsersActive().values.map { it.mapToUser() }) }
            .flowOn(ioDispatcher)
    }
}