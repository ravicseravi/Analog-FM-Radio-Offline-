package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.audio.RadioAudioPlayer
import com.example.data.RadioRepository
import com.example.data.db.RadioDatabase
import com.example.ui.RadioScreen
import com.example.ui.RadioViewModel
import com.example.ui.RadioViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val radioViewModel: RadioViewModel by viewModels {
    val database = RadioDatabase.getDatabase(applicationContext)
    val repository = RadioRepository(database.favoriteStationDao())
    val audioPlayer = RadioAudioPlayer(applicationContext)
    RadioViewModelFactory(repository, audioPlayer)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
          ) { /* granted or denied */ }
          LaunchedEffect(Unit) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
          }
        }
        RadioScreen(viewModel = radioViewModel)
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}

