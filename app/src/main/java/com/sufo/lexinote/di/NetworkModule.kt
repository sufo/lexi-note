package com.sufo.lexinote.di

import com.sufo.lexinote.BuildConfig
import com.sufo.lexinote.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by sufo on 2025/7/21 22:04.
 *
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    //region OkHttp
    /**
     * 提供一个全局的 OkHttpClient 实例。
     * Ktor 和 Coil 都将使用这个 OkHttpClient，确保网络行为统一。
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .proxy(Proxy.NO_PROXY) //设置可绕过系统代理直接发包（避免抓包）
            // 添加一个 HttpLoggingInterceptor 用于打印网络请求和响应日志
            //.addInterceptor(HttpLoggingInterceptor())
            // 拦截器放在ktor里面添加
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        okHttp: OkHttpClient,
        authInterceptor: AuthInterceptor,
//        loggingInterceptor: LoggingInterceptor,
    ): HttpClient {
        return HttpClient(OkHttp){
            expectSuccess = false  //禁止自动抛出非2xxx异常
            //配置内容协商（例如json）
            install(ContentNegotiation){
                //先安装的优先级较低
                json(Json {
                    prettyPrint = BuildConfig.DEBUG //美化输出
                    isLenient = true //宽松解析。比如允许JSON键名不带引号
                    ignoreUnknownKeys = true //或略JSON中未知字段
                    explicitNulls = false //是否序列化null值，false表示不系列化null值（即如果属性为null，则不出现在JSON中）
                })
            }
            //日志
            install(Logging) {
                level = if(BuildConfig.DEBUG) LogLevel.ALL else LogLevel.HEADERS
            }
            // install(HttpResponseValidator) {
            //     validateResponse { response ->
            //         val statusCode = response.status.value
            //         if (statusCode >= 300) {
            //             val failureReason = when (statusCode) {
            //                 in 300..399 -> "Redirection"
            //                 in 400..499 -> "Client Error"
            //                 in 500..599 -> "Server Error"
            //                 else -> "Unknown Error"
            //             }
            //             throw Exception("HTTP Error: $statusCode - $failureReason")
            //         }
            //     }
            // }
            defaultRequest {
                header(HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Json)
                url(BuildConfig.BASE_URL)
            }
            //http引擎（我这里用的是OkHttp）配置
            engine {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = if(BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                })
                addInterceptor(authInterceptor)
            }
        }
    }

}