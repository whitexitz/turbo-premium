package com.turbobooster
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TurboApp() }
    }
}

@Composable
fun TurboApp() {
    var turboAtivo by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0F)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("TURBO BOOSTER", color = Color(0xFF00FF88), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { turboAtivo = !turboAtivo },
                colors = ButtonDefaults.buttonColors(containerColor = if (turboAtivo) Color(0xFF00FF88) else Color(0xFF1A1A2E))
            ) {
                Text(if (turboAtivo) "ATIVO" else "ATIVAR TURBO", color = Color.White, fontSize = 16.sp)
            }
            Text(
                if (turboAtivo) "Otimizando..." else "Pronto para otimizar",
                color = Color(0xFF6A6A80), fontSize = 14.sp
            )
        }
    }
}
