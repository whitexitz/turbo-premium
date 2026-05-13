package com.turbobooster

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
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

    private val usageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ShizukuManager.init()
        requestPermissions()
        setContent {
            TurboTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BgDeep) {
                    NavGraph()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ShizukuManager.verificar()
    }

    override fun onDestroy() {
        ShizukuManager.destroy()
        super.onDestroy()
    }

    private fun requestPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            overlayLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
        if (!hasUsageStatsPermission()) {
            usageLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        if (!ShizukuManager.conectado) {
            ShizukuManager.solicitarPermissao()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
