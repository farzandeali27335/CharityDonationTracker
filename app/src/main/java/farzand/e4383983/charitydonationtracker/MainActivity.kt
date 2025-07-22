package farzand.e4383983.charitydonationtracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import farzand.e4383983.charitydonationtracker.data.AppDestinations
import farzand.e4383983.charitydonationtracker.data.UserDetails
import farzand.e4383983.charitydonationtracker.donation.CampaignDetailScreen
import farzand.e4383983.charitydonationtracker.donation.CampaignListScreen
import farzand.e4383983.charitydonationtracker.donation.DonationHistoryScreen
import farzand.e4383983.charitydonationtracker.donation.DonationSummaryScreen
import farzand.e4383983.charitydonationtracker.donation.FirebaseManager
import farzand.e4383983.charitydonationtracker.donation.Screen
import farzand.e4383983.charitydonationtracker.ui.theme.CharityDonationTrackerTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseManager = FirebaseManager(this,this) // Initialize FirebaseManager

        enableEdgeToEdge()
        setContent {
            CharityDonationTrackerTheme{
//                WelComeScreen()
                MyAppNavGraph(firebaseManager)
            }
        }
    }
}

@Composable
fun MyAppNavGraph(firebaseManager: FirebaseManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.Splash.route // Start with the splash screen
    ) {
        // 1. Splash Screen Destination
        composable(AppDestinations.Splash.route) {
            // SplashScreen needs navController to navigate to the next screen
            SplashScreen(navController = navController)
        }

        // 2. Login Screen Destination
        composable(AppDestinations.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to Home and clear the back stack up to Login (and including it)
                    navController.navigate(AppDestinations.Home.route) {
                        popUpTo(AppDestinations.Login.route) {
                            inclusive = true // Remove Login from back stack
                        }
                    }
                }
            )
        }

        // 3. Home Screen Destination
        composable(AppDestinations.Home.route) {
            HomeScreenDesign(onButtonClicked = { buttonId ->
                when(buttonId)
                {
                    1 -> { // Assuming 1 maps to "Campaign List"
                        navController.navigate(Screen.CampaignList.route)
                    }
                    // Add more cases for other buttons if HomeScreenDesign has them
                    // For example, if button 2 is for Donation History:
                     2 -> {
                         navController.navigate(Screen.DonationHistory.route)
                     }

                    3->{
                        navController.navigate(Screen.DonationSummary.route)
                    }
                }
            })
        }

        // 4. Campaign List Screen Destination
        composable(Screen.CampaignList.route) {
            CampaignListScreen(navController = navController, firebaseManager = firebaseManager)
        }

        // 5. Campaign Detail Screen Destination
        composable(
            route = Screen.CampaignDetail.route, // Use the route with placeholder
            arguments = listOf(androidx.navigation.navArgument("campaignId") {
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId")
            if (campaignId != null) {
                CampaignDetailScreen(
                    navController = navController,
                    firebaseManager = firebaseManager,
                    campaignId = campaignId
                )
            } else {
                // Handle error or navigate back if campaignId is missing
                Text(
                    "Error: Campaign ID missing",
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 6. Donation History Screen Destination
        composable(Screen.DonationHistory.route) {
            DonationHistoryScreen(firebaseManager = firebaseManager,navController)
        }

        composable(Screen.DonationSummary.route) {
            DonationSummaryScreen(firebaseManager = firebaseManager,navController)
        }

    }
}

/**
 * WelComeScreen is now refactored as SplashScreen.
 * It's responsible for displaying the splash and then navigating.
 */
@Composable
fun SplashScreen(navController: NavController) {
    // No need for mutableStateOf 'showSplash' anymore, LaunchedEffect directly navigates
    // No need for LocalContext as Activity for navigation

    val context = LocalContext.current as Activity


    LaunchedEffect(Unit) {
        delay(3000) // Delay for 3 seconds
        // Navigate to the Login screen

        val UserStatus = UserDetails.getUserLoginStatus(context)

        if (UserStatus) {
            navController.navigate(AppDestinations.Home.route) {
                // This pops up to the start destination (Splash) and removes it
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate(AppDestinations.Login.route) {
                // This pops up to the start destination (Splash) and removes it
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        }


    }

    WelComeScreenDesign() // Your actual splash screen UI
}




@Composable
fun WelComeScreenDesign() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.PrimaryDark)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {



            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Charity Donation Tracker",
            )

            Text(
                text = "Charity Donation Tracker",
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = " By Farzand e ali",
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CharityDonationTrackerTheme {
    }
}
