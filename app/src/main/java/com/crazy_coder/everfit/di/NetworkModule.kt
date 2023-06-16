package com.crazy_coder.everfit.di

import android.app.Application
import com.crazy_coder.everfit.data.local.datastore.PreferenceDataStore
import com.crazy_coder.everfit.data.remote.api.HazmatApi
import com.crazy_coder.everfit.data.remote.api.WorkoutApi
import com.crazy_coder.everfit.data.remote.api.middleware.DefaultInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    companion object {
        private const val READ_TIMEOUT: Long = 60
        private const val WRITE_TIMEOUT: Long = 60
        private const val CONNECTION_TIMEOUT: Long = 10
        const val BACKEND_URL = "https://f9ab-59-153-247-89.ap.ngrok.io"
    }

    @Provides
    @Singleton
    fun provideWorkoutApi(): WorkoutApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://demo5364288.mockable.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(WorkoutApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHazmatApi(retrofit: Retrofit): HazmatApi {
        return retrofit.create(HazmatApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpCache(app: Application): Cache {
        val cacheSize: Long = 10 * 1024 * 1024 // 10 MiB
        return Cache(app.cacheDir, cacheSize)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(cache: Cache, interceptor: Interceptor): OkHttpClient {
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.cache(cache)
        httpClientBuilder.addInterceptor(interceptor)

        httpClientBuilder.readTimeout(
            READ_TIMEOUT, TimeUnit.SECONDS
        )
        httpClientBuilder.writeTimeout(
            WRITE_TIMEOUT, TimeUnit.SECONDS
        )
        httpClientBuilder.connectTimeout(
            CONNECTION_TIMEOUT, TimeUnit.SECONDS
        )

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        httpClientBuilder.addInterceptor(logging)

        return httpClientBuilder.build()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Singleton
    @Provides
    fun provideInterceptor(preferenceDataStore: PreferenceDataStore): Interceptor {
        return DefaultInterceptor(preferenceDataStore)
    }
}