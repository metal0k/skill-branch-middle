package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User = User.makeUser(fullName, email = email, password = password)
        .also { user ->
            require(!map.contains(user.login)){"User already exists!"}
            map[user.login] = user
        }

    fun registerUserByPhone(fullName: String, rawPhone: String): User = User.makeUser(fullName, phone = rawPhone)
        .also { user ->
                require(user.validPhone) { "Phone format is invalid" }
                require(!map.contains(rawPhone.trim())){"User already exists!"}
                map[rawPhone.trim()] = user
        }

    fun loginUser(
        login: String,
        password: String
    ): String? = map[login.trim()]?.let {
        if (it.checkPassword(password)) it.userInfo
        else null
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun requestAccessCode(login: String) {
        val user = map[login]
        user?.apply {
            user.changePassword(accessCode!!, generateAccessCode())
        }

    }

}