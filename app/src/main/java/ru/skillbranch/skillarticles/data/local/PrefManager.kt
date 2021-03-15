package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager {
    internal val preferences: SharedPreferences by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
//        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
        prefs
    }


//    private val prefChangeListener =
//        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
//            when(key){
//                "isAuthPref" -> isAuthLiveData.postValue(isAuthPref)
//                "isDarkModePref" -> appSettingsLiveData.postValue(
//                    appSettingsLiveData.value?.copy(
//                        isDarkMode = isDarkModePref ?: false
//                    )
//                )
//                "isBigTextPref" -> appSettingsLiveData.postValue(
//                    appSettingsLiveData.value?.copy(
//                        isBigText = isBigTextPref ?: false
//                    )
//                )
//
//            }
//
//        }


    fun clearAll() {
        preferences.edit().clear().apply()
    }


    private var isAuthPref: Boolean? by PrefDelegate(false)
    private val isAuthLiveData = MutableLiveData(false)

    private var isDarkModePref: Boolean? by PrefDelegate(false)
    private var isBigTextPref: Boolean? by PrefDelegate(false)
    private val appSettingsLiveData by lazy {
        MutableLiveData(AppSettings(isDarkMode = isDarkModePref!!, isBigText = isBigTextPref!!))
    }

    fun getAppSettings(): LiveData<AppSettings> {
        return appSettingsLiveData
    }

    fun isAuth(): MutableLiveData<Boolean> {
        return isAuthLiveData
    }

    fun setAuth(auth: Boolean) {
        if (isAuthPref != auth) {
            isAuthPref = auth
            isAuthLiveData.value = auth
        }
    }

    fun updateAppSettings(appSettings: AppSettings) {
        appSettingsLiveData.value = appSettings;
        isDarkModePref = appSettings.isDarkMode
        isBigTextPref = appSettings.isBigText
    }
}
