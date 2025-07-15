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
import farzand.e4383983.charitydonationtracker.ui.theme.CharityDonationTrackerTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CharityDonationTrackerTheme{
//                WelComeScreen()
                MyAppNavGraph()
            }
        }
    }
}

@Composable
fun MyAppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.Splash.route // Start with the splash screen
    ) {
        // 1. Splash Screen Destination
        composable(AppDestinations.Splash.route) {
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
            HomeScreenDesign() // Your previously designed home screen
            // If HomeScreen has navigation out, you'd pass navController to it
            // For example:
            // HomeScreenDesign(
            //     onNavigateToProfile = { navController.navigate(AppDestinations.Profile.route) },
            //     onNavigateToDonationsHistory = { navController.navigate(AppDestinations.DonationsHistory.route) },
            //     onNavigateToCampaigns = { navController.navigate(AppDestinations.Campaigns.route) }
            // )
        }

        // Add other destinations for your Home Screen options
        composable(AppDestinations.Profile.route) {
            // Your Profile Composable
            Text("Profile Screen") // Placeholder
        }
        composable(AppDestinations.DonationsHistory.route) {
            // Your Donations History Composable
            Text("Donations History Screen") // Placeholder
        }
        composable(AppDestinations.Campaigns.route) {
            // Your Campaigns Composable
            Text("Campaigns Screen") // Placeholder
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

    LaunchedEffect(Unit) {
        delay(3000) // Delay for 3 seconds
        // Navigate to the Login screen
        navController.navigate(AppDestinations.Login.route) {
            // This pops up to the start destination (Splash) and removes it
            popUpTo(AppDestinations.Splash.route) {
                inclusive = true
            }
        }
    }

    WelComeScreenDesign() // Your actual splash screen UI
}



@Composable
fun WelComeScreen() {
    var showSplash by remember { mutableStateOf(true) }

    val context = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        delay(3000)
        showSplash = false
    }

    if (showSplash) {
        WelComeScreenDesign()
    } else {
        context.startActivity(Intent(context, LoginActivity::class.java))
        context.finish()
    }

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
