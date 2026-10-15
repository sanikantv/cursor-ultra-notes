package app.lutex.proteincalculator.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.lutex.proteincalculator.R
import app.lutex.proteincalculator.core.designsystem.EmeraldButton
import app.lutex.proteincalculator.core.designsystem.EmeraldOutlinedButton
import app.lutex.proteincalculator.core.designsystem.GlassCard
import app.lutex.proteincalculator.core.designsystem.emeraldColors
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String,
    val showLogo: Boolean,
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_welcome_title),
            body = stringResource(R.string.onboarding_welcome_body),
            showLogo = true,
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_personal_title),
            body = stringResource(R.string.onboarding_personal_body),
            showLogo = false,
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_planning_title),
            body = stringResource(R.string.onboarding_planning_body),
            showLogo = false,
        ),
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val colors = emeraldColors()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val item = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (item.showLogo) {
                    Image(
                        painter = painterResource(R.drawable.ic_app_logo),
                        contentDescription = stringResource(R.string.logo_content_description),
                        modifier = Modifier
                            .size(168.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .testTag("app_logo"),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.height(28.dp))
                }
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(pages.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (selected) 22.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (selected) colors.accent else colors.textSecondary.copy(alpha = 0.4f)),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (pagerState.currentPage > 0) {
                EmeraldOutlinedButton(
                    text = stringResource(R.string.action_back),
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_back"),
                )
            }
            if (pagerState.currentPage < pages.lastIndex) {
                EmeraldButton(
                    text = stringResource(R.string.action_next),
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_next"),
                )
            } else {
                EmeraldButton(
                    text = stringResource(R.string.action_get_started),
                    onClick = onFinished,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_start"),
                )
            }
        }
    }
}
