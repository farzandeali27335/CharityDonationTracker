package farzand.e4383983.charitydonationtracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase
import farzand.e4383983.charitydonationtracker.data.AppDestinations
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.compose.ui.graphics.Color as ComposeColor


data class DonorData(
    var fullName: String = "",
    var emailid: String = "",
    var country: String = "",
    var password: String = "",
    var profileImage: String = ""
)

fun getImageUri(activityContext: Context): Uri {
    val file = File(activityContext.filesDir, "captured_image.jpg")
    return FileProvider.getUriForFile(
        activityContext,
        "${activityContext.packageName}.fileprovider",
        file
    )
}


object DonorPhoto {
    lateinit var selImageUri: Uri
    var isImageSelected = false
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    var errorMessage by remember { mutableStateOf("") }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val genderOptions = listOf("Male", "Female", "Other")
    var selectedGender by remember { mutableStateOf("Male") }

    val activityContext = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val captureImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = getImageUri(activityContext)
                DonorPhoto.selImageUri = imageUri as Uri
                DonorPhoto.isImageSelected = true
            } else {
                DonorPhoto.isImageSelected = false
                Toast.makeText(activityContext, "Capture Failed", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(activityContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                captureImageLauncher.launch(getImageUri(activityContext)) // Launch the camera
            } else {
                Toast.makeText(activityContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.PrimaryDark))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hey, Register Now!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ComposeColor.Black
            ), // Adjusted text color
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(ComposeColor.LightGray)
                .border(
                    2.dp,
                    ComposeColor(0xFF6200EE),
                    CircleShape
                )
                .align(Alignment.CenterHorizontally)
                .clickable {
                    if (ContextCompat.checkSelfPermission(
                            activityContext,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        captureImageLauncher.launch(getImageUri(activityContext))
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = if (imageUri != null) {
                    rememberAsyncImagePainter(model = imageUri)
                } else {
                    painterResource(id = R.drawable.iv_profile)
                },
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(140.dp)
                    .clickable {
                        if (ContextCompat.checkSelfPermission(
                                activityContext,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            captureImageLauncher.launch(getImageUri(activityContext))
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tap to add profile picture",
            style = MaterialTheme.typography.bodySmall,
            color = ComposeColor.Blue,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Enter Full Name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Full Name Icon",
                    tint = colorResource(id = R.color.button_color)
                )
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select Gender",
            style = MaterialTheme.typography.bodyMedium,
            color = ComposeColor.Black
        )

        Row(
            Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            genderOptions.forEach { gender ->
                RadioButton(
                    selected = (gender == selectedGender),
                    onClick = { selectedGender = gender },
                    colors = RadioButtonDefaults.colors(selectedColor = ComposeColor(0xFF6200EE))
                )
                Text(
                    text = gender,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 6.dp),
                    color = ComposeColor.Black
                )
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter E-Mail") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = colorResource(id = R.color.button_color)
                )
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon",
                    tint = colorResource(id = R.color.button_color)
                )
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon",
                    tint = colorResource(id = R.color.button_color)
                )
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(36.dp))
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = ComposeColor.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                when {
                    fullName.isBlank() -> {
                        errorMessage = "Please enter your full name."
                    }

                    !isValidUsername(fullName) -> {
                        errorMessage = "Please enter a valid full name (letters only)."
                    }

                    email.isBlank() -> {
                        errorMessage = "Please enter your email."
                    }

                    !isValidEmail(email) -> {
                        errorMessage = "Please enter a valid email address."
                    }

                    password.isBlank() -> {
                        errorMessage = "Please enter your password."
                    }

                    confirmPassword.isBlank() -> {
                        errorMessage = "Please confirm your password."
                    }

                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match"
                    }

                    else -> {
                        errorMessage = ""

                        if (DonorPhoto.isImageSelected) {
                            val inputStream =
                                activityContext.contentResolver.openInputStream(DonorPhoto.selImageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            val base64Image =
                                Base64.encodeToString(
                                    outputStream.toByteArray(),
                                    Base64.DEFAULT
                                )

                            val userData = DonorData(
                                fullName = fullName,
                                emailid = email,
                                country = "UK",
                                password = password,
                                profileImage = base64Image
                            )
                            registerUser(userData, activityContext, navController)
                        } else {
                            errorMessage = "Please upload profile photo"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 0.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.button_color)
            )
        ) {
            Text("Register", color = ComposeColor.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text(
                text = "I have an account / ",
                style = MaterialTheme.typography.bodyLarge,
                color = ComposeColor.Black
            )

            Text(
                text = "Login Now",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                color = ComposeColor.Black,
                modifier = Modifier.clickable {
                    navController.navigate(AppDestinations.Login.route) {
                        popUpTo(AppDestinations.Register.route) { inclusive = true }
                    }

                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}


fun isValidUsername(username: String): Boolean {
    return username.isNotBlank() && username.all { it.isLetter() || it.isWhitespace() }
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

fun registerUser(userData: DonorData, context: Context, navController: NavController) {
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val databaseReference = firebaseDatabase.getReference("DonorData")
    val sanitizedEmail = userData.emailid.replace(".", ",")

    databaseReference.child(sanitizedEmail)
        .setValue(userData)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "You Registered Successfully", Toast.LENGTH_SHORT).show()
                navController.navigate(AppDestinations.Login.route) {
                    popUpTo(AppDestinations.Register.route) { inclusive = true }
                }
            } else {
                Toast.makeText(
                    context,
                    "Registration Failed: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Something went wrong: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
