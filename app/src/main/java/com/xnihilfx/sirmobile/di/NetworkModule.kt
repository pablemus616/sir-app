package com.xnihilfx.sirmobile.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.xnihilfx.sirmobile.BuildConfig
import com.xnihilfx.sirmobile.data.local.SessionStore
import com.xnihilfx.sirmobile.data.remote.ApplicationsApi
import com.xnihilfx.sirmobile.data.remote.AuthApi
import com.xnihilfx.sirmobile.data.remote.AuthInterceptor
import com.xnihilfx.sirmobile.data.remote.CandidateContactsApi
import com.xnihilfx.sirmobile.data.remote.CandidatesApi
import com.xnihilfx.sirmobile.data.remote.ClientsApi
import com.xnihilfx.sirmobile.data.remote.ContactTypesApi
import com.xnihilfx.sirmobile.data.remote.OpportunitiesApi
import com.xnihilfx.sirmobile.data.remote.PipelineStagesApi
import com.xnihilfx.sirmobile.data.remote.PositionAreasApi
import com.xnihilfx.sirmobile.data.remote.TokenAuthenticator
import dagger.Module; import dagger.Provides; import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun json(): Json = Json {
        ignoreUnknownKeys = true; isLenient = true; encodeDefaults = false
        explicitNulls = false; coerceInputValues = true
    }
    @Provides @Singleton fun logging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }
    @Provides @Singleton fun okHttp(logging: HttpLoggingInterceptor, session: SessionStore, json: Json): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(session))
            .authenticator(TokenAuthenticator(session, json, BuildConfig.API_BASE_URL))
            .addInterceptor(logging)
            .build()
    @Provides @Singleton fun retrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    @Provides @Singleton fun authApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides @Singleton fun opportunitiesApi(retrofit: Retrofit): OpportunitiesApi = retrofit.create(OpportunitiesApi::class.java)
    @Provides @Singleton fun candidatesApi(retrofit: Retrofit): CandidatesApi = retrofit.create(CandidatesApi::class.java)
    @Provides @Singleton fun candidateContactsApi(retrofit: Retrofit): CandidateContactsApi = retrofit.create(CandidateContactsApi::class.java)
    @Provides @Singleton fun contactTypesApi(retrofit: Retrofit): ContactTypesApi = retrofit.create(ContactTypesApi::class.java)
    @Provides @Singleton fun applicationsApi(retrofit: Retrofit): ApplicationsApi = retrofit.create(ApplicationsApi::class.java)
    @Provides @Singleton fun clientsApi(retrofit: Retrofit): ClientsApi = retrofit.create(ClientsApi::class.java)
    @Provides @Singleton fun positionAreasApi(retrofit: Retrofit): PositionAreasApi = retrofit.create(PositionAreasApi::class.java)
    @Provides @Singleton fun pipelineStagesApi(retrofit: Retrofit): PipelineStagesApi = retrofit.create(PipelineStagesApi::class.java)
}
