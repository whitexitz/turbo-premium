package com.turbobooster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.turbobooster.shizuku.ShizukuManager
import com.turbobooster.ui.navigation.NavGraph
import com.turbobooster.ui.theme.FundoPrincipal
import com.turbobooster.ui.theme.TurboBoosterTheme

class MainActivity : ComponentActivity() {

    private val overlayPermLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Resultado verificado ao retornar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ShizukuManager.inicializar()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermLauncher.launch(intent)
        }

        setContent {
            TurboBoosterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FundoPrincipal
                ) {
                    NavGraph()
                }
            }
        }
    }

    override fun onDestroy() {
        ShizukuManager.destruir()
        super.onDestroy()
    }
}
