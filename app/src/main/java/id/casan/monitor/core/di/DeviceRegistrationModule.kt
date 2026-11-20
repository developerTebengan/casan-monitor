package id.casan.monitor.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.casan.monitor.notification.AdminDeviceRegistration
import id.casan.monitor.notification.MonitorDeviceRegistration

@Module
@InstallIn(SingletonComponent::class)
interface DeviceRegistrationModule {
    @Binds
    fun registration(implementation: AdminDeviceRegistration): MonitorDeviceRegistration
}
