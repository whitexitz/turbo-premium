package com.turbobooster.ui.screens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.turbobooster.shizuku.ShizukuManager
import com.turbobooster.shizuku.ShizukuStatus
import com.turbobooster.ui.theme.*

fun hasOverlayPermission(context: Context): Boolean = Settings.canDrawOverlays(context)

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun PermissionsScreen(onContinuar: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var overlayOk by remember { mutableStateOf(hasOverlayPermission(context)) }
    var usageOk by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    val shizukuStatus by ShizukuManager.status.collectAsState()
    val shizukuOk = shizukuStatus == ShizukuStatus.CONECTADO

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                overlayOk = hasOverlayPermission(context)
                usageOk = hasUsageStatsPermission(context)
                ShizukuManager.verificar()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            "PERMISSÕES",
            color = NeonGreen,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Text(
            "Conceda as permissões abaixo para ativar o monitoramento e as otimizações.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(4.dp))

        PermissaoItem(
            titulo = "Overlay (Draw over apps)",
            desc = "Necessário para exibir FPS/CPU sobre jogos em tempo real.",
            ok = overlayOk,
            essencial = true,
            onConceder = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        )

        PermissaoItem(
            titulo = "Uso de apps (Usage Stats)",
            desc = "Permite detectar qual jogo está ativo para aplicar otimizações automáticas.",
            ok = usageOk,
            essencial = false,
            onConceder = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )

        PermissaoItem(
            titulo = "Shizuku",
            desc = "Habilita comandos privilegiados: matar processos, limpar cache, desativar animações.",
            ok = shizukuOk,
            essencial = false,
            onConceder = { ShizukuManager.solicitarPermissao() }
        )

        Spacer(Modifier.weight(1f))

        if (!overlayOk) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonOrange.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .border(1.dp, NeonOrange.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "⚠ A permissão de Overlay é obrigatória para continuar.",
                    color = NeonOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = onContinuar,
            enabled = overlayOk,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen.copy(alpha = 0.15f),
                disabledContainerColor = BgCard
            ),
            border = BorderStroke(
                1.dp,
                if (overlayOk) NeonGreen.copy(alpha = 0.6f) else BorderNeon
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "ENTRAR NO APP",
                color = if (overlayOk) NeonGreen else TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PermissaoItem(
    titulo: String,
    desc: String,
    ok: Boolean,
    essencial: Boolean,
    onConceder: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (ok) NeonGreen.copy(alpha = 0.05f) else BgCard,
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (ok) NeonGreen.copy(alpha = 0.3f) else NeonRed.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (ok) NeonGreen else NeonRed, CircleShape)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(titulo, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (essencial) {
                    Text(
                        "OBRIGATÓRIA",
                        color = NeonOrange,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
            Text(desc, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
        }
        if (!ok) {
            TextButton(onClick = onConceder, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(
                    "CONCEDER",
                    color = NeonBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Text("✓", color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}
