package id.casan.monitor.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.casan.monitor.data.repository.LocalNotificationRepository
import id.casan.monitor.domain.repository.NotificationRepository

@Module
@InstallIn(SingletonComponent::class)
interface NotificationModule {
    @Binds
    fun repository(implementation: LocalNotificationRepository): NotificationRepository
}
