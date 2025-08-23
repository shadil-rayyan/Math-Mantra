package com.zendalona.zmantra.di

import android.content.Context
import com.zendalona.zmantra.data.repository.userguide.UserGuideRepositoryImpl
import com.zendalona.zmantra.domain.repository.UserGuideRepository
import com.zendalona.zmantra.domain.usecase.userguide.GetUserGuideHtmlUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UserGuideModule {

        @Provides
        fun provideUserGuideRepository(
                @ApplicationContext context: Context
        ): UserGuideRepository = UserGuideRepositoryImpl(context)

        @Provides
        fun provideGetUserGuideHtmlUseCase(
                repository: UserGuideRepository
        ): GetUserGuideHtmlUseCase = GetUserGuideHtmlUseCase(repository)
}
