package farzand.e4383983.charitydonationtracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import farzand.e4383983.charitydonationtracker.data.UserDetails

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
//            LoginScreen()
        }
    }
}


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.PrimaryDark))
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Charity Donation Tracker",
            color = Color.Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Hey,\nLogin Now!",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter Registered E-Mail") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = colorResource(id = R.color.button_color)
                )
            },
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
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                when{
                    email.isEmpty() -> {
                        Toast.makeText(context, " Please Enter Mail", Toast.LENGTH_SHORT).show()
                    }
                    password.isEmpty() -> {
                        Toast.makeText(context, " Please Enter Password", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val userData = UserData(
                            "",
                            email,
                            "",
                            password
                        )

                        userSignIn(userData, context,onLoginSuccess)
//                        signInWithuseremail(email, password, context)
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
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))


        Row(
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text(
                text = "I'm an New User / ",
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = "Register Now",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                    context.finish()
                }
            )

        }

        Spacer(modifier = Modifier.weight(1f))

    }

}

fun userSignIn(userData: UserData, context: Context,onLoginSuccess: () -> Unit) {

    val firebaseDatabase = FirebaseDatabase.getInstance()
    val databaseReference =
        firebaseDatabase.getReference("UserData").child(userData.emailid.replace(".", ","))

    databaseReference.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val dbData = task.result?.getValue(UserData::class.java)
            if (dbData != null) {
                if (dbData.password == userData.password) {
//                    QRCodeGeneratorData.writeLS(context, true)
//                    QRCodeGeneratorData.writeMail(context, dbData.emailid)
//                    QRCodeGeneratorData.writeUserName(context, dbData.name)

                    saveUserDetails(userData, context)

                    onLoginSuccess.invoke()
                    Toast.makeText(context, "Login Successfully", Toast.LENGTH_SHORT).show()

//                    context.startActivity(Intent(context, HomeActivity::class.java))
//                    (context as Activity).finish()
                } else {
                    Toast.makeText(context, "Seems Incorrect Credentials", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(context, "Your account not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                context,
                "Something went wrong",
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}

fun saveUserDetails(user: UserData, context: Context) {
    UserDetails.saveUserLoginStatus(context = context, true)
    UserDetails.saveName(context, user.name)
    UserDetails.saveEmail(context, user.emailid)
}


data class UserData(
    var name : String = "",
    var emailid : String = "",
    var country : String = "",
    var password: String = ""
)



@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
//    LoginScreen()
}