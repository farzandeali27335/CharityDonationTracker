package farzand.e4383983.charitydonationtracker.donation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import farzand.e4383983.charitydonationtracker.data.AppDestinations
import farzand.e4383983.charitydonationtracker.data.DonorAccountData
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import androidx.compose.ui.graphics.Color as ComposeColor


@Composable
fun ProfileScreen(navController: NavHostController, firebaseManager: FirebaseManager) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Retrieve saved user details from local storage
    val userName by remember {
        mutableStateOf(
            DonorAccountData.getDonorName(context) ?: "Guest User"
        )
    }
    val userEmail by remember {
        mutableStateOf(
            DonorAccountData.getDonorEmail(context) ?: "guest@example.com"
        )
    }
    var profilePicBase64 by remember {
        mutableStateOf(
            DonorAccountData.getDonorProfilePictureBase64(
                context
            )
        )
    } // Make it 'var' to update

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val newBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)

            profilePicBase64 = newBase64
            DonorAccountData.saveDonorProfilePictureBase64(context, newBase64)

            // Update in Firebase Realtime Database
            coroutineScope.launch {
                val result = firebaseManager.updateUserProfilePicture(userEmail, newBase64)
                if (result.isSuccess) {
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to update picture on server.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "ProfileScreen",
                        "Failed to update profile picture: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        } else {
            Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, launch camera
            cameraLauncher.launch(null)
        } else {
            // Permission denied
            Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }



    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "My Profile",
                navController = navController,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(ComposeColor.LightGray)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (profilePicBase64 != null && profilePicBase64!!.isNotEmpty()) {
                    val decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    AsyncImage(
                        model = bitmap,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback icon if no image is set
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Add/Update Profile Picture",
                        modifier = Modifier.size(100.dp),
                        tint = ComposeColor.DarkGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to update profile picture",
                style = MaterialTheme.typography.bodySmall,
                color = ComposeColor.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Username
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = {
                    DonorAccountData.clearDonorDetails(context)
                    navController.navigate(AppDestinations.Login.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                    Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    "Logout",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}