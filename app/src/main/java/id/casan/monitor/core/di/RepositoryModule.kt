package id.casan.monitor.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.casan.monitor.data.repository.MockIncidentRepository
import id.casan.monitor.domain.repository.IncidentRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun incidents(repository: MockIncidentRepository): IncidentRepository
}
