package farzand.e4383983.charitydonationtracker.donation

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import farzand.e4383983.charitydonationtracker.data.UserDetails
import farzand.e4383983.charitydonationtracker.ui.theme.CharityDonationTrackerTheme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID


// -------------------------------------------------------------------------------- //
// 1. Data Models
// Defines the structure for Campaign and Donation items.
// All fields must be public 'var' and have default values for Realtime DB deserialization.
// -------------------------------------------------------------------------------- //

data class Campaign(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var description: String = "",
    var category: String = "General",
    var goalAmount: Double = 0.0,
    var raisedAmount: Double = 0.0,
    var imageUrl: String = "https://www.usatoday.com/gcdn/-mm-/3b8b0abcb585d9841e5193c3d072eed1e5ce62bc/c=0-30-580-356/local/-/media/2017/10/05/USATODAY/usatsports/glass-jar-full-of-cois-with-donate-written-on-it-charity-donation-philanthropy_large.jpg?width=1200&disable=upscale&format=pjpg&auto=webp", // Placeholder image
    var timestamp: Long = System.currentTimeMillis() // For ordering
)

data class Donation(
    var id: String = UUID.randomUUID().toString(),
    var donorName: String = "Anonymous",
    var amount: Double = 0.0,
    var message: String = "",
    var timestamp: Long = System.currentTimeMillis(), // Use Long for Realtime DB timestamps
    var campaignId: String = "", // Link to the campaign it's for
    var userId: String = "" // User who made the donation
)

// -------------------------------------------------------------------------------- //
// 2. FirebaseManager
// Handles all Firebase initialization and Realtime Database operations.
// No Firebase Auth or Firestore in this simplified version.
// -------------------------------------------------------------------------------- //

class FirebaseManager(private val activity: ComponentActivity,private val context: Context) {
    private lateinit var realtimeDb: DatabaseReference

    // A simple UUID to identify the user session (not persistent across app reinstalls)
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        // Initialize Firebase app if not already initialized
        if (FirebaseApp.getApps(activity).isEmpty()) {
            FirebaseApp.initializeApp(activity)
        }
        realtimeDb = FirebaseDatabase.getInstance().reference

