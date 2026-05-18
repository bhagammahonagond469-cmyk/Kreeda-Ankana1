package com.kreedaankana.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.navigation.NavController
import com.kreedaankana.model.Sport
import com.kreedaankana.ui.components.GradientHeader
import com.kreedaankana.ui.components.SportChip
import com.kreedaankana.ui.theme.*
import com.kreedaankana.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSlotScreen(navController: NavController, viewModel: MainViewModel) {
    val dateFmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today     = LocalDate.now()
    val next7Days = (0..6).map { today.plusDays(it.toLong()) }

    var selectedDate  by remember { mutableStateOf(today) }
    var selectedSport by remember { mutableStateOf(Sport.CRICKET) }
    var selectedStart by remember { mutableStateOf("") }
    var selectedEnd   by remember { mutableStateOf("") }
    val isLoading     by viewModel.isLoading.collectAsState()
    val message       by viewModel.message.collectAsState()
    val snackbarState  = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarState.showSnackbar(it.text)
            viewModel.clearMessage()
            if (!it.isError) navController.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            TopAppBar(
                title = { Text("Book a Slot", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SportGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Team info banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SportGreenSurface)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, tint = SportGreenMid, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(viewModel.currentTeamName.collectAsState().value,
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                            color = SportGreen)
                        Text(viewModel.currentVillage.collectAsState().value,
                            style = MaterialTheme.typography.bodySmall, color = SportGreenMid)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionLabel("Select Date")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(next7Days) { date ->
                    val label = if (date == today) "Today" else
                        date.format(DateTimeFormatter.ofPattern("EEE d"))
                    FilterChip(
                        selected = date == selectedDate,
                        onClick  = { selectedDate = date },
                        label    = { Text(label, fontWeight = FontWeight.SemiBold) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SportGreenMid,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Select Sport")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Sport.values()) { sport ->
                    SportChip(sport = sport, selected = selectedSport == sport,
                        onClick = { selectedSport = sport })
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Select Time Slot")
            val dateStr = selectedDate.format(dateFmt)
            viewModel.timeSlots.forEach { (start, end) ->
                val status   = viewModel.getSlotStatus(dateStr, start, selectedSport)
                val isFree   = status == com.kreedaankana.model.SlotStatus.FREE
                val selected = selectedStart == start
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = if (selected) CardDefaults.outlinedCardBorder()
                               else CardDefaults.outlinedCardBorder(),
                    onClick  = { if (isFree) { selectedStart = start; selectedEnd = end } }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isFree) SportGreenMid else SlotPast
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("$start – $end", fontWeight = FontWeight.SemiBold)
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                selected -> SportGreenMid
                                isFree   -> SlotAvailable.copy(0.15f)
                                else     -> SlotBooked.copy(0.15f)
                            }
                        ) {
                            Text(
                                when {
                                    selected -> "✓ Selected"
                                    isFree   -> "Free"
                                    else     -> status.name.lowercase().replaceFirstChar { it.uppercase() }
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style    = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    selected -> Color.White
                                    isFree   -> SlotAvailable
                                    else     -> SlotBooked
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.bookSlot(
                        date      = selectedDate.format(dateFmt),
                        startTime = selectedStart,
                        endTime   = selectedEnd,
                        sport     = selectedSport
                    )
                },
                enabled  = selectedStart.isNotEmpty() && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = SportGreenMid)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.BookOnline, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Booking", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = SportGreenMid,
        fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
}
