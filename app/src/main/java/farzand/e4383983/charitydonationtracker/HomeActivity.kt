package farzand.e4383983.charitydonationtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.* // For Material Design 3 components
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            HomeScreenDesign()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenDesign() {
    // Load your custom colors
    val primaryDark = colorResource(id = R.color.PrimaryDark)
    val buttonColor = colorResource(id = R.color.button_color)
    val textOnPrimaryDark = colorResource(id = R.color.text_on_primary_dark)
    val textOnButton = colorResource(id = R.color.text_on_button)

    // Create a scroll state for the column
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Welcome to Our Platform",
                        color = textOnPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryDark
                )
            )
        },
        containerColor = Color.White // A clean background for the main content
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp) // Apply horizontal padding here
                .verticalScroll(scrollState), // Apply verticalScroll modifier here
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between elements
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Top spacing

            // Header/Welcome Message
            Text(
                text = "Empowering Change, Together!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Donate Button (Prominent)
            Button(
                onClick = { /* Handle Donate click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email, // Changed icon to VolunteerActivism
                        contentDescription = "Donate Icon",
                        tint = textOnButton,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Donate Now",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textOnButton
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Spacing

            // Grid for other options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donations History Card
                OptionCard(
                    title = "Donations History",
                    icon = Icons.Default.Email, // Changed icon to History
                    iconTint = buttonColor,
                    onClick = { /* Handle Donations History click */ }
                )

                // Profile Card
                OptionCard(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    iconTint = buttonColor,
                    onClick = { /* Handle Profile click */ }
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Spacing

            // Ongoing Campaigns Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = primaryDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email, // Changed icon to Campaign
                            contentDescription = "Campaigns Icon",
                            tint = textOnPrimaryDark,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Ongoing Campaigns",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textOnPrimaryDark
                        )
                    }

                    // Example Campaign Item 1
                    CampaignItem(
                        title = "Support Local Schools",
                        description = "Help us provide essential supplies for underprivileged students.",
                        progress = 0.75f, // 75% complete
                        buttonColor = buttonColor
                    )

                    // Example Campaign Item 2
                    CampaignItem(
                        title = "Clean Water Initiative",
                        description = "Bringing clean and safe drinking water to rural communities.",
                        progress = 0.40f, // 40% complete
                        buttonColor = buttonColor
                    )

                    // Add more CampaignItem as needed
                    TextButton(onClick = { /* View all campaigns */ }) {
                        Text("View All Campaigns", color = buttonColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Add some space at the bottom for scrolling
        }
    }
}

@Composable
fun RowScope.OptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(140.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun CampaignItem(
    title: String,
    description: String,
    progress: Float,
    buttonColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = buttonColor,
            trackColor = buttonColor.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(progress * 100).toInt()}% Achieved",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreenDesign() {
    HomeScreenDesign()
}