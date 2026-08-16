package com.whatsThatLink.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.whatsThatLink.app.data.RecentScan
import com.whatsThatLink.app.ui.theme.WhatsThatLinkTheme
import java.text.SimpleDateFormat
import java.util.*

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavHostController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications permission denied. You won't receive scan alerts.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        setContent {
            WhatsThatLinkTheme {
                navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WhatsThatLinkApp(viewModel, navController)
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val scanId = intent?.getLongExtra("EXTRA_SCAN_ID", -1L) ?: -1L
        if (scanId != -1L) {
            navController.navigate("result/$scanId")
        }
    }
}

@Composable
fun WhatsThatLinkApp(viewModel: MainViewModel, navController: NavHostController) {

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute in listOf("home", "history", "settings")) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = { navController.navigate("home") },
                        icon = { Icon(Icons.Default.Link, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "history",
                        onClick = { navController.navigate("history") },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { navController.navigate("settings") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen(viewModel, navController) }
            composable("history") { HistoryScreen(viewModel, navController) }
            composable("settings") { SettingsScreen() }
            composable("result/{scanId}") { backStackEntry ->
                val scanId = backStackEntry.arguments?.getString("scanId")?.toLongOrNull() ?: -1L
                ResultScreen(scanId, viewModel, navController)
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: MainViewModel, navController: NavHostController) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }
    val isScanning by viewModel.isScanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()

    LaunchedEffect(scanResult) {
        scanResult?.onSuccess { scan ->
            viewModel.clearScanResult()
            navController.navigate("result/${scan.id}")
        }?.onFailure { e ->
            viewModel.clearScanResult()
            Toast.makeText(context, "Scan failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column {
                Text(text = "WhatsThatLink?", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Before you click, know the risk.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        Text(text = "Check a link", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Paste a suspicious link below and we'll analyze it for potential phishing threats.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL") },
            placeholder = { Text("https://example.com") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (url.isNotBlank()) viewModel.scanUrl(url) },
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "SCAN LINK", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).background(color = Color(0xFF2EBD59), shape = RoundedCornerShape(50)))
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(text = "Notification monitoring", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Ready to detect suspicious links",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ResultScreen(scanId: Long, viewModel: MainViewModel, navController: NavHostController) {
    var scan by remember { mutableStateOf<RecentScan?>(null) }
    val context = LocalContext.current

    LaunchedEffect(scanId) {
        scan = viewModel.getScanById(scanId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (scan == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val s = scan!!
            val riskColor = when (s.risk.uppercase()) {
                "HIGH" -> Color(0xFFE53935)
                "MEDIUM" -> Color(0xFFFFB300)
                else -> Color(0xFF43A047)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${s.risk.uppercase()} RISK",
                            color = riskColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = s.prediction,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = riskColor
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(text = "Detected URL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = s.url, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("URL", s.url))
                        Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Copy URL")
                }

                Spacer(Modifier.height(24.dp))
                Text(text = "Security Signals", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))

                SignalRow("Phishing Probability", "${(s.phishingProbability * 100).toInt()}%")
                SignalRow("VirusTotal Malicious", s.vtMalicious.toString())
                SignalRow("VirusTotal Suspicious", s.vtSuspicious.toString())
                
                if (!s.vtAvailable) {
                    Text(
                        "Note: VirusTotal data was unavailable for this scan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))
                if (s.risk == "HIGH" || s.risk == "MEDIUM") {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = riskColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("DON'T OPEN LINK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("GO BACK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SignalRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel, navController: NavHostController) {
    val history by viewModel.history.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Scan History", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Your recently analyzed links.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))
        if (history.isEmpty()) {
            Text("No scans yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(history) { scan ->
                    ScanItem(scan) {
                        navController.navigate("result/${scan.id}")
                    }
                }
            }
        }
    }
}

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ScanItem(scan: RecentScan, onClick: () -> Unit) {
    val context = LocalContext.current
    val riskColor = when (scan.risk.uppercase()) {
        "HIGH" -> Color(0xFFE53935)
        "MEDIUM" -> Color(0xFFFFB300)
        else -> Color(0xFF43A047)
    }
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("URL", scan.url))
                    Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                }
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).background(riskColor, RoundedCornerShape(50)))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(text = scan.url, maxLines = 1, fontWeight = FontWeight.Bold)
                Text(text = scan.risk, color = riskColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = dateFormat.format(Date(scan.timestamp)), fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Notification monitoring", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Allow detection of URLs in notifications.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) {
                    Text("Manage Access")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "API Status", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Connected to: ${BuildConfig.API_URL}", fontSize = 13.sp)
            }
        }
    }
}
