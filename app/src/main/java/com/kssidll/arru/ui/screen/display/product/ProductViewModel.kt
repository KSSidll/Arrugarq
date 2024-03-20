package com.kssidll.arru.ui.screen.display.product


import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.paging.*
import com.kssidll.arru.data.data.*
import com.kssidll.arru.data.repository.*
import com.kssidll.arru.domain.*
import com.kssidll.arru.domain.data.*
import com.patrykandpatrick.vico.core.entry.*
import dagger.hilt.android.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.*

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepositorySource,
): ViewModel() {
    private val mProduct: MutableState<Product?> = mutableStateOf(null)
    val product: Product? by mProduct

    private var mProductListener: Job? = null

    val chartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    private var mTimePeriodFlowHandler: TimePeriodFlowHandler<Data<List<ItemSpentByTime>>>? = null
    val spentByTimePeriod: TimePeriodFlowHandler.Periods? get() = mTimePeriodFlowHandler?.currentPeriod

    // TODO this should probably take from transaction spending instead of items
    // this being items means that if there's a transaction but no items are registered
    // the displayed data would be inaccurate
    val spentByTimeData: Flow<Data<List<ItemSpentByTime>>>? get() = mTimePeriodFlowHandler?.spentByTimeData

    fun productTotalSpent(): Flow<Data<Float?>>? {
        if (product == null) return null

        return productRepository.totalSpentFlow(product!!)
    }

    fun productPriceByShop(): Flow<Data<List<ProductPriceByShopByTime>>>? {
        if (product == null) return null

        return productRepository.averagePriceByVariantByShopByMonthFlow(product!!)
    }

    /**
     * @return paging data of full item for current product as flow
     */
    fun transactions(): Flow<PagingData<FullItem>> {
        if (product == null) return emptyFlow()
        return productRepository.fullItemsPagedFlow(product!!)
    }

    /**
     * Switches the state period to [newPeriod]
     * @param newPeriod Period to switch the state to
     */
    fun switchPeriod(newPeriod: TimePeriodFlowHandler.Periods) {
        mTimePeriodFlowHandler?.switchPeriod(newPeriod)
    }

    /**
     * @return True if provided [productId] was valid, false otherwise
     */
    suspend fun performDataUpdate(productId: Long) = viewModelScope.async {
        val product = productRepository.get(productId) ?: return@async false

        // We ignore the possiblity of changing category while one is already loaded
        // as not doing that would increase complexity too much
        // and if it happens somehow, it would be considered a bug
        if (mProduct.value != null || productId == mProduct.value?.id) return@async true

        mProductListener?.cancel()
        mProductListener = viewModelScope.launch {
            productRepository.getFlow(productId)
                .collectLatest {
                    if (it is Data.Loaded) {
                        mProduct.value = it.data
                    } else {
                        mProduct.value = null
                    }
                }
        }

        mProduct.value = product

        mTimePeriodFlowHandler = TimePeriodFlowHandler(
            scope = viewModelScope,
            dayFlow = {
                productRepository.totalSpentByDayFlow(product)
            },
            weekFlow = {
                productRepository.totalSpentByWeekFlow(product)
            },
            monthFlow = {
                productRepository.totalSpentByMonthFlow(product)
            },
            yearFlow = {
                productRepository.totalSpentByYearFlow(product)
            },
        )

        return@async true
    }
        .await()
}
