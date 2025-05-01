package com.example.dominionhelper.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dominionhelper.MainActivity
import com.example.dominionhelper.SettingsActivity
import com.example.dominionhelper.utils.navigateToActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    scope: CoroutineScope,
    drawerState: DrawerState,
    selectedOption: String
) {
    val screens = listOf("Home", "Settings", "Option 3")
    val context = LocalContext.current

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        screens.forEach { option ->
            NavigationDrawerItem(
                label = { Text(option) },
                selected = option == selectedOption,
                onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    when (option) {
                        "Home" -> {
                            navigateToActivity(context, MainActivity::class.java)
                        }

                        "Settings" -> {
                            navigateToActivity(context, SettingsActivity::class.java)
                        }

                        "Option 3" -> {
                            //navigateToActivity(context, AboutActivity::class.java)
                        }

                        else -> {}
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
