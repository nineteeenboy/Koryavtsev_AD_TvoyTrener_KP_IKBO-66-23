package com.example.trainer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.trainer.presentation.theme.FitnessBlack
import com.example.trainer.R
import com.example.trainer.util.UiState

/** Shared screen chrome with accessible back navigation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenFrame(title: String, onBack: (() -> Unit)? = null, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = FitnessBlack, titleContentColor = Color.White, navigationIconContentColor = Color.White,
        ), navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
        })
    }, content = content)
}

/** Exhaustive state renderer used by catalog, details and profile. */
@Composable
fun <T> StateContent(
    state: UiState<T>, onRetry: () -> Unit, emptyText: String = stringResource(R.string.empty),
    content: @Composable (T) -> Unit,
) {
    when (state) {
        UiState.Loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(Modifier.testTag("loading"))
        }
        UiState.Empty -> Text(emptyText, Modifier.padding(24.dp).testTag("empty"))
        is UiState.Error -> Column(Modifier.padding(24.dp)) {
            Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("error"))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
        is UiState.Success -> content(state.data)
    }
}

/** Displays submit failures and in-flight progress inside forms. */
@Composable
fun ActionStatus(state: UiState<*>) {
    when (state) {
        UiState.Loading -> CircularProgressIndicator(Modifier.size(28.dp).testTag("loading"))
        is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("error"))
        else -> Unit
    }
}