        // Generate a simple session ID for the user
        _userId.value = "rajesh@gmail,com"
//        _userId.value = UserDetails.getEmail(context = context).replace(".",",")
        println("Firebase: Session User ID: ${_userId.value}")
    }

    // --- Realtime Database Operations (for Campaigns) ---

    // Function to add sample campaigns (for initial data)
    suspend fun addSampleCampaigns() {
        val campaignsRef = realtimeDb.child("campaigns")
        val sampleCampaigns = listOf(
            Campaign(
                id = "campaign1",
                name = "Educate Underprivileged Children",
                description = "Help provide quality education to children in remote areas.",
                category = "Education",
                goalAmount = 10000.0,
                raisedAmount = 3500.0,
                imageUrl = "https://www.usatoday.com/gcdn/-mm-/3b8b0abcb585d9841e5193c3d072eed1e5ce62bc/c=0-30-580-356/local/-/media/2017/10/05/USATODAY/usatsports/glass-jar-full-of-cois-with-donate-written-on-it-charity-donation-philanthropy_large.jpg?width=1200&disable=upscale&format=pjpg&auto=webp"
            ),
            Campaign(
                id = "campaign2",
                name = "Clean Water for All",
                description = "Fund projects to bring clean and safe drinking water to communities.",
                category = "Environment",
                goalAmount = 15000.0,
                raisedAmount = 7200.0,
                imageUrl = "https://www.usatoday.com/gcdn/-mm-/3b8b0abcb585d9841e5193c3d072eed1e5ce62bc/c=0-30-580-356/local/-/media/2017/10/05/USATODAY/usatsports/glass-jar-full-of-cois-with-donate-written-on-it-charity-donation-philanthropy_large.jpg?width=1200&disable=upscale&format=pjpg&auto=webp"
            ),
            Campaign(
                id = "campaign3",
                name = "Support Animal Shelters",
                description = "Provide food, shelter, and medical care for abandoned animals.",
                category = "Animal Welfare",
                goalAmount = 5000.0,
                raisedAmount = 2100.0,
                imageUrl = "https://www.usatoday.com/gcdn/-mm-/3b8b0abcb585d9841e5193c3d072eed1e5ce62bc/c=0-30-580-356/local/-/media/2017/10/05/USATODAY/usatsports/glass-jar-full-of-cois-with-donate-written-on-it-charity-donation-philanthropy_large.jpg?width=1200&disable=upscale&format=pjpg&auto=webp"
            ),
            Campaign(
                id = "campaign4",
                name = "Medical Aid for Remote Villages",
                description = "Deliver essential medical supplies and services to underserved populations.",
                category = "Health",
                goalAmount = 12000.0,
                raisedAmount = 9800.0,
                imageUrl = "https://www.usatoday.com/gcdn/-mm-/3b8b0abcb585d9841e5193c3d072eed1e5ce62bc/c=0-30-580-356/local/-/media/2017/10/05/USATODAY/usatsports/glass-jar-full-of-cois-with-donate-written-on-it-charity-donation-philanthropy_large.jpg?width=1200&disable=upscale&format=pjpg&auto=webp"
            ),
            Campaign(
                id = "campaign5",
                name = "Disaster Relief Fund",
                description = "Provide immediate assistance to victims of natural disasters.",
                category = "Disaster Relief",
                goalAmount = 20000.0,
                raisedAmount = 1500.0,
                imageUrl = "https://www.usatoday.com/gcdn/-mm-/3b8b0abcb585d9841e5193c3d072eed1e5ce62bc/c=0-30-580-356/local/-/media/2017/10/05/USATODAY/usatsports/glass-jar-full-of-cois-with-donate-written-on-it-charity-donation-philanthropy_large.jpg?width=1200&disable=upscale&format=pjpg&auto=webp"
            )
        )

        sampleCampaigns.forEach { campaign ->
            try {
                // Check if campaign already exists before adding
                val snapshot = campaignsRef.child(campaign.id).get().await()
                if (!snapshot.exists()) {
                    campaignsRef.child(campaign.id).setValue(campaign).await()
                    println("Firebase: Added sample campaign: ${campaign.name}")
                } else {
                    println("Firebase: Campaign ${campaign.name} already exists, skipping.")
                }
            } catch (e: Exception) {
                println("Firebase: Error adding sample campaign ${campaign.name}: ${e.message}")
            }
        }
    }

    // Get all campaigns from Realtime Database, optionally filtered by category
    fun getCampaigns(category: String? = null): Flow<List<Campaign>> = callbackFlow {
        val campaignsRef = realtimeDb.child("campaigns")
        var query = campaignsRef.orderByChild("timestamp") // Order by timestamp

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allCampaigns = snapshot.children.mapNotNull { childSnapshot ->
                    childSnapshot.getValue(Campaign::class.java)
                }.sortedByDescending { it.timestamp } // Sort descending after fetching

                val filteredCampaigns = if (category != null && category != "All") {
                    allCampaigns.filter { it.category == category }
                } else {
                    allCampaigns
                }
                trySend(filteredCampaigns).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase: Listen failed for campaigns: ${error.message}")
                close(error.toException())
            }
        }

        query.addValueEventListener(valueEventListener)
        awaitClose { query.removeEventListener(valueEventListener) }
    }

    // Get a single campaign's details from Realtime Database
    fun getCampaignDetails(campaignId: String): Flow<Campaign?> = callbackFlow {
        val campaignRef = realtimeDb.child("campaigns").child(campaignId)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val campaign = snapshot.getValue(Campaign::class.java)
                trySend(campaign).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase: Listen failed for campaign details: ${error.message}")
                close(error.toException())
            }
        }

        campaignRef.addValueEventListener(valueEventListener)
        awaitClose { campaignRef.removeEventListener(valueEventListener) }
    }

    // --- Realtime Database Operations (for Donations) ---

    // Function to add a new donation to Realtime Database
    suspend fun addRealtimeDonation(donation: Donation): Result<Unit> {
//        val currentUserId = _userId.value
        val currentUserId = UserDetails.getEmail(context = context)!!.replace(".",",")
        if (currentUserId == null) {
            return Result.failure(IllegalStateException("User session not initialized."))
        }

        return try {
            // Store donation under campaign-specific path
            val campaignDonationRef = realtimeDb.child("donations").child(donation.campaignId).push()
            // Store donation under user-specific path for history
            val userDonationRef = realtimeDb.child("users").child(currentUserId).child("donations").push()

            val donationWithId = donation.copy(
                id = campaignDonationRef.key ?: UUID.randomUUID().toString(),
                userId = currentUserId
            )

            campaignDonationRef.setValue(donationWithId).await()
            userDonationRef.setValue(donationWithId).await()

            // Update raised amount in Realtime Database for the campaign
            val campaignRef = realtimeDb.child("campaigns").child(donation.campaignId)
            val currentCampaignSnapshot = campaignRef.get().await()
            val currentCampaign = currentCampaignSnapshot.getValue(Campaign::class.java)

            if (currentCampaign != null) {
                val newRaisedAmount = currentCampaign.raisedAmount + donation.amount
                campaignRef.child("raisedAmount").setValue(newRaisedAmount).await()
            } else {
                println("Firebase: Campaign not found for updating raised amount: ${donation.campaignId}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to get real-time updates of donations made by the current user
    fun getUserDonations(): Flow<List<Donation>> = callbackFlow {
//        val currentUserId = _userId.value
        val currentUserId = UserDetails.getEmail(context = context)!!.replace(".",",")

        if (currentUserId == null) {
            trySend(emptyList()).isSuccess
            close(IllegalStateException("User session not initialized."))
            return@callbackFlow
        }

        val userDonationsRef = realtimeDb.child("users").child(currentUserId).child("donations")

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val donations = snapshot.children.mapNotNull { childSnapshot ->
                    childSnapshot.getValue(Donation::class.java)
                }.sortedByDescending { it.timestamp }
                trySend(donations).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase: Listen failed for user donations: ${error.message}")
                close(error.toException())
            }
        }

        userDonationsRef.addValueEventListener(valueEventListener)
        awaitClose { userDonationsRef.removeEventListener(valueEventListener) }
    }

    fun getAllDonations(): Flow<List<Donation>> = callbackFlow {
        val allDonationsRef = realtimeDb.child("donations") // Root of all donations by campaign


        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allDonations = mutableListOf<Donation>()
                // Iterate through each campaign's donations
                snapshot.children.forEach { campaignSnapshot ->
                    campaignSnapshot.children.mapNotNullTo(allDonations) { donationSnapshot ->
                        donationSnapshot.getValue(Donation::class.java)
                    }
                }
                trySend(allDonations).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase: Listen failed for all donations: ${error.message}")
                close(error.toException())
            }
        }

        allDonationsRef.addValueEventListener(valueEventListener)
        awaitClose { allDonationsRef.removeEventListener(valueEventListener) }
    }
}

