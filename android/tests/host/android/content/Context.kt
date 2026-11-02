package android.content
open class Context {
    companion object {
        const val MODE_PRIVATE = 0
    }

    val memory = SharedPreferences()

    fun getSharedPreferences(name: String, mode: Int) = memory
}

class SharedPreferences {
    val all = mutableMapOf<String, String>()

    fun getString(key: String, default: String?) = all[key] ?: default

    fun edit() = Editor(this)

    class Editor(val p: SharedPreferences) {
        fun putString(k: String, v: String): Editor {
            p.all[k] = v
            return this
        }

        fun clear(): Editor {
            p.all.clear()
            return this
        }

        fun apply() {}

        fun commit() = true
    }
}
