package com.example.trainer.presentation.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainer.R
import com.example.trainer.domain.model.Trainer
import com.example.trainer.presentation.components.StateContent
import com.example.trainer.presentation.theme.FitnessBlack
import com.example.trainer.presentation.theme.FitnessRed
import com.example.trainer.presentation.trainers.TrainerCard
import com.example.trainer.util.UiState
import kotlinx.coroutines.launch

private data class GymSection(val photo: Int, val title: Int, val caption: Int, val text: Int)
private val gymSections = listOf(
    GymSection(R.drawable.gym_1, R.string.landing_gym_title, R.string.landing_gym_caption, R.string.landing_gym_text),
    GymSection(R.drawable.gym_2, R.string.landing_strength_title, R.string.landing_strength_caption, R.string.landing_strength_text),
    GymSection(R.drawable.gym_3, R.string.landing_functional_title, R.string.landing_functional_caption, R.string.landing_functional_text),
    GymSection(R.drawable.gym_4, R.string.landing_pool_title, R.string.landing_pool_caption, R.string.landing_pool_text),
)

/** Public welcome page with bundled gym photography and server-backed trainer profiles. */
@Composable
fun LandingScreen(
    trainers: UiState<List<Trainer>>, signedIn: Boolean,
    onContinue: () -> Unit, onTrainer: (Int) -> Unit, onRetry: () -> Unit,
) {
    val scroll = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val featured: UiState<List<Trainer>> = when (trainers) {
        is UiState.Success -> trainers.data.filter { it.isFeatured }.let {
            if (it.isEmpty()) UiState.Empty else UiState.Success(it)
        }
        else -> trainers
    }
    Scaffold(topBar = {
        Surface(color = FitnessBlack) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Image(painterResource(R.drawable.ic_trainer_brand),
                        stringResource(R.string.brand_logo_description), Modifier.size(60.dp))
                    Text(stringResource(R.string.brand_title), color = Color.White, fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, lineHeight = 16.sp, letterSpacing = 1.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                TextButton(onClick = { scope.launch { scroll.animateScrollToItem(5) } }) {
                    Text(stringResource(R.string.landing_team_link), color = Color.White)
                }
            }
        }
    }) { padding ->
        LazyColumn(state = scroll, modifier = Modifier.fillMaxSize().padding(padding).testTag("landingList"),
            contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "intro") {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(stringResource(R.string.landing_eyebrow), color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
                    Text(stringResource(R.string.landing_title), color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, lineHeight = 42.sp)
                    Text(stringResource(R.string.landing_intro), style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp)
                }
            }
            gymSections.forEachIndexed { index, section ->
                item(key = "gym_$index") {
                    Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(Modifier.size(6.dp, 48.dp).background(MaterialTheme.colorScheme.primary))
                            Text(stringResource(section.title), style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                        Image(painterResource(section.photo), stringResource(section.title), contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.05f))
                        Text(stringResource(section.caption), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(section.text), style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp)
                        HorizontalDivider(Modifier.padding(top = 10.dp, bottom = 30.dp))
                    }
                }
            }
            item(key = "teamTitle") {
                Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.landing_team_title), fontSize = 32.sp, lineHeight = 37.sp,
                        fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.landing_team_intro), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (featured is UiState.Success) {
                items(featured.data, key = { "profile_${it.id}" }) { trainer ->
                    Box(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                        TrainerCard(trainer, { onTrainer(trainer.id) })
                    }
                }
            } else item(key = "teamState") {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    StateContent(featured, onRetry, stringResource(R.string.landing_team_empty)) { }
                }
            }
            item(key = "cta") {
                Column(Modifier.fillMaxWidth().padding(top = 36.dp).background(FitnessBlack).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(stringResource(R.string.landing_cta_title), color = Color.White,
                        fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.landing_cta_text), color = Color(0xFFD4D0D2),
                        style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("landingContinue"),
                        colors = ButtonDefaults.buttonColors(containerColor = FitnessRed, contentColor = Color.White),
                        shape = RoundedCornerShape(4.dp)) {
                        Text(stringResource(if (signedIn) R.string.landing_catalog else R.string.landing_sign_in))
                    }
                    Text(stringResource(R.string.landing_footer), color = Color(0xFFAAA5A8),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