// -------------------------------------------------------------------------------- //
// 3. Screens (Composable Functions)
// These define the UI for each part of the application.
// -------------------------------------------------------------------------------- //

// Campaign List Screen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignListScreen(
    navController: NavHostController,
    firebaseManager: FirebaseManager
) {
    val categories = listOf("All", "Education", "Health", "Environment", "Animal Welfare", "Disaster Relief", "Other")
    var selectedCategory by remember { mutableStateOf("All") }

    // Collect campaigns based on selected category
    val campaigns by firebaseManager.getCampaigns(selectedCategory).collectAsState(initial = emptyList())

    // Add sample campaigns once on app start (or when this screen is first composed)
    LaunchedEffect(Unit) {
        firebaseManager.addSampleCampaigns()
    }


    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "OnGoing Campaigns",
                navController = navController, // Not used for back, but required by composable
                showBackButton = true // No back button on main tab screens
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
//            Text(
//                text = "Browse Campaigns",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Tabs
            // REMOVED Modifier.horizontalScroll() from ScrollableTabRow
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                modifier = Modifier.fillMaxWidth(), // Removed .horizontalScroll()
                edgePadding = 0.dp
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (campaigns.isEmpty()) {
                Text(
                    text = "No campaigns found for this category.",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(campaigns) { campaign ->
                        CampaignCard(campaign = campaign) {
                            navController.navigate(Screen.CampaignDetail.createRoute(campaign.id))

//                        navController.navigate("campaign_detail/${campaign.id}")
                        }
                    }
                }
            }
        }
    }
}

