package com.example.dominionhelper.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SettingsList(settings: List<SettingItem>, modifier: Modifier = Modifier) {

    Log.i("SettingsList", "settings: $settings")

    LazyColumn(modifier = modifier) {

        items(settings) { setting ->
            when (setting) {
                is SettingItem.SwitchSetting -> SwitchSettingItem(setting)
                is SettingItem.TextSetting -> TextSettingItem(setting)
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