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
            require(!map.contains(user.login)){"A user with this email already exists"}
            map[user.login] = user
        }

    fun registerUserByPhone(fullName: String, rawPhone: String): User = User.makeUser(fullName, phone = rawPhone)
        .also { user ->
                require(user.validPhone) { "Enter a valid phone number starting with a + and containing 11 digits" }
                require(!map.contains(rawPhone.trim())){"A user with this phone already exists"}
                map[rawPhone.trim()] = user
        }

    fun importUser(
        fullName: String,
        email: String?,
        password: String,
        salt: String?,
        phone: String?
    ): User = User.makeUser(fullName, email, password, salt, phone)
        .also { user ->
            require(!map.contains(user.login)){"User already exists!"}
            map[user.login] = user
        }

    fun loginUser(
        login: String,
        password: String
    ): String? = map[login.trim().toLowerCase()]?.let {
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



    val INDEX_FULLNAME = 0
    val INDEX_EMAIL = 1
    val INDEX_PWDSALT = 2
    val INDEX_PHONE = 3

    fun importUsers(list: List<String>): List<User> {
        var userList = mutableListOf<User>()
        for (csvItem in list) {
            val csvCols = csvItem.split(";")
            val hasMail = !csvCols[INDEX_EMAIL].isNullOrBlank()
            val hasPhone = !csvCols[INDEX_PHONE].isNullOrBlank()
            if (!hasMail && !hasPhone)
                continue
            val (salt, pwd) = csvCols[INDEX_PWDSALT].split(":")
            userList.add(importUser(csvCols[INDEX_FULLNAME], csvCols[INDEX_EMAIL], pwd, salt, csvCols[INDEX_PHONE]))
        }

        return userList;
    }

}