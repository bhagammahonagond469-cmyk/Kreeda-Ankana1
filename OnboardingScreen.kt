package com.kreedaankana.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreedaankana.model.Sport
import com.kreedaankana.ui.components.SportChip
import com.kreedaankana.ui.theme.*
import com.kreedaankana.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    var teamName     by remember { mutableStateOf("") }
    var captainName  by remember { mutableStateOf("") }
    var captainPhone by remember { mutableStateOf("") }
    var village      by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf(Sport.CRICKET) }

    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SportGreen, SportGreenMid, Color(0xFF1A2E1B))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Logo / Title
            Text("🏟️", fontSize = 72.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Kreeda-Ankana",
                style     = MaterialTheme.typography.headlineLarge,
                color     = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign  = TextAlign.Center
            )
            Text(
                "Sports Ground & Match Organizer",
                style     = MaterialTheme.typography.bodyLarge,
                color     = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Text(
                "\"Turning Village Grounds into Organised Sports Hubs\"",
                style     = MaterialTheme.typography.bodySmall,
                color     = SportOrangeLight,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Registration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        "Register Your Team",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SportGreen
                    )
                    Text(
                        "Create your team profile to start booking slots",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    KreedaTextField(
                        value = teamName, onValueChange = { teamName = it },
                        label = "Team Name", icon = Icons.Default.Group,
                        placeholder = "e.g., Sunrise XI"
                    )
                    Spacer(Modifier.height(12.dp))
                    KreedaTextField(
                        value = captainName, onValueChange = { captainName = it },
                        label = "Captain's Name", icon = Icons.Default.Person,
                        placeholder = "Your full name"
                    )
                    Spacer(Modifier.height(12.dp))
                    KreedaTextField(
                        value = captainPhone, onValueChange = { captainPhone = it },
                        label = "Mobile Number", icon = Icons.Default.Phone,
                        placeholder = "+91 XXXXX XXXXX",
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(12.dp))
                    KreedaTextField(
                        value = village, onValueChange = { village = it },
                        label = "Village / Area", icon = Icons.Default.LocationOn,
                        placeholder = "e.g., Rajpur Village"
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Primary Sport", style = MaterialTheme.typography.labelLarge,
                        color = SportGreen, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Sport.values().forEach { sport ->
                            SportChip(
                                sport    = sport,
                                selected = selectedSport == sport,
                                onClick  = { selectedSport = sport }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick  = {
                            viewModel.registerTeam(
                                teamName, captainName, captainPhone, village, selectedSport
                            )
                        },
                        enabled  = teamName.isNotBlank() && captainName.isNotBlank()
                                && captainPhone.isNotBlank() && village.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SportGreenMid)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.SportsCricket, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Register Team", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Feature pills
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeaturePill("📅 Book Slots")
                FeaturePill("⚔️ Challenges")
                FeaturePill("🏆 Leaderboard")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KreedaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = SportGreenMid) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = SportGreenMid,
            focusedLabelColor    = SportGreenMid,
            cursorColor          = SportGreenMid
        ),
        singleLine = true
    )
}

@Composable
private fun FeaturePill(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Text(
            text, color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold
        )
    }
}
