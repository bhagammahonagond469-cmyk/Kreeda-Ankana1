package com.kreedaankana.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreedaankana.model.Sport
import com.kreedaankana.ui.components.*
import com.kreedaankana.ui.theme.*
import com.kreedaankana.viewmodel.MainViewModel

// ── Leaderboard ───────────────────────────────────────────────────────────────
@Composable
fun LeaderboardScreen(viewModel: MainViewModel) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    var filterSport by remember { mutableStateOf<Sport?>(null) }

    val filtered = if (filterSport == null) leaderboard
    else leaderboard.filter { it.sport == filterSport!!.name }

    Column(Modifier.fillMaxSize()) {
        GradientHeader {
            Text("Village Leaderboard", style = MaterialTheme.typography.headlineSmall,
                color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text("Ranked by total wins this season", style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.75f))
        }

        // Sport filter
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterSport == null,
                onClick  = { filterSport = null },
                label    = { Text("All", fontWeight = FontWeight.Bold) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SportGreenMid, selectedLabelColor = Color.White)
            )
            Sport.values().forEach { sport ->
                FilterChip(
                    selected = filterSport == sport,
                    onClick  = { filterSport = sport },
                    label    = { Text(sport.emoji, fontSize = 16.sp) }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No teams yet", style = MaterialTheme.typography.titleMedium)
                    Text("Register and play to appear here", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.teamId }) { entry ->
                    LeaderboardRow(entry)
                }
            }
        }
    }
}

// ── Profile ───────────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val teamId    by viewModel.currentTeamId.collectAsState()
    val teamName  by viewModel.currentTeamName.collectAsState()
    val village   by viewModel.currentVillage.collectAsState()
    val sport     by viewModel.currentSport.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()

    val myEntry = leaderboard.find { it.teamId == teamId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        GradientHeader {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    try { Sport.valueOf(sport).emoji } catch (e: Exception) { "🏟️" },
                    fontSize = 56.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(teamName.ifEmpty { "My Team" }, style = MaterialTheme.typography.headlineSmall,
                    color = Color.White, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text(village, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.75f))
            }
        }

        Column(Modifier.padding(20.dp)) {
            // Stats row
            myEntry?.let { entry ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatCard("Rank", "#${entry.rank}", "🏅")
                    StatCard("Wins", "${entry.wins}", "🏆")
                    StatCard("Losses", "${entry.losses}", "📉")
                }
                Spacer(Modifier.height(20.dp))
            }

            // Team info
            SectionHeader("Team Info", Icons.Default.Group)
            Spacer(Modifier.height(12.dp))

            InfoRow(Icons.Default.Group, "Team Name", teamName)
            InfoRow(Icons.Default.LocationOn, "Village", village)
            InfoRow(
                Icons.Default.SportsCricket, "Primary Sport",
                try { "${Sport.valueOf(sport).emoji} ${Sport.valueOf(sport).displayName}" }
                catch (e: Exception) { sport }
            )

            Spacer(Modifier.height(24.dp))
            SectionHeader("Quick Actions", Icons.Default.Bolt)
            Spacer(Modifier.height(12.dp))

            // How-to guide
            InfoCard("📅 Book a Slot",
                "Go to Calendar → tap any green (Free) cell or use the + button to reserve your time.")
            Spacer(Modifier.height(8.dp))
            InfoCard("⚔️ Post a Challenge",
                "Head to Challenge Board → tap + to post an open challenge. Other teams can accept it!")
            Spacer(Modifier.height(8.dp))
            InfoCard("📊 Post Results",
                "After your match, go to Score Wall → + to record the final score.")
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, emoji: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SportGreenSurface),
        modifier = Modifier.width(96.dp)
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold, color = SportGreen)
            Text(label, style = MaterialTheme.typography.labelSmall, color = SportGreenMid)
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, value: String
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SportGreenMid, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Text(value.ifEmpty { "—" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun InfoCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
        }
    }
}
