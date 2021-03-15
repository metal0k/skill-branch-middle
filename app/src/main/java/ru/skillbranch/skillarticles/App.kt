package ru.skillbranch.skillarticles

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import ru.skillbranch.skillarticles.data.local.PrefManager

//import com.facebook.stetho.Stetho

class App : Application() {

    companion object{
        private var instance : App? = null

        fun applicationContext() : Context{
            return  instance!!.applicationContext
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

//        val isNight =  PrefManager.getAppSettings().value?.isDarkMode ?: false
//        AppCompatDelegate.setDefaultNightMode(if (isNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
//        Log.e("App", "Night mode: $isNight")
//        Stetho.initializeWithDefaults(this)

    }

}