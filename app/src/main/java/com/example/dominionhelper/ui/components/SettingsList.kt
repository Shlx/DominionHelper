package com.example.dominionhelper.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
                is SettingItem.ChoiceSetting<*> -> ChoiceSettingItem(setting)
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

@Composable
fun <E : Enum<E>> ChoiceSettingItem(setting: SettingItem.ChoiceSetting<E>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = setting.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SegmentedButtonRow(
            options = setting.allOptions,
            selectedOption = setting.selectedOption,
            optionDisplayFormatter = setting.optionDisplayFormatter,
            onOptionSelected = setting.onOptionSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A custom composable that mimics a row of segmented buttons for single choice selection.
 *
 * @param E The enum type for the choices.
 * @param options List of all available enum options.
 * @param selectedOption The currently selected option.
 * @param optionDisplayFormatter Function to get the display string for each option.
 * @param onOptionSelected Callback when an option is selected.
 * @param modifier Modifier for this composable.
 */
@Composable
fun <E : Enum<E>> SegmentedButtonRow(
    options: List<E>,
    selectedOption: E,
    optionDisplayFormatter: (E) -> String,
    onOptionSelected: (E) -> Unit,
    modifier: Modifier = Modifier,
    // Optional: Allow customization of colors and shapes
    selectedButtonColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    unselectedButtonColors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    ),
    buttonBorder: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
) {
    if (options.isEmpty()) return // Don't render if no options

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min), // Ensures all buttons in the row have the same height
        horizontalArrangement = Arrangement.spacedBy(0.dp) // No space between buttons
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption

            // Determine the shape for rounded corners on the ends
            val shape: CornerBasedShape = when (index) {
                0 -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50, topEndPercent = 0, bottomEndPercent = 0)
                options.lastIndex -> RoundedCornerShape(topStartPercent = 0, bottomStartPercent = 0, topEndPercent = 50, bottomEndPercent = 50)
                else -> androidx.compose.foundation.shape.RoundedCornerShape(0.dp) // Square shape for middle buttons
            }

            // For middle buttons, we only want the top/bottom border
            // For end buttons, they get their respective side border too.
            // A simpler approach for border is to let OutlinedButton handle it and adjust padding.
            // Or draw custom borders. This example uses standard Button/OutlinedButton styling.

            val currentButtonColors = if (isSelected) selectedButtonColors else unselectedButtonColors

            Button( // Or OutlinedButton, TextButton depending on the desired base style
                onClick = { onOptionSelected(option) },
                modifier = Modifier
                    .weight(1f) // Each button takes equal width
                    .fillMaxHeight(),
                shape = shape,
                colors = currentButtonColors,
                border = if (!isSelected) buttonBorder else null, // Show border for unselected
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp) // Adjust padding as needed
            ) {
                Text(
                    text = optionDisplayFormatter(option),
                    style = MaterialTheme.typography.labelLarge // Or bodyMedium etc.
                )
            }
        }
    }
}