package com.kssidll.arru.ui.screen.settings


import androidx.compose.runtime.*
import dev.olshevski.navigation.reimagined.hilt.*

@Composable
fun SettingsRoute(
    navigateBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()

    SettingsScreen(
        setLocale = {
            viewModel.setLocale(it)
        },
        createBackup = {
            viewModel.createDbBackup()
        },
        loadBackup = {
            viewModel.loadDbBackup(it)
            navigateBack()
        },
        deleteBackup = {
            viewModel.deleteDbBackup(it)
        },
        availableBackups = viewModel.availableBackups.toList(),
        onBack = navigateBack,
    )
}
