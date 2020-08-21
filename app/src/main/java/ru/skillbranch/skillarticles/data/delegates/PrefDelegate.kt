package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PrefDelegate<TValue>(
    private val defValue: TValue
) : ReadWriteProperty<PrefManager, TValue?> {

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): TValue? {
        with(thisRef.preferences) {
            return when (defValue) {
                is Boolean -> (getBoolean(property.name, defValue) as? TValue) ?: defValue
                is Int -> (getInt(property.name, defValue) as TValue) ?: defValue
                is Float -> (getFloat(property.name, defValue) as TValue) ?: defValue
                is Long -> (getLong(property.name, defValue) as TValue) ?: defValue
                is String -> (getString(property.name, defValue) as TValue) ?: defValue
                else -> throw NotFoundRealizationException(defValue)
            }
        }
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: TValue?) {
        with(thisRef.preferences.edit()) {
            when (value) {
                is Boolean -> putBoolean(property.name, value)
                is Int -> putInt(property.name, value)
                is Float -> putFloat(property.name, value)
                is Long -> putLong(property.name, value)
                is String -> putString(property.name, value)
                else -> throw NotFoundRealizationException(value)
            }
            apply()
        }
    }

    class NotFoundRealizationException(defValue: Any?) : Exception("not found realization for $defValue")

}