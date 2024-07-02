package com.kssidll.arru.ui.screen.display.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.kssidll.arru.PreviewExpanded
import com.kssidll.arru.R
import com.kssidll.arru.data.data.Transaction
import com.kssidll.arru.domain.data.Data
import com.kssidll.arru.ui.component.list.transactionBasketCard
import com.kssidll.arru.ui.component.other.SecondaryAppBar
import com.kssidll.arru.ui.theme.ArrugarqTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionScreen(
    onBack: () -> Unit,
    transaction: Data<Transaction?>,
    onEditAction: () -> Unit,
    onItemAddClick: (transactionId: Long) -> Unit,
    onItemClick: (productId: Long) -> Unit,
    onItemLongClick: (itemId: Long) -> Unit,
    onItemCategoryClick: (categoryId: Long) -> Unit,
    onItemProducerClick: (producerId: Long) -> Unit,
    onItemShopClick: (shopId: Long) -> Unit,
) {
    val headerColor = MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            SecondaryAppBar(
                onBack = onBack,
                title = {},
                actions = {
                    // 'edit' action
                    IconButton(
                        onClick = {
                            onEditAction()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(id = R.string.edit),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal),
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
    ) { paddingValues ->
        AnimatedVisibility(
            visible = transaction is Data.Loaded && transaction.data != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (transaction is Data.Loaded && transaction.data != null) {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                ) {
                    transactionBasketCard(
                        transaction = transaction.data,
                        itemsVisible = true,
                        onItemAddClick = onItemAddClick,
                        onItemClick = onItemClick,
                        onItemLongClick = onItemLongClick,
                        onItemCategoryClick = onItemCategoryClick,
                        onItemProducerClick = onItemProducerClick,
                        onItemShopClick = onItemShopClick,
                        headerColor = headerColor,
                        modifier = Modifier.width(600.dp)
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewExpanded
@Composable
private fun TransactionScreenPreview() {
    ArrugarqTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TransactionScreen(
                onBack = {},
                transaction = Data.Loaded(Transaction.generate()),
                onEditAction = {},
                onItemAddClick = {},
                onItemClick = {},
                onItemLongClick = {},
                onItemCategoryClick = {},
                onItemProducerClick = {},
                onItemShopClick = {},
            )
        }
    }
}
