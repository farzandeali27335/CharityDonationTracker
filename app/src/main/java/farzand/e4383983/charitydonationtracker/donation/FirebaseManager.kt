package farzand.e4383983.charitydonationtracker.donation

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import farzand.e4383983.charitydonationtracker.data.DonorAccountData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseManager(private val activity: ComponentActivity, private val context: Context) {
    private var realtimeDb: DatabaseReference

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        if (FirebaseApp.getApps(activity).isEmpty()) {
            FirebaseApp.initializeApp(activity)
        }
        realtimeDb = FirebaseDatabase.getInstance().reference

        _userId.value="user@gmail,com"
//        _userId.value = DonorAccountData.getDonorEmail(context = context)!!.replace(".",",")
        println("Firebase: Session User ID: ${_userId.value}")
    }


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
                imageUrl = "https://www.cypnow.co.uk/media/qlxcmm1h/classroomtabletsmonkeybusinessadobestock-350x250.jpg?width=1002&height=668&bgcolor=White&v=1db02ad8b7d3d00"
            ),
            Campaign(
                id = "campaign2",
                name = "Clean Water for All",
                description = "Fund projects to bring clean and safe drinking water to communities.",
                category = "Environment",
                goalAmount = 15000.0,
                raisedAmount = 7200.0,
                imageUrl = "https://dva1blx501zrw.cloudfront.net/uploaded_images/us/images/2222/original/shutterstock_2153548903.jpg"
            ),
            Campaign(
                id = "campaign3",
                name = "Support Animal Shelters",
                description = "Provide food, shelter, and medical care for abandoned animals.",
                category = "Animal Welfare",
                goalAmount = 5000.0,
                raisedAmount = 2100.0,
                imageUrl = "https://pets24.co.za/wp-content/uploads/2023/11/animal-shelter-adoption-and-dog-with-a-black-coup-2023-01-04-20-08-33-utc-scaled.webp"
            ),
            Campaign(
                id = "campaign4",
                name = "Medical Aid for Remote Villages",
                description = "Deliver essential medical supplies and services to underserved populations.",
                category = "Health",
                goalAmount = 12000.0,
                raisedAmount = 9800.0,
                imageUrl =  "https://media.path.org/images/Zambia_08.18_CommunityEngagement.2e16d0ba.fill-490x333.jpg"
            ),
            Campaign(
                id = "campaign5",
                name = "Disaster Relief Fund",
                description = "Provide immediate assistance to victims of natural disasters.",
                category = "Disaster Relief",
                goalAmount = 20000.0,
                raisedAmount = 1500.0,
                imageUrl = "https://bsmedia.business-standard.com/_media/bs/img/article/2023-07/12/full/1689161121-0331.jpg?im=FeatureCrop,size=(826,465)"
            )
        )

        sampleCampaigns.forEach { campaign ->
            try {
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


    suspend fun addRealtimeDonation(donation: Donation): Result<Unit> {
        val currentUserId = DonorAccountData.getDonorEmail(context = context)!!.replace(".",",")

        return try {
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

    fun getUserDonations(): Flow<List<Donation>> = callbackFlow {
        val currentUserId = DonorAccountData.getDonorEmail(context = context)!!.replace(".",",")

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
        val allDonationsRef = realtimeDb.child("donations")


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

    suspend fun updateUserProfilePicture(userEmail: String, base64Image: String?): Result<Unit> {
        return try {
            val sanitizedEmail = userEmail.replace(".", ",")
            val userRef = realtimeDb.child("UserData").child(sanitizedEmail)
            userRef.child("profilePictureBase64").setValue(base64Image).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error updating profile picture for $userEmail: ${e.message}")
            Result.failure(e)
        }
    }
}
