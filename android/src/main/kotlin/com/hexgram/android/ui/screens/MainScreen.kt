package com.hexgram.android.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hexgram.android.ui.theme.HexgramColors
import com.hexgram.android.ui.theme.SerifFont

private data class TabItem(
    val label: String,
    val icon: ImageVector,
    val title: String
)

private val tabs = listOf(
    TabItem("六爻", Icons.Default.Star, "六爻纳甲"),
    TabItem("八字", Icons.Default.DateRange, "四柱八字"),
    TabItem("黄历", Icons.Default.DateRange, "黄历查询"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = tabs[selectedTab].title,
                        fontFamily = SerifFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HexgramColors.gold
                    )
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = HexgramColors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = HexgramColors.bgPrimary,
                    titleContentColor = HexgramColors.gold
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = HexgramColors.bgPanel,
                contentColor = HexgramColors.gold
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Text(
                                text = when (index) {
                                    0 -> "☰"
                                    1 -> "命"
                                    2 -> "历"
                                    else -> ""
                                },
                                fontSize = if (index == 0) 20.sp else 18.sp,
                                fontFamily = SerifFont,
                                color = if (selectedTab == index) HexgramColors.gold
                                else HexgramColors.textTertiary
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontFamily = SerifFont,
                                fontSize = 12.sp,
                                color = if (selectedTab == index) HexgramColors.gold
                                else HexgramColors.textTertiary
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = HexgramColors.gold.copy(alpha = 0.12f),
                            selectedIconColor = HexgramColors.gold,
                            selectedTextColor = HexgramColors.gold,
                            unselectedIconColor = HexgramColors.textTertiary,
                            unselectedTextColor = HexgramColors.textTertiary
                        )
                    )
                }
            }
        },
        containerColor = HexgramColors.bgPrimary
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HexgramColors.bgPrimary)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    0 -> LiuyaoScreen()
                    1 -> BaziScreen()
                    2 -> HuangliScreen()
                }
            }
        }
    }
}
