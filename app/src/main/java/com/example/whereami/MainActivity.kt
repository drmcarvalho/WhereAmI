package com.example.whereami

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.whereami.ui.theme.WhereAmITheme
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhereAmITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AddressScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AddressScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    var addressSuccess by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    val searchingAddressResource = stringResource(R.string.searching_address)
    val addressErrorResource = stringResource(R.string.address_error)
    val permissionDeniedResource = stringResource(R.string.location_permission_denied)
    val addressClipLabelResource = stringResource(R.string.address_clip_label)

    val showAddress = {
        addressSuccess = false
        address = searchingAddressResource
        scope.launch {
            val result = try {
                MyLocation(context).getFullAddress()
            } catch (_: IOException) {
                null
            }
            addressSuccess = result != null
            address = result ?: addressErrorResource
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.containsValue(true)) {
            showAddress()
        }
        else {
            addressSuccess = false
            address = permissionDeniedResource
        }
    }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(R.drawable.ic_launcher_playstore), contentDescription = stringResource(R.string.meme_image_description), modifier = Modifier.size(128.dp))
        Text(stringResource(R.string.title))
        Text(text = address, textAlign = TextAlign.Center)
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showAddress()
            }
            else {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }) {
            Text(stringResource(R.string.show_address_button))
        }
        Button(onClick = {
            if (addressSuccess) {
                scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(addressClipLabelResource, address))) }
            }
        }) {
            Text(stringResource(R.string.copy_button))
        }
    }
}
