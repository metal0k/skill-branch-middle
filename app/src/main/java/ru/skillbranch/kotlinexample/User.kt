package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String?  = null,
    meta: Map<String, Any> ? = null
) {
    val userInfo: String

    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize()

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")

    private var phone: String? = null
        set(value){
            field = value?.normalizePhone()
        }

    var validPhone: Boolean = false
        get() = phone?.matches("\\+?\\d{11}".toRegex()) ?: false

    fun String.normalizePhone() = this.replace("[^+\\d]".toRegex(), "")

    private var _login: String? = null
    var login: String
        set(value) {
            _login = value.toLowerCase()
        }
        get() = _login!!

    private var salt: String? = null

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    //for email
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ): this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("secondary mail constructor")
        passwordHash = encrypt(password)
    }

    //for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ): this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("secondary phone constructor")
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        println("PasswordHash is $passwordHash")
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    init {
        println("First init block, primary constructor called")

        check(firstName.isNotBlank()) {"First name must not be blank"}
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()){"Email or phone must not be blank"}

        phone = rawPhone
        login = email ?: phone!!

        userInfo = """
              firstName: $firstName
              lastName: $lastName
              login: $login
              fullName: $fullName
              initials: $initials
              email: $email
              phone: $phone
              meta: $meta
              """.trimIndent()
    }

    fun checkPassword(pwd: String) = encrypt(pwd) == passwordHash.also {
        println("Checking passwordHash is $passwordHash")
    }

    fun changePassword(oldPwd: String, newPwd: String) {
        if (checkPassword(oldPwd)) {
            passwordHash = encrypt(newPwd)
            if (!accessCode.isNullOrEmpty()) accessCode = newPwd
            println("Old pwd $oldPwd was changed to new pwd $newPwd")
        } else throw IllegalArgumentException("Old password does not match current password")
    }

    private fun encrypt(password: String): String {
        if (salt.isNullOrEmpty())
            salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        println("Salt pwd: $salt")
        return salt.plus(password).md5()
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(this.toByteArray())
        val hex = BigInteger(1, digest).toString(16)
        return hex.padStart(32, '0')
    }

    fun generateAccessCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return StringBuilder().apply{
            repeat(6) {
                (chars.indices).random().also { idx -> append(chars[idx]) }
            }
        }.toString()
    }

    fun sendAccessCodeToUser(phone: String, code: String) {
        println("... sending access code \"$code\" to phone $phone")
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()

            return when {
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() ->
                    User(
                        firstName, lastName,
                        email, password
                    )
                else -> throw IllegalArgumentException("Email or phone must not be null or blank")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> =
            this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size){
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("FullName must contain olny first and last names, current split "+
                        "result: ${this@fullNameToPair}")
                    }
                }
    }



}