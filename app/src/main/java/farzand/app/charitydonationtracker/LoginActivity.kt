package farzand.app.charitydonationtracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LoginScreen()
        }
    }
}


@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current as Activity

//    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

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
                    contentDescription = "Email Icon"
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
                    contentDescription = "Password Icon"
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
//                        signInWithuseremail(email, password, context)
                    }

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 0.dp),
            shape = RoundedCornerShape(8.dp)
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
//                    context.startActivity(Intent(context, RegisterActivity::class.java))
//                    context.finish()
                }
            )

        }

        Spacer(modifier = Modifier.weight(1f))

    }

}



@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}