// Composable for displaying a single campaign card in the list
@Composable
fun CampaignCard(campaign: Campaign, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = campaign.imageUrl,
                contentDescription = "${campaign.name} image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = campaign.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = campaign.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (campaign.raisedAmount / campaign.goalAmount).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Raised: $${String.format("%.2f", campaign.raisedAmount)} / Goal: $${String.format("%.2f", campaign.goalAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Campaign Detail Screen Composable
@Composable
fun CampaignDetailScreen(
    navController: NavHostController,
    firebaseManager: FirebaseManager,
    campaignId: String
) {
    val campaign by firebaseManager.getCampaignDetails(campaignId).collectAsState(initial = null)
    var showDonationPopup by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Campaign Details",
                navController = navController, // Not used for back, but required by composable
                showBackButton = true // No back button on main tab screens
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Enable scrolling for long descriptions
        ) {
            campaign?.let { camp ->
                AsyncImage(
                    model = camp.imageUrl,
                    contentDescription = "${camp.name} image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = camp.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Category: ${camp.category}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = camp.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = (camp.raisedAmount / camp.goalAmount).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Raised: $${
                        String.format(
                            "%.2f",
                            camp.raisedAmount
                        )
                    } / Goal: $${String.format("%.2f", camp.goalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showDonationPopup = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Donate")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Donate Now", style = MaterialTheme.typography.titleMedium)
                }

                if (showDonationPopup) {
                    DonationPopup(
                        campaignId = camp.id,
                        firebaseManager = firebaseManager,
                        onDismiss = { showDonationPopup = false },
                        onDonationSuccess = {
                            showDonationPopup = false
                            Toast.makeText(context, "Donation successful!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    )
                }
            } ?: run {
                // Loading or not found state
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading campaign details...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Donation Popup Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationPopup(
    campaignId: String,
    firebaseManager: FirebaseManager,
    onDismiss: () -> Unit,
    onDonationSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var donorName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make a Donation", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = donorName,
                    onValueChange = { donorName = it },
                    label = { Text("Your Name (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Allow only numeric input and a single decimal point
                        if (newValue.all { it.isDigit() || it == '.' } && newValue.count { it == '.' } <= 1) {
                            amount = newValue
                        }
                    },
                    label = { Text("Donation Amount ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
                        Toast.makeText(context, "Please enter a valid donation amount.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    val newDonation = Donation(
                        donorName = donorName.ifEmpty { "Anonymous" },
                        amount = amount.toDouble(),
                        message = message,
                        timestamp = System.currentTimeMillis(),
                        campaignId = campaignId,
                        userId = firebaseManager.userId.value ?: UUID.randomUUID().toString() // Ensure userId is captured
                    )
                    coroutineScope.launch {
                        val result = firebaseManager.addRealtimeDonation(newDonation)
                        isSaving = false
                        if (result.isSuccess) {
                            onDonationSuccess()
                        } else {
                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Donating...")
                } else {
                    Text("Submit Donation")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


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
                navController = navController, // Not used for back, but required by composable
                showBackButton = true // No back button on main tab screens
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
//            Text(
//                text = "Your Donation History",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            if (userId != null) {
//                Text(
//                    text = "Your User ID: $userId",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Gray,
//                    modifier = Modifier.fillMaxWidth(),
//                    textAlign = TextAlign.Center
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//            }

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

// Composable for displaying a single donation item in user history
@Composable
fun UserDonationCard(donation: Donation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "$${String.format("%.2f", donation.amount)} to Campaign: ${donation.campaignId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Donor: ${donation.donorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: ${android.text.format.DateFormat.format("MMM dd, yyyy HH:mm", Date(donation.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (donation.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Message: \"${donation.message}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// NEW: Reusable TopAppBar Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    navController: NavHostController,
    showBackButton: Boolean = false
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// NEW: Donation Summary Screen Composable
// Donation Summary Screen Composable
// NEW: Donation Summary Screen Composable
@Composable
fun DonationSummaryScreen(firebaseManager: FirebaseManager,navController: NavHostController) {
    // Collect ONLY the current user's donations
    val userDonations by firebaseManager.getUserDonations().collectAsState(initial = emptyList())

    // Collect all campaigns to get the category mapping (still needed to map campaignId to category)
    val campaignsMap by firebaseManager.getCampaigns().collectAsState(initial = emptyList())
    val campaignIdToCategoryMap = remember(campaignsMap) {
        campaignsMap.associate { it.id to it.category }
    }

    // Aggregate user's donations by category
    val summaryByCategoryActual = remember(userDonations, campaignIdToCategoryMap) {
        userDonations
            .groupBy { donation ->
                // Look up the category using the campaignId from the map
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
                title = "Your Donation Summary", // Title reflects user-specific summary
                navController = navController, // Not used for back, but required by composable
                showBackButton = true // No back button on main tab screens
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply innerPadding from Scaffold
                .padding(horizontal = 16.dp) // Add horizontal padding for content
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Adjust spacing after app bar

            if (userDonations.isEmpty()) { // Check userDonations, not allDonations
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

                // Optionally, display the list summary below the chart
                Text(
                    text = "Detailed Breakdown:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f), // Take remaining space
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

// Composable for displaying a single summary item (can be used below the chart)
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

// NEW: Composable for displaying the MPAndroidChart Pie Chart
@Composable
fun DonationPieChart(summaryData: List<Pair<String, Double>>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Fixed height for the chart
            .padding(8.dp),
        factory = { ctx ->
            // Create a new PieChart instance
            PieChart(ctx).apply {
                description.isEnabled = false // Disable description label
                setUsePercentValues(true) // Show values as percentages
                setEntryLabelColor(android.graphics.Color.BLACK) // Corrected: Explicitly use android.graphics.Color.BLACK
                setEntryLabelTextSize(12f) // Label text size
                holeRadius = 58f // Size of the hole in the middle
                transparentCircleRadius = 61f // Size of the transparent circle
                animateY(1400) // Animation duration
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.BLACK) // Hole color

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
            // Update chart data when `summaryData` changes
            val entries = summaryData.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }

            val dataSet = PieDataSet(entries, "Donation Categories").apply {
                // Define colors for the slices
                colors = ColorTemplate.VORDIPLOM_COLORS.toList() +
                        ColorTemplate.JOYFUL_COLORS.toList() +
                        ColorTemplate.COLORFUL_COLORS.toList() +
                        ColorTemplate.LIBERTY_COLORS.toList() +
                        ColorTemplate.PASTEL_COLORS.toList()

                sliceSpace = 2f // Space between slices
                selectionShift = 5f // How much a slice moves when selected
                valueTextColor = android.graphics.Color.BLACK // Value text color
                valueTextSize = 14f // Value text size
            }

            val data = PieData(dataSet)
            pieChart.data = data
            pieChart.invalidate() // Refresh the chart
        }
    )
}


// -------------------------------------------------------------------------------- //
// 4. Main Activity and Navigation
// Sets up the main activity, FirebaseManager, and Jetpack Compose Navigation.
// -------------------------------------------------------------------------------- //

//class MainActivity : ComponentActivity() {
//    private lateinit var firebaseManager: FirebaseManager
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        firebaseManager = FirebaseManager(this) // Initialize FirebaseManager
//
//        setContent {
//            CharityDonationTrackerTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    // Use MyAppNavGraph as the root navigation
//                    MyAppNavGraph(firebaseManager = firebaseManager)
//                }
//            }
//        }
//    }
//}

// Defines the navigation routes for the main app flow


// Defines the navigation routes specific to the charity features
sealed class Screen(val route: String) {
    object CampaignList : Screen("campaign_list_route")
    object CampaignDetail : Screen("campaign_detail_route/{campaignId}") {
        fun createRoute(campaignId: String) = "campaign_detail_route/$campaignId"
    }
    object DonationHistory : Screen("donation_history_route")

    object DonationSummary : Screen("donation_summary_route")

}



// -------------------------------------------------------------------------------- //
// 5. Preview (Optional)
// For Android Studio's design preview.
// -------------------------------------------------------------------------------- //
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CharityDonationTrackerTheme {
        // For preview, you might need a mock FirebaseManager or simplify the UI
        // to avoid direct Firebase calls.
        Text("Run on device for full functionality with Firebase.", modifier = Modifier.padding(16.dp))
    }
}
