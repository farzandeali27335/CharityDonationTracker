package farzand.e4383983.charitydonationtracker

import android.app.Activity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import farzand.e4383983.charitydonationtracker.data.DonorAccountData
import farzand.e4383983.charitydonationtracker.donation.AboutUsScreen
import farzand.e4383983.charitydonationtracker.donation.CampaignDetailScreen
import farzand.e4383983.charitydonationtracker.donation.CampaignListScreen
import farzand.e4383983.charitydonationtracker.donation.DonationHistoryScreen
import farzand.e4383983.charitydonationtracker.donation.DonationSummaryScreen
import farzand.e4383983.charitydonationtracker.donation.FirebaseManager
import farzand.e4383983.charitydonationtracker.donation.ProfileScreen
import farzand.e4383983.charitydonationtracker.ui.theme.CharityDonationTrackerTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseManager = FirebaseManager(this, this)

        enableEdgeToEdge()
        setContent {
            CharityDonationTrackerTheme {
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
        startDestination = AppDestinations.Splash.route
    ) {
        composable(AppDestinations.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(AppDestinations.Login.route) {
            LoginScreen(navController,
                onLoginSuccess = {
                    navController.navigate(AppDestinations.Home.route) {
                        popUpTo(AppDestinations.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(AppDestinations.Register.route) {
            RegisterScreen(navController)
        }

        composable(AppDestinations.Home.route) {
            HomeScreenDesign(onButtonClicked = { buttonId ->
                when (buttonId) {
                    1 -> {
                        navController.navigate(Screen.CampaignList.route)
                    }
                    2 -> {
                        navController.navigate(Screen.DonationHistory.route)
                    }

                    3 -> {
                        navController.navigate(Screen.DonationSummary.route)
                    }

                    4 -> {
                        navController.navigate(Screen.AboutUs.route)
                    }

                    5 -> {
                        navController.navigate(Screen.Profile.route)

                    }
                }
            })
        }

        composable(Screen.CampaignList.route) {
            CampaignListScreen(navController = navController, firebaseManager = firebaseManager)
        }

        composable(
            route = Screen.CampaignDetail.route,
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
                Text(
                    "Error: Campaign ID missing",
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center
                )
            }
        }

        composable(Screen.DonationHistory.route) {
            DonationHistoryScreen(firebaseManager = firebaseManager, navController)
        }

        composable(Screen.DonationSummary.route) {
            DonationSummaryScreen(firebaseManager = firebaseManager, navController)
        }

        composable(Screen.AboutUs.route) {
            AboutUsScreen(navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController, firebaseManager)
        }

    }
}


@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current as Activity


    LaunchedEffect(Unit) {
        delay(3000)

        if (DonorAccountData.getDonorLoginStatus(context)) {
            navController.navigate(AppDestinations.Home.route) {
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate(AppDestinations.Login.route) {
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        }


    }

    WelComeScreenDesign()
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

sealed class Screen(val route: String) {
    object CampaignList : Screen("campaign_list_route")
    object CampaignDetail : Screen("campaign_detail_route/{campaignId}") {
        fun createRoute(campaignId: String) = "campaign_detail_route/$campaignId"
    }
    object DonationHistory : Screen("donation_history_route")
    object DonationSummary : Screen("donation_summary_route")
    object AboutUs : Screen("about_us")
    object Profile : Screen("profile")

}