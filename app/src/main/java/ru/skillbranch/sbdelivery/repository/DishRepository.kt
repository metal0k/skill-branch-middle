package ru.skillbranch.sbdelivery.repository

import ru.skillbranch.sbdelivery.data.db.dao.CartDao
import ru.skillbranch.sbdelivery.data.db.dao.DishesDao
import ru.skillbranch.sbdelivery.data.network.RestService
import ru.skillbranch.sbdelivery.data.network.res.ReviewRes
import ru.skillbranch.sbdelivery.data.toDishContent
import ru.skillbranch.sbdelivery.screens.dish.data.DishContent
import java.io.IOException
import javax.inject.Inject

interface IDishRepository {
    suspend fun findDish(id: String): DishContent
    suspend fun addToCart(id: String, count: Int)
    suspend fun cartCount(): Int
    suspend fun loadReviews(dishId: String): List<ReviewRes>
    suspend fun sendReview(id: String, rating: Int, review: String): ReviewRes
    suspend fun setLike(id: String, like: Boolean)
}

class DishRepository @Inject constructor(
    private val api: RestService,
    private val dishesDao: DishesDao,
    private val cartDao: CartDao,
) : IDishRepository {
    override suspend fun findDish(id: String): DishContent = dishesDao.findDish(id).toDishContent()

    override suspend fun addToCart(id: String, count: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun cartCount(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun loadReviews(dishId: String): List<ReviewRes> {
        val res = api.getFullReviews(dishId,0, 100)
        return if (res.isSuccessful)
            res.body()!!.map{it.toReviewRes()}
        else
            emptyList()
    }

    override suspend fun sendReview(dishId: String, rating: Int, review: String): ReviewRes {
        TODO("Not yet implemented")
    }

    override suspend fun setLike(dishId: String, like: Boolean) {
    }
}