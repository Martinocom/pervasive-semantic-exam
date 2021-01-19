package display.classes

import display.classes.ViewExtensions.Companion.MEDIUM_TEXT_STYLE
import display.classes.ViewExtensions.Companion.forceMaxHeight
import display.classes.ViewModel.Action
import display.classes.ViewModel.Property
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import tornadofx.*
import tornadofx.Stylesheet.Companion.empty
import tornadofx.Stylesheet.Companion.listCell

class ThingView : View() {

    private var thingTitle = "Sample Thing"
    private val propertyList = FXCollections.observableArrayList<Property>()
    private val actionList = FXCollections.observableArrayList<Action>()


    companion object {
        private const val PROPERTIES_TITLE = "Properties:"
        private const val ACTION_TITLE = "Actions:"

        private const val LIST_CELL_HEIGHT = 24
    }

    override val root = vbox {
        label {
            text = thingTitle
            style { fontSize = MEDIUM_TEXT_STYLE }
        }

        label { text = PROPERTIES_TITLE }
        listview(propertyList) {
            prefHeightProperty().bind(Bindings.size(propertyList).multiply(LIST_CELL_HEIGHT));
        }

        label { text = ACTION_TITLE }
        listview(actionList) {
            prefHeightProperty().bind(Bindings.size(propertyList).multiply(LIST_CELL_HEIGHT));
        }
    }

}