package com.kssidll.arrugarq.ui.component

import android.content.res.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.kssidll.arrugarq.domain.*
import com.kssidll.arrugarq.domain.data.*
import com.kssidll.arrugarq.helper.*
import com.kssidll.arrugarq.ui.component.chart.*
import com.kssidll.arrugarq.ui.theme.*
import com.patrykandpatrick.vico.compose.chart.*
import com.patrykandpatrick.vico.compose.chart.line.*
import com.patrykandpatrick.vico.compose.chart.scroll.*
import com.patrykandpatrick.vico.core.chart.scale.*
import com.patrykandpatrick.vico.core.entry.*
import com.patrykandpatrick.vico.core.scroll.*
import kotlinx.coroutines.*

@Composable
fun SpendingSummaryComponent(
    spentByTimeData: List<Chartable>,
    spentByTimePeriod: TimePeriodFlowHandler.Periods?,
    onSpentByTimePeriodSwitch: (TimePeriodFlowHandler.Periods) -> Unit,
    modifier: Modifier = Modifier,
    chartModifier: Modifier = Modifier,
    autoScrollSpec: AnimationSpec<Float> = tween(1200),
    scrollState: ChartScrollState = rememberChartScrollState(),
    columnChartEntryModelProducer: ChartEntryModelProducer = remember { ChartEntryModelProducer() },
    smaChartEntryModelProducer: ChartEntryModelProducer = remember { ChartEntryModelProducer() },
    runInitialAnimation: Boolean = true,
    columnWidth: Dp = 75.dp,
    columnSpacing: Dp = 12.dp,
) {
    Column(modifier = modifier) {
        PeriodButtons(
            spentByTimePeriod = spentByTimePeriod,
            onSpentByTimePeriodSwitch = onSpentByTimePeriodSwitch,
        )

        Spacer(modifier = Modifier.height(24.dp))

        OneDimensionalColumnChart(
            data = spentByTimeData,
            modifier = chartModifier,
            chartEntryModelProducer = columnChartEntryModelProducer,
            autoScrollSpec = autoScrollSpec,
            scrollState = scrollState,
            runInitialAnimation = runInitialAnimation,
            columnWidth = columnWidth,
            columnSpacing = columnSpacing,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.heightIn(max = 120.dp)) {
            val period = when (spentByTimePeriod) {
                TimePeriodFlowHandler.Periods.Day -> 28
                TimePeriodFlowHandler.Periods.Week -> 14
                TimePeriodFlowHandler.Periods.Month -> 7
                TimePeriodFlowHandler.Periods.Year -> 3
                null -> 1
            }

            SMAChart(
                data = spentByTimeData,
                autoScrollSpec = autoScrollSpec,
                scrollState = scrollState,
                period = period,
                lineSpacing = columnWidth + columnSpacing,
                chartEntryModelProducer = smaChartEntryModelProducer,
                runInitialAnimation = runInitialAnimation,
            )
        }


    }
}

/**
 * @param period: amount of data used to calculate the moving average
 * @param visibilityThreshold: how many points need to be visible before the chart is, actual data size required is period + visibiltyThreshold
 */
