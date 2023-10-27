package com.example.pokerunwearos.data.repository.health

import android.util.Log
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import com.example.pokerunwearos.presentation.service.PassiveDataService
import kotlinx.coroutines.guava.await
import javax.inject.Inject

class PassiveClientManager @Inject constructor(
    healthServicesClient: HealthServicesClient
) {
    private val passiveMonitoringClient = healthServicesClient.passiveMonitoringClient
    private val dataTypes = setOf(DataType.STEPS_DAILY)

    private val passiveListenerConfig = PassiveListenerConfig(
        dataTypes = dataTypes,
        shouldUserActivityInfoBeRequested = false,
        dailyGoals = setOf(),
        healthEventTypes = setOf()
    )

    suspend fun hasStepsDailyCapability(): Boolean {
        val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
        return DataType.STEPS_DAILY in capabilities.supportedDataTypesPassiveMonitoring
    }

    suspend fun registerForStepsDailyData() {
        Log.i(TAG, "Registering listener for steps daily")
        passiveMonitoringClient.setPassiveListenerServiceAsync(
            PassiveDataService::class.java,
            passiveListenerConfig
        ).await()
    }

//    suspend fun unregisterForStepsDailyData() {
//        Log.i(TAG, "Unregistering listeners")
//        passiveMonitoringClient.clearPassiveListenerServiceAsync().await()
//    }

    private companion object {
        const val TAG = "Health Passive Client"
    }
}