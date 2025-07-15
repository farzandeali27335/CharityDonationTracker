package farzand.e4383983.charitydonationtracker.data

sealed class AppDestinations(val route: String) {
    object Splash : AppDestinations("splash_route")
    object Login : AppDestinations("login_route")
    object Home : AppDestinations("home_route")
    // Add other destinations like Profile, DonationsHistory, Campaigns
    object Profile : AppDestinations("profile_route")
    object DonationsHistory : AppDestinations("donations_history_route")
    object Campaigns : AppDestinations("campaigns_route")
}