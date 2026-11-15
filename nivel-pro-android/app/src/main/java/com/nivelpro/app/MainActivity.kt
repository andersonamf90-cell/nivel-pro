package com.nivelpro.app

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nivelpro.app.ui.PlumbScreen
import com.nivelpro.app.ui.PlumbViewModel
import com.nivelpro.app.ui.theme.NivelProTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PlumbViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mantém a tela ligada durante o uso em campo e configura barras pretas OLED
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )

        setContent {
            NivelProTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> viewModel.startSensors()
                            Lifecycle.Event.ON_PAUSE -> viewModel.stopSensors()
                            else -> Unit
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        viewModel.stopSensors()
                    }
                }

                PlumbScreen(
                    state = state,
                    onToggleFreeze = viewModel::toggleFreeze,
                    onToggleBeep = viewModel::toggleBeep,
                    onOpenCalibration = viewModel::openCalibrationDialog,
                    onCloseCalibration = viewModel::closeCalibrationDialog,
                    onCalibrateZero = viewModel::calibrateZero,
                    onResetCalibration = viewModel::resetCalibration,
                    onOpenSettings = viewModel::openSettingsSheet,
                    onCloseSettings = viewModel::closeSettingsSheet,
                    onToleranceChange = viewModel::updateTolerance,
                    onSmoothingChange = viewModel::updateSmoothingPreset
                )
            }
        }
    }
}
