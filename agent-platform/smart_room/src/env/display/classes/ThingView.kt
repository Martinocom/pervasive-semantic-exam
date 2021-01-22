package display.classes

import display.classes.ViewExtensions.Companion.MEDIUM_TEXT_STYLE
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import tornadofx.*

class ThingView(td: JsonObject) : View() {

    private var thingTitle = "Sample Thing"
    private val propertyList = FXCollections.observableArrayList<String>()
    private val actionList = FXCollections.observableArrayList<String>()


    companion object {
        private const val PROPERTIES_TITLE = "Properties:"
        private const val ACTION_TITLE = "Actions:"

        private const val LIST_CELL_HEIGHT = 25
    }

    init {
        thingTitle = td["title"].toString()
        td["actions"]?.jsonObject?.map { it.key }?.let { actionList.addAll(it.toList()) }
        td["properties"]?.jsonObject?.map { it.key }?.let { propertyList.addAll(it.toList()) }

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