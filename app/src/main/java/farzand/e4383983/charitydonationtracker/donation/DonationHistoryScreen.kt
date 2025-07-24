package farzand.e4383983.charitydonationtracker.donation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// Donation History Screen Composable
@Composable
fun DonationHistoryScreen(firebaseManager: FirebaseManager,navController: NavHostController) {
    // Collect the flow of donations from FirebaseManager as state
    val donations by firebaseManager.getUserDonations().collectAsState(initial = emptyList())
    val userId by firebaseManager.userId.collectAsState()

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Your Donation History",
                navController = navController,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {


            if (donations.isEmpty()) {
                Text(
                    text = "No donations made yet. Support a campaign!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(donations) { donation ->
                        UserDonationCard(donation = donation)
                    }
                }
            }
        }
    }
}
