package edu.kit.outwait.customDataTypes

/**
 * currently our app supports two modes. See specification document K1
 */
enum class Mode(private val modeName: String) {
    ONE("one"), TWO("two");

    override fun toString(): String {
        return this.modeName
    }
}