@Composable
private fun SMAChart(
    data: List<Chartable>,
    period: Int = 7,
    visibilityThreshold: Int = 5,
    alwaysReserveSpace: Boolean = false,
    autoScrollSpec: AnimationSpec<Float> = tween(1200),
    diffAnimationSpec: AnimationSpec<Float> = autoScrollSpec,
    scrollState: ChartScrollState = rememberChartScrollState(),
    lineSpacing: Dp = 87.dp,
    scrollOwner: Boolean = false,
    chartEntryModelProducer: ChartEntryModelProducer = remember { ChartEntryModelProducer() },
    runInitialAnimation: Boolean = true,
) {
    fun isVisible(): Boolean {
        return data.size >= visibilityThreshold + period
    }

    val scope = rememberCoroutineScope()

    val chart = lineChart(
        spacing = lineSpacing
    )

    if (isVisible() || alwaysReserveSpace) {
        LaunchedEffect(
            data,
            period
        ) {
            if (isVisible()) {
                val displayData = Array(data.size) { 0F }

                var sum = 0F
                for (itr in 0..<period) {
                    sum += data[itr].getValue()
                }

                displayData[period - 1] = sum / period

                for (itr in period..<data.size) {
                    sum -= data[itr - period].getValue()
                    sum += data[itr].getValue()
                    displayData[itr] = sum / period
                }

                chartEntryModelProducer.setEntries(displayData.mapIndexed { index, it ->
                    FloatEntry(
                        index.toFloat(),
                        it
                    )
                })
            } else {
                chartEntryModelProducer.setEntries(emptyList<ChartEntry>())
            }
        }

        Chart(
            chart = chart,
            chartScrollState = scrollState,
            chartModelProducer = chartEntryModelProducer,
            chartScrollSpec = rememberChartScrollSpec(
                isScrollEnabled = true,
                initialScroll = InitialScroll.End,
                autoScrollCondition = if (scrollOwner) AutoScrollCondition { _, oldModel ->
                    if (oldModel == null) return@AutoScrollCondition false

                    val newDataSize = data.size
                    val previousDataSize = oldModel.entries.getOrElse(0) { emptyList() }
                        .indexOfLast { it.y > 0F }

                    // handle back scroll
                    if (newDataSize < previousDataSize) {
                        val itemWidth =
                            (scrollState.maxValue + chart.bounds.width()).div(previousDataSize)
                        val itemDiff = newDataSize - previousDataSize
                        val scrollAmount = itemWidth * itemDiff
                        val relativeScrollAmount =
                            (scrollState.maxValue - scrollState.value) + scrollAmount
                        scope.launch {
                            scrollState.animateScrollBy(
                                value = relativeScrollAmount,
                                animationSpec = autoScrollSpec,
                            )
                        }
                        return@AutoScrollCondition false
                    }

                    return@AutoScrollCondition true
                } else AutoScrollCondition.Never,
                autoScrollAnimationSpec = autoScrollSpec,
            ),
            runInitialAnimation = runInitialAnimation,
            isZoomEnabled = false,
            diffAnimationSpec = diffAnimationSpec,
            autoScaleUp = AutoScaleUp.None,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodButtons(
    spentByTimePeriod: TimePeriodFlowHandler.Periods?,
    onSpentByTimePeriodSwitch: (TimePeriodFlowHandler.Periods) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp
            )
    ) {
        TimePeriodFlowHandler.Periods.entries.forEachIndexed { index, it ->
            val shape = when (index) {
                0 -> RoundedCornerShape(
                    topStartPercent = 50,
                    bottomStartPercent = 50
                )

                TimePeriodFlowHandler.Periods.entries.size - 1 -> RoundedCornerShape(
                    topEndPercent = 50,
                    bottomEndPercent = 50
                )

                else -> RectangleShape
            }

            SegmentedButton(
                selected = it == spentByTimePeriod,
                shape = shape,
                label = {
                    Text(it.getTranslation())
                },
                icon = {

                },
                onClick = {
                    onSpentByTimePeriodSwitch(it)
                },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.tertiary,
                    inactiveContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onTertiary,
                    inactiveContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    activeBorderColor = Color.Transparent,
                    inactiveBorderColor = Color.Transparent,
                )
            )
        }
    }

}

@Preview(
    group = "Spending Summary Component",
    name = "Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    group = "Spending Summary Component",
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SpendingSummaryComponentPreview() {
    ArrugarqTheme {
        Surface {
            SpendingSummaryComponent(
                spentByTimeData = generateRandomItemSpentByTimeList(),
                spentByTimePeriod = TimePeriodFlowHandler.Periods.Month,
                onSpentByTimePeriodSwitch = {},
            )
        }
    }
}