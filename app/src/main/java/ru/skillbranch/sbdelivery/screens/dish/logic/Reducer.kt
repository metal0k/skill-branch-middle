package ru.skillbranch.sbdelivery.screens.dish.logic

import ru.skillbranch.sbdelivery.data.network.res.ReviewRes
import ru.skillbranch.sbdelivery.screens.dish.data.DishUiState
import ru.skillbranch.sbdelivery.screens.dish.data.ReviewUiState
import ru.skillbranch.sbdelivery.screens.root.logic.Eff
import ru.skillbranch.sbdelivery.screens.root.logic.RootFeature
import ru.skillbranch.sbdelivery.screens.root.logic.RootState
import ru.skillbranch.sbdelivery.screens.root.logic.ScreenState

fun DishFeature.State.selfReduce(msg: DishFeature.Msg) : Pair<DishFeature.State, Set<Eff>> =
    when(msg){
        is DishFeature.Msg.AddToCart -> {
            copy(count = 1) to setOf(
                DishFeature.Eff.AddToCart(msg.id, msg.count)
            ).toEffs()
        }
        is DishFeature.Msg.DecrementCount -> copy(count = if (count > 1) count - 1 else count) to emptySet()
        is DishFeature.Msg.HideReviewDialog -> copy(isReviewDialog = false) to emptySet()
        is DishFeature.Msg.IncrementCount -> copy(count = count + 1) to emptySet()
        is DishFeature.Msg.SendReview -> {
            val curReviews = if(reviews is ReviewUiState.Value) reviews.list else emptyList()
            copy(
                isReviewDialog = false,
                reviews =   ReviewUiState.ValueWithLoading(curReviews)
            ) to setOf(DishFeature.Eff.SendReview(msg.dishId, msg.rating, msg.review)).toEffs()
        }
        is DishFeature.Msg.ShowDish -> copy(content = DishUiState.Value(msg.dish)) to emptySet()
        is DishFeature.Msg.ShowReviewDialog -> copy(isReviewDialog = true) to emptySet()
        is DishFeature.Msg.ShowReviews -> copy(reviews = ReviewUiState.Value(msg.reviews)) to emptySet()
        is DishFeature.Msg.ToggleLike -> copy(isLiked = !isLiked) to setOf(DishFeature.Eff.SetLike(id, isLiked)).toEffs()
    }

fun  DishFeature.State.reduce(root: RootState, msg: DishFeature.Msg) : Pair<RootState, Set<Eff>> {
    val (screenState, effs) = selfReduce(msg)
    return root.changeCurrentScreen<ScreenState.Dish> { copy(state = screenState) } to effs
}

private fun Set<DishFeature.Eff>.toEffs(): Set<Eff> = mapTo(HashSet(), Eff::Dish)