package com.kssidll.arrugarq.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kssidll.arrugarq.data.data.ProductCategory
import com.kssidll.arrugarq.data.data.ProductCategoryAltName
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductCategoryDao {
    @Query("SELECT * FROM productcategory ORDER BY id ASC")
    suspend fun getAll(): List<ProductCategory>

    @Query("SELECT * FROM productcategory ORDER BY id ASC")
    fun getAllFlow(): Flow<List<ProductCategory>>

    @Query("SELECT * FROM productcategory WHERE id == :id")
    suspend fun get(id: Long): ProductCategory

    @Query("SELECT * FROM productcategory WHERE id == :id")
    fun getFlow(id: Long): Flow<ProductCategory>

    @Query("SELECT * FROM productcategory WHERE typeId == :typeId")
    suspend fun getByTypeId(typeId: Long): List<ProductCategory>

    @Query("SELECT * FROM productcategory WHERE typeId == :typeId")
    fun getByTypeIdFlow(typeId: Long): Flow<List<ProductCategory>>

    @Query("SELECT * FROM productcategory WHERE name == :name")
    suspend fun getByName(name: String): ProductCategory

    @Query("SELECT * FROM productcategory WHERE name == :name")
    fun getByNameFlow(name: String): Flow<ProductCategory>

    @Query("SELECT productcategory.* from productcategory JOIN productcategoryaltname ON productcategory.id = productcategoryaltname.productCategoryId WHERE productcategory.name LIKE :name OR productcategoryaltname.name LIKE :name")
    suspend fun findLike(name: String): List<ProductCategory>

    @Query("SELECT productcategory.* from productcategory JOIN productcategoryaltname ON productcategory.id = productcategoryaltname.productCategoryId WHERE productcategory.name LIKE :name OR productcategoryaltname.name LIKE :name")
    fun findLikeFlow(name: String): Flow<List<ProductCategory>>

    @Insert
    suspend fun insert(productCategory: ProductCategory): Long

    @Insert
    suspend fun addAltName(alternativeName: ProductCategoryAltName): Long

    @Update
    suspend fun update(productCategory: ProductCategory)

    @Update
    suspend fun updateAltName(alternativeName: ProductCategoryAltName)

    @Delete
    suspend fun delete(productCategory: ProductCategory)

    @Delete
    suspend fun deleteAltName(alternativeName: ProductCategoryAltName)
}