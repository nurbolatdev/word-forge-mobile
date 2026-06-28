package com.wordforge.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wordforge.ui.navigation.NavRoutes

private data class ModalityOption(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String
)

private val modalityOptions = listOf(
    ModalityOption("MCQ", "☑", "Multiple Choice", "Pick from 4 options"),
    ModalityOption("TYPING", "⌨", "Typing", "Type the translation"),
    ModalityOption("CLOZE", "📝", "Fill the blank", "Complete the sentence")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectScreen(viewModel: QuizViewModel, navController: NavController) {
    val direction by viewModel.direction.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose mode") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Direction",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = direction == "EN_TO_RU",
                    onClick = { viewModel.setDirection("EN_TO_RU") },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("🇬🇧 EN → RU") }
                SegmentedButton(
                    selected = direction == "RU_TO_EN",
                    onClick = { viewModel.setDirection("RU_TO_EN") },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("🇷🇺 RU → EN") }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Mode",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            modalityOptions.forEach { option ->
                ModalityCard(
                    option = option,
                    onClick = {
                        viewModel.startRound(option.id)
                        navController.navigate(NavRoutes.QUIZ_PLAY)
                    }
                )
            }
        }
    }
}

@Composable
private fun ModalityCard(option: ModalityOption, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(option.emoji, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(
                    option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
