package ru.skillbranch.sbdelivery.screens.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.skillbranch.sbdelivery.domain.User
import ru.skillbranch.sbdelivery.screens.root.ui.AppTheme
import java.lang.reflect.Modifier


@Composable
fun NavigationDrawer(
    currentRoute:String,
    modifier: Modifier = Modifier(),
    user: User? = User("Сидоров Иван", "sidorov.ivan@mail.ru"),
    notificationCount:Int = 0,
    cartCount:Int = 0,
    onSelect: (String) -> Unit
) {
}

@Preview
@Composable
fun DrawerPreview() {
    AppTheme {
        NavigationDrawer("home",  notificationCount = 7, cartCount = 8) {

        }
    }

}