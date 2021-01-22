package display.classes.ViewModel

data class Property(val name: String, var value: String) {
    constructor(name: String) : this(name, "")

    override fun toString(): String {
        return "$name: $value"
    }
}