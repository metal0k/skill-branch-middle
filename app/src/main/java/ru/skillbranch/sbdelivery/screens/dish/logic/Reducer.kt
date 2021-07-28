package ru.skillbranch.sbdelivery.screens.dish.logic

import ru.skillbranch.sbdelivery.screens.dish.data.DishUiState
import ru.skillbranch.sbdelivery.screens.dish.data.ReviewUiState
import ru.skillbranch.sbdelivery.screens.root.logic.Eff
import ru.skillbranch.sbdelivery.screens.root.logic.RootState
import ru.skillbranch.sbdelivery.screens.root.logic.ScreenState

fun DishFeature.State.selfReduce(msg: DishFeature.Msg) : Pair<DishFeature.State, Set<Eff>> =
    when(msg){
        is DishFeature.Msg.AddToCart -> TODO()
        is DishFeature.Msg.DecrementCount -> copy(count = if (count > 1) count - 1 else count) to emptySet()
        is DishFeature.Msg.HideReviewDialog -> TODO()
        is DishFeature.Msg.IncrementCount -> copy(count = count + 1) to emptySet()
        is DishFeature.Msg.SendReview -> TODO()
        is DishFeature.Msg.ShowDish -> copy(content = DishUiState.Value(msg.dish)) to emptySet()
        is DishFeature.Msg.ShowReviewDialog -> TODO()
        is DishFeature.Msg.ShowReviews -> copy(reviews = ReviewUiState.Value(msg.reviews)) to emptySet()
        is DishFeature.Msg.ToggleLike -> copy(isLiked = !isLiked) to setOf(DishFeature.Eff.SetLike(id, isLiked)).toEffs()
    }

fun  DishFeature.State.reduce(root: RootState, msg: DishFeature.Msg) : Pair<RootState, Set<Eff>> {
    val (screenState, effs) = selfReduce(msg)
    return root.changeCurrentScreen<ScreenState.Dish> { copy(state = screenState) } to effs
}

private fun Set<DishFeature.Eff>.toEffs(): Set<Eff> = mapTo(HashSet(), Eff::Dish)