package com.lambui.everfit.data.repo

import android.util.Log
import com.lambui.everfit.data.remote.api.WorkoutApi
import com.lambui.everfit.data.dao.WorkoutDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutApi: WorkoutApi,
    private val workoutDao: WorkoutDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun toggleAssignmentCompletion(workoutId: String, assignmentId: String) {
        withContext(ioDispatcher) {
            try {
                val workout = workoutDao.getWorkoutById(workoutId)
                val newAssignments = workout.assignments.map { assignment ->
                    if (assignment.id == assignmentId) {
                        val newStatus = if (assignment.status == 0) {
                            2
                        } else {
                            0
                        }
                        assignment.copy(status = newStatus)
                    } else {
                        assignment
                    }
                }
                val newWorkout = workout.copy(assignments = newAssignments)
                workoutDao.update(newWorkout)
            } catch (e: Throwable) {
                Log.e("WorkoutRepository", "Error", e)
            }
        }
    }

    suspend fun fetchWorkouts() = withContext(ioDispatcher) {
        try {
            val workouts = workoutApi.getWorkouts().data.mapIndexed { index, workout ->
                workout.copy(id = "${index}_${workout.id}")
            }
            workoutDao.insertAll(workouts = workouts.toTypedArray())
        } catch (e: Throwable) {
            Log.e("WorkoutRepository", "Error", e)
        }
    }

    suspend fun getWorkouts() = withContext(ioDispatcher) {
        workoutDao.getAll()
    }
}