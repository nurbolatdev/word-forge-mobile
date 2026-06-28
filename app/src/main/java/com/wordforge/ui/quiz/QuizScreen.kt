package com.wordforge.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wordforge.domain.model.QuizQuestion
import com.wordforge.ui.navigation.NavRoutes
import com.wordforge.ui.theme.ErrorRed
import com.wordforge.ui.theme.Joy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: QuizViewModel, navController: NavController) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    val question by viewModel.question.collectAsStateWithLifecycle()
    val answerResult by viewModel.answerResult.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetQuiz()
                        navController.popBackStack(NavRoutes.QUIZ_SELECT, false)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit quiz")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            when (phase) {
                QuizPhase.IDLE, QuizPhase.LOADING -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                QuizPhase.QUESTION, QuizPhase.RESULT -> {
                    question?.let { q ->
                        QuestionContent(
                            question = q,
                            phase = phase,
                            correctText = answerResult?.correctTranslationText,
                            wasCorrect = answerResult?.correct,
                            selectedId = if (phase == QuizPhase.RESULT && q.modality == "MCQ")
                                answerResult?.let { if (it.correct) -2L else -1L } else null,
                            answeredId = answerResult?.correctTranslationId,
                            onMcqSelect = viewModel::submitMcq,
                            onTypedSubmit = viewModel::submitTyped,
                            onNext = viewModel::next
                        )
                    }
                }
                QuizPhase.DONE -> {
                    DoneScreen(
                        onNewRound = {
                            viewModel.resetQuiz()
                            navController.popBackStack(NavRoutes.QUIZ_MODE, false)
                        },
                        onChangeList = {
                            viewModel.resetQuiz()
                            navController.popBackStack(NavRoutes.QUIZ_SELECT, false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionContent(
    question: QuizQuestion,
    phase: QuizPhase,
    correctText: String?,
    wasCorrect: Boolean?,
    selectedId: Long?,
    answeredId: Long?,
    onMcqSelect: (Long) -> Unit,
    onTypedSubmit: (String) -> Unit,
    onNext: () -> Unit
) {
    val progress = (question.questionIndex.toFloat() + 1f) / question.totalCards.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "${question.questionIndex + 1} / ${question.totalCards}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        when (question.modality) {
            "MCQ" -> McqContent(
                question = question,
                phase = phase,
                wasCorrect = wasCorrect,
                correctId = answeredId,
                onSelect = onMcqSelect,
                onNext = onNext
            )
            "TYPING", "CLOZE" -> TypedContent(
                question = question,
                phase = phase,
                correctText = correctText,
                wasCorrect = wasCorrect,
                onSubmit = onTypedSubmit,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun McqContent(
    question: QuizQuestion,
    phase: QuizPhase,
    wasCorrect: Boolean?,
    correctId: Long?,
    onSelect: (Long) -> Unit,
    onNext: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = question.promptText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )
        }

        val pairs = question.options.chunked(2)
        pairs.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { option ->
                    val isCorrect = option.translationId == correctId
                    val containerColor = when {
                        phase == QuizPhase.RESULT && isCorrect -> Joy
                        phase == QuizPhase.RESULT && wasCorrect == false && !isCorrect -> ErrorRed.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = when {
                        phase == QuizPhase.RESULT && isCorrect -> Joy
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                    FilledTonalButton(
                        onClick = {
                            if (phase == QuizPhase.QUESTION) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelect(option.translationId)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = containerColor),
                        enabled = phase == QuizPhase.QUESTION
                    ) {
                        Text(
                            option.text,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        if (phase == QuizPhase.RESULT) {
            ResultFeedback(wasCorrect = wasCorrect == true, correctText = null, onNext = onNext)
        }
    }
}

@Composable
private fun TypedContent(
    question: QuizQuestion,
    phase: QuizPhase,
    correctText: String?,
    wasCorrect: Boolean?,
    onSubmit: (String) -> Unit,
    onNext: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var typed by rememberSaveable(question.cardId) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (question.modality == "CLOZE" && question.clozeText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = question.clozeText,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        }

        val promptLabel = if (question.modality == "CLOZE") "Fill in the blank" else question.promptText
        if (question.modality != "CLOZE") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = promptLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        }

        OutlinedTextField(
            value = typed,
            onValueChange = { typed = it },
            label = { Text(if (question.modality == "CLOZE") "Fill in the blank" else "Your answer") },
            modifier = Modifier.fillMaxWidth(),
            enabled = phase == QuizPhase.QUESTION,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { if (typed.isNotBlank() && phase == QuizPhase.QUESTION) onSubmit(typed.trim()) }
            )
        )

        if (phase == QuizPhase.QUESTION) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSubmit(typed.trim())
                },
                enabled = typed.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Check") }
        }

        if (phase == QuizPhase.RESULT) {
            ResultFeedback(wasCorrect = wasCorrect == true, correctText = correctText, onNext = onNext)
        }
    }
}

@Composable
private fun ResultFeedback(wasCorrect: Boolean, correctText: String?, onNext: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val feedbackColor = if (wasCorrect) Joy else ErrorRed
        val feedbackText = if (wasCorrect) "Correct!" else "Wrong"

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = feedbackColor.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, feedbackColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    feedbackText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = feedbackColor
                )
                if (!wasCorrect && correctText != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Correct: $correctText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Next →") }
    }
}

@Composable
private fun DoneScreen(onNewRound: () -> Unit, onChangeList: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "Round complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Great work! Keep it up.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(onClick = onNewRound, modifier = Modifier.fillMaxWidth()) {
            Text("New round")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onChangeList, modifier = Modifier.fillMaxWidth()) {
            Text("Change list")
        }
    }
}
