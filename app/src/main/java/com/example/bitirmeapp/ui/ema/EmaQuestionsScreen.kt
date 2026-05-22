package com.example.bitirmeapp.ui.ema

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitirmeapp.ui.ema.components.*
import com.example.bitirmeapp.ui.theme.NabzBackground
import com.example.bitirmeapp.ui.theme.NabzPrimary
import kotlinx.coroutines.launch

private const val TOTAL_PAGES = 7

@Composable
fun EmaQuestionsScreen(
    viewModel: EmaViewModel = viewModel(),
    onBackToIntro: () -> Unit,
    onComplete: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { TOTAL_PAGES })
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isComplete, state.savedEntryId) {
        val id = state.savedEntryId
        if (state.isComplete && id != null) {
            onComplete(id)
        }
    }

    LaunchedEffect(state.tahminHatasi) {
        state.tahminHatasi?.let { snackbarHostState.showSnackbar(it) }
    }

    val currentPage = pagerState.currentPage
    val isAnswered = when (currentPage) {
        0 -> state.stress != null
        1 -> state.pam != null
        2 -> state.socialLevel != null
        3 -> state.phq4Q1 != null
        4 -> state.phq4Q2 != null
        5 -> state.phq4Q3 != null
        6 -> state.phq4Q4 != null
        else -> false
    }
    val isLastPage = currentPage == TOTAL_PAGES - 1

    Scaffold(
        containerColor = NabzBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            EmaHeader(
                currentPage = currentPage,
                totalPages = TOTAL_PAGES,
                onBackClick = {
                    if (currentPage > 0) {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage - 1)
                        }
                    } else {
                        onBackToIntro()
                    }
                }
            )

            EmaProgressBar(
                currentPage = currentPage,
                totalPages = TOTAL_PAGES
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0 -> StressQuestion(
                        selected = state.stress,
                        onAnswered = { viewModel.updateStress(it) }
                    )
                    1 -> PamQuestion(
                        selected = state.pam,
                        onAnswered = { viewModel.updatePam(it) }
                    )
                    2 -> SocialLevelQuestion(
                        selected = state.socialLevel,
                        onAnswered = { viewModel.updateSocialLevel(it) }
                    )
                    3 -> Phq4Question(
                        questionTitle = "Gergin, kaygılı veya huzursuz hissetme",
                        selected = state.phq4Q1,
                        onAnswered = { viewModel.updatePhq4Q1(it) }
                    )
                    4 -> Phq4Question(
                        questionTitle = "Endişeyi durduramama veya kontrol edememe",
                        selected = state.phq4Q2,
                        onAnswered = { viewModel.updatePhq4Q2(it) }
                    )
                    5 -> Phq4Question(
                        questionTitle = "Yapılan işlerden zevk veya ilgi kaybı",
                        selected = state.phq4Q3,
                        onAnswered = { viewModel.updatePhq4Q3(it) }
                    )
                    6 -> Phq4Question(
                        questionTitle = "Çökkün, depresif veya umutsuz hissetme",
                        selected = state.phq4Q4,
                        onAnswered = { viewModel.updatePhq4Q4(it) }
                    )
                }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isLastPage) {
                        viewModel.completeAndSave()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                enabled = isAnswered && !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NabzPrimary,
                    disabledContainerColor = NabzPrimary.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (isLastPage) "BİTİR" else "DEVAM",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
