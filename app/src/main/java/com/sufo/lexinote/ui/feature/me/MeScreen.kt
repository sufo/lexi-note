package com.sufo.lexinote.ui.feature.me

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sufo.lexinote.R
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import com.sufo.lexinote.ui.theme.LexiNoteTheme
import com.sufo.lexinote.utils.AppUtils
import java.util.Locale

@Composable
fun MeScreen(
    nav: NavigationService,
    viewModel: MeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.saveExportedFile(it)
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.onFileSelectedForImport(it)
            }
        }
    )

    LaunchedEffect(uiState.exportFile) {
        uiState.exportFile?.let {
            val fileName = "lexinote_backup_${System.currentTimeMillis()}.zip"
            exportLauncher.launch(fileName)
        }
    }

    LaunchedEffect(uiState.triggerImport) {
        if (uiState.triggerImport) {
            importLauncher.launch("application/zip")
            viewModel.onImportTriggered()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.moomin), // Placeholder avatar
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Alex Doe", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Joined July 2025", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (uiState.isLoading) {
                    // You can show a placeholder or a loading indicator for each metric
                    MetricItem(value = "-", label = stringResource(R.string.mastered))
                    MetricItem(value = "-", label = stringResource(R.string.study_days))
                    MetricItem(value = "-", label = stringResource(R.string.current_streak))
                } else {
                    MetricItem(value = uiState.masteredCount.toString(), label = stringResource(R.string.mastered))
                    MetricItem(value = uiState.studyDays.toString(), label = stringResource(R.string.study_days))
                    MetricItem(value = uiState.currentStreak.toString(), label = stringResource(R.string.current_streak))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Learning Activity
//            Column {
//                Text("Learning Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(12.dp))
//                Card(
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                ) {
//                    // Heatmap placeholder
//                    Box(modifier = Modifier
//                        .fillMaxWidth()
//                        .height(120.dp)
//                        .padding(16.dp)
//                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
//                    ) {
//                        Text("Learning Activity Heatmap", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Achievements
//            Column {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
//                    Text("View All", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
//                }
//                Spacer(modifier = Modifier.height(12.dp))
//                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                    item { AchievementItem(title = "Early Bird", icon = R.drawable.ic_early_bird) }
//                    item { AchievementItem(title = "First Steps", icon = R.drawable.ic_first_steps) }
//                    item { AchievementItem(title = "7-Day Streak", icon = R.drawable.ic_streak) }
//                    item { AchievementItem(title = "Word Master", icon = R.drawable.ic_locked, locked = true) }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))

            // Settings List
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SettingsItem(title = stringResource(R.string.setting_preference), icon = Icons.Default.Settings, onClick = {
                        nav.navigate(Screen.ThemeSettings.route)
                    })
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(title = stringResource(R.string.export_data), icon = Icons.Default.Upload, onClick = {
                        viewModel.onExportClicked()
                    })
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(title = stringResource(R.string.import_data), icon = Icons.Default.Download, onClick = {
                        viewModel.onImportClicked()
                    })
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(title = stringResource(R.string.version), icon = Icons.Outlined.Info, desc = AppUtils.getAppVersionName(LocalContext.current))
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun MetricItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun AchievementItem(title: String, icon: Int, locked: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(if (locked) Color.LightGray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = if (locked) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if(locked) Color.Gray else Color.Unspecified)
    }
}

@Composable
fun SettingsItem(title: String, icon: ImageVector, desc:String?=null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        desc?.let{Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)}
        if(onClick != null) {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

//@Composable
//fun showToast(){
//    val context = LocalContext.current
//    Toast.makeText(context, stringResource(R.string.developing), Toast.LENGTH_SHORT).show()
//}

@Preview(showBackground = true)
@Composable
fun MeScreenPreview() {
    LexiNoteTheme {
        MeScreen(NavigationService())
    }
}