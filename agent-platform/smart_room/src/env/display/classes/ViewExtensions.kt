package display.classes

import javafx.scene.layout.Pane
import tornadofx.Dimension

class ViewExtensions {
    companion object {
        val BIG_TEXT_STYLE = Dimension(1.5, Dimension.LinearUnits.em)
        val MEDIUM_TEXT_STYLE = Dimension(1.2, Dimension.LinearUnits.em)

        /**
         * Makes width and height equal to [Double.MAX_VALUE]
         */
        fun Pane.forceMaxDimensions() {
            with(this) {
                forceMaxWidth()
                forceMaxHeight()
            }
        }

        /**
         * Makes height equal to [Double.MAX_VALUE]
         */
        fun Pane.forceMaxHeight() {
            with(this) {
                minHeight = Double.MAX_VALUE
                prefHeight = Double.MAX_VALUE
                maxHeight = Double.MAX_VALUE
            }
        }

        /**
         * Makes width equal to [Double.MAX_VALUE]
         */
        fun Pane.forceMaxWidth() {
            minWidth = Double.MAX_VALUE
            prefWidth = Double.MAX_VALUE
            maxWidth = Double.MAX_VALUE
        }

        /**
         * Makes height equal to value
         * @param value
         */
        fun Pane.forceMaxHeight(value: Double) {
            with(this) {
                minHeight = value
                prefHeight = value
                maxHeight = value
            }
        }

        /**
         * Makes width equal to value
         * @param value
         */
        fun Pane.forceMaxWidth(value: Double) {
            minWidth = value
            prefWidth = value
            maxWidth = value
        }
    }
}