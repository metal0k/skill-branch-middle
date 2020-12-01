package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.navigation.NavDestination
import com.google.android.material.bottomnavigation.BottomNavigationView

fun View.setMarginOptionally(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) {
    (this.layoutParams as? ViewGroup.MarginLayoutParams)?.apply{
        setMargins(
            left,
            top,
            right,
            bottom
        )
        this@setMarginOptionally.requestLayout()
    }
}

fun View.setPaddingOptionally(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) {
    this.setPadding(
        left,
        top,
        right,
        bottom
    )
}

fun BottomNavigationView.selectDestination(destination: NavDestination) {
    if (this.selectedItemId != destination.id) {
        val item = menu.findItem(destination.id)
        item?.isChecked = true
//        this.selectedItemId = destination.id
    }
    //TODO need to implement

}
