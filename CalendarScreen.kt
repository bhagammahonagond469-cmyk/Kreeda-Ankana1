package com.kreedaankana.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(navController: NavController, viewModel: MainViewModel) {
    val slots by viewModel.slots.collectAsState()
    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()
    val next7Days = (0..6).map { today.plusDays(it.toLong()) }

    var selectedDate by remember { mutableStateOf(today) }
    var selectedSport by remember { mutableStateOf(Sport.CRICKET) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        GradientHeader {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Ground Calendar", style = MaterialTheme.typography.headlineSmall,
                        color = Color.White, fontWeight = FontWeight.ExtraBold)
                    Text("Tap a free slot to book", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.75f))
                }
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.BookSlot.route) },
                    containerColor = SportOrange,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Book Slot", tint = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Day Selector
            Text("Select Date", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(next7Days) { date ->
                    DayChip(date = date, selected = date == selectedDate,
                        onClick = { selectedDate = date })
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sport Selector
            Text("Sport", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Sport.values()) { sport ->
                    SportChip(sport = sport, selected = selectedSport == sport,
                        onClick = { selectedSport = sport })
                }
            }

            Spacer(Modifier.height(20.dp))

            // Slot Grid
            Text(
                "${selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
                selectedDate.format(DateTimeFormatter.ofPattern("d MMMM")),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot(SlotAvailable, "Free")
                LegendDot(SlotBooked, "Booked")
                LegendDot(SlotPast, "Past")
            }
            Spacer(Modifier.height(12.dp))

            // Time slots grid
            viewModel.timeSlots.forEach { (start, end) ->
                val dateStr = selectedDate.format(dateFmt)
                val status = viewModel.getSlotStatus(dateStr, start, selectedSport)
                val bookedSlot = slots.find {
                    it.date == dateStr && it.startTime == start && it.sport == selectedSport.name
                }
                SlotRow(
                    startTime = start, endTime = end, status = status,
                    teamName  = bookedSlot?.teamName ?: "",
                    onClick   = {
                        navController.navigate(Screen.BookSlot.route)
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Quick stats
            val daySlots = slots.filter { it.date == selectedDate.format(dateFmt) && it.sport == selectedSport.name }
            val booked = daySlots.count { it.status == "BOOKED" }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SportGreenSurface)
            ) {
                Row(Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("${selectedSport.emoji} ${selectedSport.displayName}", "Sport")
                    StatItem("$booked/${viewModel.timeSlots.size}", "Slots Booked")
                    StatItem("${viewModel.timeSlots.size - booked}", "Available")
                }
            }
        }
    }
}

@Composable
private fun DayChip(date: LocalDate, selected: Boolean, onClick: () -> Unit) {
    val today = LocalDate.now()
    val isToday = date == today
    Surface(
        modifier = Modifier
            .width(56.dp)
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(12.dp),
        color  = when {
            selected -> SportGreenMid
            isToday  -> SportGreenSurface
            else     -> MaterialTheme.colorScheme.surfaceVariant
        },
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
            Text(
                date.dayOfMonth.toString(),
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            if (isToday) {
                Box(
                    Modifier.size(5.dp).clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (selected) Color.White else SportOrange)
                )
            }
        }
    }
}

@Composable
private fun SlotRow(
    startTime: String, endTime: String,
    status: com.kreedaankana.model.SlotStatus,
    teamName: String, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$startTime–$endTime", style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        Spacer(Modifier.weight(1f))
        SlotCell(status = status, teamName = teamName, onClick = onClick,
            modifier = Modifier.width(120.dp))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = SportGreenMid)
        Text(label, style = MaterialTheme.typography.labelSmall, color = SportGreenMid.copy(0.7f))
    }
}
