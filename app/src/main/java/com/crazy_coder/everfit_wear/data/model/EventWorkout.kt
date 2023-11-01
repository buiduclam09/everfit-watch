package com.crazy_coder.everfit_wear.data.model

import kotlin.random.Random

data class EventWorkout(val title: String?, val event: String?, val hashInt : Long = Random.nextLong())
