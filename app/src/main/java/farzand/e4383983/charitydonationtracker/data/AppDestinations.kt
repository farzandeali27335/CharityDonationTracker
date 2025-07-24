package farzand.e4383983.charitydonationtracker.data

sealed class AppDestinations(val route: String) {
    object Splash : AppDestinations("splash_route")
    object Login : AppDestinations("login_route")
    object Home : AppDestinations("home_route")
    object Register : AppDestinations("register_route")


}