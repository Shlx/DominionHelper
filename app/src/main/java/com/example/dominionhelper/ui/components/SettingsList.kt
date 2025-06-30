package com.example.dominionhelper.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dominionhelper.ui.SettingItem


@Composable
fun SettingsList(settings: List<SettingItem>, modifier: Modifier = Modifier) {

    Log.i("SettingsList", "settings: $settings")

    LazyColumn(modifier = modifier) {

        items(settings) { setting ->
            when (setting) {
                is SettingItem.SwitchSetting -> SwitchSettingItem(setting)
                is SettingItem.TextSetting -> TextSettingItem(setting)
                is SettingItem.NumberSetting -> NumberSettingItem(setting)
            }
        }
    }
}

@Composable
fun SwitchSettingItem(setting: SettingItem.SwitchSetting) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = setting.title,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = setting.isChecked,
            onCheckedChange = setting.onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextSettingItem(setting: SettingItem.TextSetting) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = setting.title)
        TextField(
            value = setting.text,
            onValueChange = setting.onTextChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberSettingItem(setting: SettingItem.NumberSetting) {

    var textFieldValue by remember(setting.number) { mutableStateOf(setting.number.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = setting.title)
        TextField(
            value = textFieldValue,
            onValueChange = { newText ->
                textFieldValue = newText // Update the local text state immediately
                // Try to parse the integer, only call onNumberChange if valid
                // Allow empty string for temporary clearing of the field
                if (newText.isEmpty()) {
                    // Decide how to handle empty:
                    // Option 1: Call with a default (e.g., 0)
                    // setting.onNumberChange(0)
                    // Option 2: Do nothing yet, wait for valid number or blur
                    // Option 3: If your SettingItem allows nullable numbers, pass null
                } else {
                    newText.toIntOrNull()?.let { number ->
                        // Pass the parsed integer to the callback
                        // This assumes your onNumberChange expects an Int
                        setting.onNumberChange(number)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}