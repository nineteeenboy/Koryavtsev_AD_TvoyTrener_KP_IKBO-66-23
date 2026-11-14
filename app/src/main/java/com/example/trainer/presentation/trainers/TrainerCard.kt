package com.example.trainer.presentation.trainers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.trainer.R
import com.example.trainer.domain.model.Trainer

/** Shared portrait card used by the public welcome page and filtered catalog. */
@Composable
fun TrainerCard(trainer: Trainer, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth().testTag("trainer_${trainer.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        AsyncImage(trainer.photoUrl, stringResource(R.string.trainer_photo, trainer.name),
            modifier = Modifier.fillMaxWidth().height(280.dp), contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter)
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(trainer.specialization.uppercase(), color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium)
            Text(trainer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (trainer.experienceYears > 0) Text(stringResource(R.string.trainer_experience, trainer.experienceYears))
            Text(stringResource(R.string.trainer_tariff,
                trainer.price.toInt()), style = MaterialTheme.typography.bodyMedium)
            Text(if (trainer.reviewCount == 0) stringResource(R.string.trainer_no_reviews)
                else stringResource(R.string.trainer_review_score, trainer.rating, trainer.reviewCount))
            Text(stringResource(R.string.trainer_profile_link), color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
