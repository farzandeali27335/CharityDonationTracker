package farzand.e4383983.charitydonationtracker.donation

import kotlinx.coroutines.launch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.example.charitydonationtracker.ui.theme.CharityDonationTrackerTheme
import com.google.firebase.FirebaseApp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import farzand.e4383983.charitydonationtracker.ui.theme.CharityDonationTrackerTheme

// -------------------------------------------------------------------------------- //
// 1. Data Model
// Defines the structure for a Donation item.
// -------------------------------------------------------------------------------- //
data class Donation(
    val id: String = UUID.randomUUID().toString(),
    val donorName: String = "Anonymous",
    val amount: Double = 0.0,
    val donationType: String = "Education",
    val message: String = "",
    val timestamp: Date = Date()
)

// -------------------------------------------------------------------------------- //
// 2. FirebaseManager
// Handles all Firebase initialization, authentication, and Firestore operations.
// This centralizes Firebase logic and makes it reusable.
// -------------------------------------------------------------------------------- //
class FirebaseManager(private val activity: ComponentActivity) {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        // Initialize Firebase app if not already initialized
        if (FirebaseApp.getApps(activity).isEmpty()) {
            FirebaseApp.initializeApp(activity)
        }
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        signInAnonymously() // Sign in anonymously on startup
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    _userId.value = auth.currentUser?.uid
                    println("Firebase: Anonymous sign-in successful. User ID: ${_userId.value}")
                } else {
                    println("Firebase: Anonymous sign-in failed: ${task.exception?.message}")
                    // Fallback to a random UUID if anonymous sign-in fails
                    _userId.value = UUID.randomUUID().toString()
                }
            }
    }

    // Function to add a new donation to Firestore
    suspend fun addDonation(donation: Donation): Result<Unit> {
        val currentUserId = _userId.value
        if (currentUserId == null) {
            return Result.failure(IllegalStateException("User not authenticated."))
        }

        return try {
            // Firestore path for private user data: artifacts/{appId}/users/{userId}/donations
            // Note: __app_id is a placeholder for the Canvas environment.
            // For a standalone Android app, you might use a fixed string or retrieve it from resources.
            val appId = "default-app-id" // Replace with actual app ID if available
            val collectionPath = "artifacts/$appId/users/$currentUserId/donations"
            db.collection(collectionPath)
                .add(donation)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to get real-time updates of donations from Firestore
    fun getDonations(): Flow<List<Donation>> = flow {
        val currentUserId = _userId.value
        if (currentUserId == null) {
            emit(emptyList())
            return@flow
        }

        val appId = "default-app-id" // Replace with actual app ID if available
        val collectionPath = "artifacts/$appId/users/$currentUserId/donations"

        // Use a callbackFlow to bridge Firestore's snapshot listener with Kotlin Flow
        val snapshotListener = db.collection(collectionPath)
            .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Firebase: Listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val donations = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Donation>()?.copy(id = doc.id) // Map to Donation object, copy ID
                    }
                    emit(donations) // Emit the new list of donations
                } else {
                    emit(emptyList()) // Emit empty list if no data
                }
            }

        // Keep the flow alive as long as the collector is active
        awaitClose { snapshotListener.remove() }
    }
}

// -------------------------------------------------------------------------------- //
// 3. Screens (Composable Functions)
// These define the UI for each part of the application.
// -------------------------------------------------------------------------------- //

// Donate Now Screen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateNowScreen(
    firebaseManager: FirebaseManager,
    onDonationSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State for form inputs
    var donorName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var donationType by remember { mutableStateOf("Education") }
    var message by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Donate Now",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Donor Name Input
        OutlinedTextField(
            value = donorName,
            onValueChange = { donorName = it },
            label = { Text("Donor Name (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Donation Amount Input
        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                // Allow only numeric input
                if (newValue.all { it.isDigit() || it == '.' }) {
                    amount = newValue
                }
            },
            label = { Text("Donation Amount ($)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.foundation.text.KeyboardType.Number),
            singleLine = true
        )

        // Donation Type Dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = donationType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Donation Type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), // This makes the text field act as the anchor for the dropdown
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Education", "Health", "Environment", "Disaster Relief", "Animal Welfare", "Other").forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            donationType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        // Message Input
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
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
                    donationType = donationType,
                    message = message,
                    timestamp = Date() // Current date for timestamp
                )
                coroutineScope.launch {
                    val result = firebaseManager.addDonation(newDonation)
                    isSaving = false
                    if (result.isSuccess) {
                        Toast.makeText(context, "Donation successful!", Toast.LENGTH_SHORT).show()
                        // Clear form fields
                        donorName = ""
                        amount = ""
                        donationType = "Education"
                        message = ""
                        onDonationSuccess() // Notify parent of success
                    } else {
                        Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving, // Disable button while saving
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processing...")
            } else {
                Text("Submit Donation")
            }
        }
    }
}

// Donation History Screen Composable
@Composable
fun DonationHistoryScreen(firebaseManager: FirebaseManager) {
    // Collect the flow of donations from FirebaseManager as state
    val donations by firebaseManager.getDonations().collectAsState(initial = emptyList())
    val userId by firebaseManager.userId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Donation History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (userId != null) {
            Text(
                text = "Your User ID: $userId",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (donations.isEmpty()) {
            Text(
                text = "No donations made yet. Be the first!",
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
                    DonationCard(donation = donation)
                }
            }
        }
    }
}

// Composable for displaying a single donation item
@Composable
fun DonationCard(donation: Donation) {
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
                text = "$${String.format("%.2f", donation.amount)} - ${donation.donationType}",
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
                text = "Date: ${android.text.format.DateFormat.format("MMM dd, yyyy HH:mm", donation.timestamp)}",
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

// -------------------------------------------------------------------------------- //
// 4. Main Activity and Navigation
// Sets up the main activity, FirebaseManager, and Jetpack Compose Navigation.
// -------------------------------------------------------------------------------- //

class MainActivity : ComponentActivity() {
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseManager = FirebaseManager(this) // Initialize FirebaseManager

        setContent {
            CharityDonationTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(firebaseManager = firebaseManager)
                }
            }
        }
    }
}

// Defines the navigation routes
sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object DonateNow : Screen("donate_now", "Donate Now", { Icon(Icons.Default.Payments, contentDescription = "Donate") })
    object DonationHistory : Screen("donation_history", "History", { Icon(Icons.Default.History, contentDescription = "History") })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(firebaseManager: FirebaseManager) {
    val navController = rememberNavController()
    val items = listOf(Screen.DonateNow, Screen.DonationHistory)
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(screen.title) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.DonateNow.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.DonateNow.route) {
                DonateNowScreen(
                    firebaseManager = firebaseManager,
                    onDonationSuccess = {
                        // Navigate to history after successful donation
                        navController.navigate(Screen.DonationHistory.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        selectedItem = items.indexOf(Screen.DonationHistory)
                    }
                )
            }
            composable(Screen.DonationHistory.route) {
                DonationHistoryScreen(firebaseManager = firebaseManager)
            }
        }
    }
}

// -------------------------------------------------------------------------------- //
// 5. Preview (Optional)
// For Android Studio's design preview.
// -------------------------------------------------------------------------------- //
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CharityDonationTrackerTheme {
        // You would typically mock FirebaseManager for previews
        // For simplicity, this preview might not fully function without a real Firebase setup.
        // A better preview would involve creating a mock FirebaseManager.
        Text("Preview is not fully functional without Firebase setup. Run on device.")
    }
}