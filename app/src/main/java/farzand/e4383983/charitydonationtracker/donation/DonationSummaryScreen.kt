package farzand.e4383983.charitydonationtracker.donation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun DonationSummaryScreen(firebaseManager: FirebaseManager,navController: NavHostController) {
    val userDonations by firebaseManager.getUserDonations().collectAsState(initial = emptyList())

    val campaignsMap by firebaseManager.getCampaigns().collectAsState(initial = emptyList())
    val campaignIdToCategoryMap = remember(campaignsMap) {
        campaignsMap.associate { it.id to it.category }
    }

    val summaryByCategoryActual = remember(userDonations, campaignIdToCategoryMap) {
        userDonations
            .groupBy { donation ->
                campaignIdToCategoryMap[donation.campaignId] ?: "Unknown Category"
            }
            .mapValues { (_, donationsInGroup) ->
                donationsInGroup.sumOf { it.amount }
            }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Your Donation Summary",
                navController = navController,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (userDonations.isEmpty()) {
                Text(
                    text = "You haven't made any donations yet for summary.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            } else {
                // Display the Pie Chart
                DonationPieChart(summaryData = summaryByCategoryActual)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Detailed Breakdown:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(summaryByCategoryActual) { (category, totalAmount) ->
                        SummaryCard(category = category, totalAmount = totalAmount)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(category: String, totalAmount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$${String.format("%.2f", totalAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 20.sp // Make amount stand out
            )
        }
    }
}

@Composable
fun DonationPieChart(summaryData: List<Pair<String, Double>>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp),
        factory = { ctx ->
            // Create a new PieChart instance
            PieChart(ctx).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setEntryLabelColor(android.graphics.Color.BLACK)
                setEntryLabelTextSize(12f)
                holeRadius = 58f
                transparentCircleRadius = 61f
                animateY(1400)
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.BLACK)

                // Legend setup
                legend.apply {
                    isEnabled = true
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.LEFT
                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                    setDrawInside(false)
                    xEntrySpace = 7f
                    yEntrySpace = 0f
                    yOffset = 0f
                }
            }
        },
        update = { pieChart ->
            val entries = summaryData.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }

            val dataSet = PieDataSet(entries, "Donation Categories").apply {
                colors = ColorTemplate.VORDIPLOM_COLORS.toList() +
                        ColorTemplate.JOYFUL_COLORS.toList() +
                        ColorTemplate.COLORFUL_COLORS.toList() +
                        ColorTemplate.LIBERTY_COLORS.toList() +
                        ColorTemplate.PASTEL_COLORS.toList()

                sliceSpace = 2f
                selectionShift = 5f
                valueTextColor = android.graphics.Color.BLACK
                valueTextSize = 14f
            }

            val data = PieData(dataSet)
            pieChart.data = data
            pieChart.invalidate()
        }
    )
}