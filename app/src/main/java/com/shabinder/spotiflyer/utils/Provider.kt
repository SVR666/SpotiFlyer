/*
 * Copyright (C)  2020  Shabinder Singh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.utils

import android.content.Context
import com.github.kiulian.downloader.YoutubeDownloader
import com.shabinder.spotiflyer.App
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.database.DatabaseDAO
import com.shabinder.spotiflyer.database.DownloadRecordDatabase
import com.shreyaspatil.EasyUpiPayment.EasyUpiPayment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object Provider {

    @Provides
    fun databaseDAO(@ApplicationContext appContext: Context):DatabaseDAO{
        return DownloadRecordDatabase.getInstance(appContext).databaseDAO
    }



    @Provides
    @Singleton
    fun provideUpi():EasyUpiPayment {
        return EasyUpiPayment.Builder()
            .with(MainActivity.getInstance())
            .setPayeeVpa("technoshab@paytm")
            .setPayeeName("Shabinder Singh")
            .setTransactionId("UNIQUE_TRANSACTION_ID")
            .setTransactionRefId("UNIQUE_TRANSACTION_REF_ID")
            .setDescription("Thanks for donating")
            .setAmount("39.00")
            .build()
    }

    @Provides
    @Singleton
    fun getMoshi():Moshi{
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun getYTDownloader():YoutubeDownloader{
        return YoutubeDownloader()
    }

    @Provides
    @Singleton
    fun getSpotifyTokenInterface():SpotifyServiceTokenRequest{
        val httpClient2: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient2.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request =
                    chain.request().newBuilder().addHeader(
                        "Authorization",
                        "Basic ${android.util.Base64.encodeToString("${App.clientId}:${App.clientSecret}".toByteArray(),android.util.Base64.NO_WRAP)}"
                    ).build()
                return chain.proceed(request)
            }
        })

        val retrofit = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(httpClient2.build())
            .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
            .build()
        return retrofit.create(SpotifyServiceTokenRequest::class.java)
    }

}