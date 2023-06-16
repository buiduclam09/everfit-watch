package com.crazy_coder.everfit.data.repo

import com.crazy_coder.everfit.data.remote.api.HazmatApi
import com.crazy_coder.everfit.data.mapper.mapToRoom
import com.crazy_coder.everfit.data.model.Room
import com.crazy_coder.everfit.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val api: HazmatApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getAllRoom(): Flow<List<Room>> {
        return flow { emit(api.getAllRoom().values.map { it.mapToRoom() }) }
            .flowOn(ioDispatcher)
    }

}