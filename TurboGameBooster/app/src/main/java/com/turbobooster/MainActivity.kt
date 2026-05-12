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
import com.turbobooster.ui.theme.BgDeep
import com.turbobooster.ui.theme.TurboTheme

class MainActivity : ComponentActivity() {
    private val overlayLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuManager.init()
        if (!Settings.canDrawOverlays(this)) {
            overlayLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
        setContent {
            TurboTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BgDeep) {
                    NavGraph()
                }
            }
        }
    }

    override fun onDestroy() {
        ShizukuManager.destroy()
        super.onDestroy()
    }
}
