package vn.vietbevis.apkbasic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import vn.vietbevis.apkbasic.core.navigation.APKBasicApp
import vn.vietbevis.apkbasic.ui.theme.APKBasicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APKBasicTheme {
                APKBasicApp()
            }
        }
    }
}
