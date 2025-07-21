package farzand.e4383983.charitydonationtracker.donation

import java.util.UUID

class DonationSummary {

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
}