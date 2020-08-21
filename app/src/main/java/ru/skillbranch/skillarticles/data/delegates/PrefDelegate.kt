package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PrefDelegate<TValue>(
    private val name: String,
    private val defValue: TValue
) : ReadWriteProperty<PrefManager, TValue?> {

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): TValue? {
        with(thisRef.preferences) {
            return when (defValue) {
                is Boolean -> (getBoolean(name, defValue) as? TValue) ?: defValue
                is Int -> (getInt(name, defValue) as TValue) ?: defValue
                is Float -> (getFloat(name, defValue) as TValue) ?: defValue
                is Long -> (getLong(name, defValue) as TValue) ?: defValue
                is String -> (getString(name, defValue) as TValue) ?: defValue
                else -> throw NotFoundRealizationException(defValue)
            }
        }
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: TValue?) {
        with(thisRef.preferences.edit()) {
            when (value) {
                is Boolean -> putBoolean(name, value)
                is Int -> putInt(name, value)
                is Float -> putFloat(name, value)
                is Long -> putLong(name, value)
                is String -> putString(name, value)
                else -> throw NotFoundRealizationException(value)
            }
            apply()
        }
    }

    class NotFoundRealizationException(defValue: Any?) : Exception("not found realization for $defValue")

}