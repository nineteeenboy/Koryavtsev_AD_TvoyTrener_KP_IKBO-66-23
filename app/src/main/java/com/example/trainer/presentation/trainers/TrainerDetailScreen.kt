package com.example.trainer.presentation.trainers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trainer.R
import coil.compose.AsyncImage
import com.example.trainer.domain.model.*
import com.example.trainer.presentation.components.*
import com.example.trainer.util.UiState

/** Detailed trainer card and independently retryable review list. */
@Composable
fun TrainerDetailScreen(
    state: UiState<Trainer>, reviews: UiState<List<Review>>,
    onRetry: () -> Unit, onBook: () -> Unit, onReview: () -> Unit, onBack: () -> Unit,
) {
    ScreenFrame(stringResource(R.string.trainer_card_title), onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StateContent(state, onRetry) { trainer ->
                AsyncImage(trainer.photoUrl, stringResource(R.string.trainer_photo, trainer.name),
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
                Text(trainer.specialization.uppercase(), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
                Text(trainer.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("trainerName"))
                Text(if (trainer.experienceYears > 0) stringResource(R.string.trainer_experience, trainer.experienceYears)
                    else stringResource(R.string.trainer_experience_unknown))
                Text(if (trainer.reviewCount == 0) stringResource(R.string.trainer_no_reviews)
                    else stringResource(R.string.trainer_review_score, trainer.rating, trainer.reviewCount))
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                trainer.description.split("\n\n").forEach { section ->
                    val lines = section.split("\n", limit = 2)
                    Text(if (lines.size == 2) lines[0] else stringResource(R.string.trainer_about),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (lines.size == 2) lines[1] else section, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(6.dp))
                }
                HorizontalDivider()
                Text(stringResource(R.string.trainer_tariff,
                    trainer.price.toInt()), style = MaterialTheme.typography.titleLarge)
                Button(onClick = onBook, modifier = Modifier.fillMaxWidth().testTag("detailBook")) {
                    Text(stringResource(R.string.trainer_book))
                }
                OutlinedButton(onClick = onReview, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.review)) }
                Text(stringResource(R.string.trainer_reviews_title), style = MaterialTheme.typography.titleLarge)
                StateContent(reviews, onRetry, stringResource(R.string.trainer_reviews_empty)) { list ->
                    list.forEach { review ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${review.authorName} · ${review.rating}/5", style = MaterialTheme.typography.titleMedium)
                                Text(review.comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

