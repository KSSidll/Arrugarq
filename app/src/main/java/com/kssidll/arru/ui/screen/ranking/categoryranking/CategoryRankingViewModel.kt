package com.kssidll.arru.ui.screen.ranking.categoryranking

import androidx.lifecycle.ViewModel
import com.kssidll.arru.data.data.ItemSpentByCategory
import com.kssidll.arru.data.repository.CategoryRepositorySource
import com.kssidll.arru.domain.data.Data
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CategoryRankingViewModel @Inject constructor(
    private val categoryRepository: CategoryRepositorySource,
): ViewModel() {

    /**
     * @return List of data points representing shop spending in time as flow
     */
    fun categoryTotalSpentFlow(): Flow<Data<List<ItemSpentByCategory>>> {
        return categoryRepository.totalSpentByCategoryFlow()
    }
}