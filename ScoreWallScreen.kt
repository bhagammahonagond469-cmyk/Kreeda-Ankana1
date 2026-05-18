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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kreedaankana.model.Sport
import com.kreedaankana.navigation.Screen
import com.kreedaankana.ui.components.*
import com.kreedaankana.ui.theme.*
import com.kreedaankana.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Score Wall ────────────────────────────────────────────────────────────────
@Composable
fun ScoreWallScreen(navController: NavController, viewModel: MainViewModel) {
    val results   by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(Modifier.fillMaxSize()) {
        GradientHeader {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Score Wall", style = MaterialTheme.typography.headlineSmall,
                        color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Text("Latest village match results", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.75f))
                }
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.PostResult.route) },
                    containerColor = SportOrange, modifier = Modifier.size(48.dp)
                ) { Icon(Icons.Default.Add, null, tint = Color.White) }
            }
        }

        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No results yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Post your first match result!", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results, key = { it.id }) { result ->
                    ResultCard(result)
                }
            }
        }
    }
}

// ── Post Result ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostResultScreen(navController: NavController, viewModel: MainViewModel) {
    val myTeamName by viewModel.currentTeamName.collectAsState()
    var opponentName  by remember { mutableStateOf("") }
    var myScore       by remember { mutableStateOf("") }
    var opponentScore by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf(Sport.CRICKET) }
    var winnerIsMe    by remember { mutableStateOf(true) }
    val isLoading     by viewModel.isLoading.collectAsState()
    val message       by viewModel.message.collectAsState()
    val snackbar       = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it.text); viewModel.clearMessage()
            if (!it.isError) navController.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Post Result", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SportGreen, titleContentColor = Color.White,
                    navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {
            SectionHeader("Match Result", Icons.Default.EmojiEvents)
            Spacer(Modifier.height(20.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Your Team", style = MaterialTheme.typography.labelLarge, color = SportGreenMid,
                        fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = myTeamName, onValueChange = {},
                        modifier = Modifier.fillMaxWidth(), enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Opponent", style = MaterialTheme.typography.labelLarge, color = SportGreenMid,
                        fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = opponentName, onValueChange = { opponentName = it },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Team name") },
                        shape = RoundedCornerShape(12.dp), singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Scores", style = MaterialTheme.typography.labelLarge, color = SportGreenMid,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = myScore, onValueChange = { myScore = it },
                    label = { Text("Your Score") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
                Text("VS", modifier = Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                OutlinedTextField(
                    value = opponentScore, onValueChange = { opponentScore = it },
                    label = { Text("Opponent") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Sport", style = MaterialTheme.typography.labelLarge, color = SportGreenMid,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sport.values().take(3).forEach { sport ->
                    SportChip(sport = sport, selected = selectedSport == sport,
                        onClick = { selectedSport = sport })
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sport.values().drop(3).forEach { sport ->
                    SportChip(sport = sport, selected = selectedSport == sport,
                        onClick = { selectedSport = sport })
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Winner", style = MaterialTheme.typography.labelLarge, color = SportGreenMid,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = winnerIsMe, onClick = { winnerIsMe = true },
                    label = { Text("🏆 $myTeamName", fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SportGreenMid, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = !winnerIsMe, onClick = { winnerIsMe = false },
                    label = { Text("🏆 $opponentName".ifEmpty { "🏆 Opponent" }, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SportOrange, selectedLabelColor = Color.White)
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.postResult(
                        team1Name  = myTeamName, team1Score = myScore,
                        team2Name  = opponentName, team2Score = opponentScore,
                        sport      = selectedSport,
                        date       = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        winnerName = if (winnerIsMe) myTeamName else opponentName
                    )
                },
                enabled  = opponentName.isNotBlank() && myScore.isNotBlank() && opponentScore.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SportGreenMid)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else {
                    Icon(Icons.Default.Scoreboard, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Post Result", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
