package com.quickshield.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

private const val API_URL =
    "https://quickshield-vpn-api.onrender.com/v1/servers"

private data class VPNServer(
    val id: String,
    val country: String,
    val city: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickShieldScreen()
        }
    }
}

@Composable
private fun QuickShieldScreen() {
    val bg = Color(0xFF07111F)
    val panel = Color(0xFF111F31)
    val cyan = Color(0xFF42D9F5)
    val scope = rememberCoroutineScope()

    var servers by remember { mutableStateOf<List<VPNServer>>(emptyList()) }
    var message by remember { mutableStateOf("Checking server list...") }
    var loading by remember { mutableStateOf(false) }

    suspend fun loadServers() {
        loading = true
        message = "Connecting to QuickShield API..."
        try {
            val result = withContext(Dispatchers.IO) {
                val connection = URL(API_URL).openConnection()
                    as HttpURLConnection
                try {
                    connection.connectTimeout = 15000
                    connection.readTimeout = 15000
                    connection.requestMethod = "GET"
                    if (connection.responseCode !in 200..299) {
                        error("API returned HTTP ${connection.responseCode}")
                    }
                    val body = connection.inputStream.bufferedReader().use {
                        it.readText()
                    }
                    val array = JSONArray(body)
                    buildList {
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            add(
                                VPNServer(
                                    item.getString("id"),
                                    item.getString("country"),
                                    item.getString("city")
                                )
                            )
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            }
            servers = result
            message = if (result.isEmpty()) {
                "No active VPN servers yet. Server setup is the next step."
            } else {
                "${result.size} server(s) available"
            }
        } catch (e: Exception) {
            message = "Could not load servers. Check internet and try again."
        }
        loading = false
    }

    LaunchedEffect(Unit) {
        loadServers()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = cyan,
            background = bg,
            surface = panel
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(20.dp)
        ) {
            Text(
                "QUICKSHIELD",
                color = cyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Text(
                "VPN",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(panel, RoundedCornerShape(24.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color(0xFF193449), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 38.sp)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Not connected",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Choose a server to get started",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        message =
                            "VPN tunnel is not configured yet. This button does not connect."
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFF254356),
                        disabledContentColor = cyan
                    )
                ) {
                    Text("CONNECT (SETUP REQUIRED)")
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "SERVER LOCATIONS",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.LightGray, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))

            if (servers.isEmpty()) {
                Text(
                    "No locations are active yet.",
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(servers) { server ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(panel, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌐", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    server.country,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    server.city,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { scope.launch { loadServers() } },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Loading..." else "Refresh server list")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Early preview • VPN connection not active",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}
