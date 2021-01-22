package display.classes

import display.classes.ViewExtensions.Companion.MEDIUM_TEXT_STYLE
import display.classes.ViewModel.Property
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.scene.control.Alert
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await
import things.Connection
import tornadofx.*

class ThingView(td: JsonObject) : View() {

    private var thingId = "ID"
    private var thingTitle = "Sample Thing"
    private val propertyList = FXCollections.observableArrayList<Property>()
    private val actionList = FXCollections.observableArrayList<String>()


    companion object {
        private const val PROPERTIES_TITLE = "Properties:"
        private const val ACTION_TITLE = "Actions:"

        private const val LIST_CELL_HEIGHT = 25
    }

    init {
        thingId = td["id"].toString().removeSurrounding("\"")
        thingTitle = td["title"].toString().removeSurrounding("\"")
        td["actions"]?.jsonObject?.map { it.key }?.let { actionList.addAll(it.toList()) }
        td["properties"]?.jsonObject?.map { it.key }?.let { propertyList.addAll(it.toList().map { prop -> Property(prop) }) }
        fetchPropertyData()

    }

    override val root = vbox {
        label {
            text = thingTitle
            style { fontSize = MEDIUM_TEXT_STYLE }
        }

        label { text = PROPERTIES_TITLE }
        listview(propertyList) {
            prefHeightProperty().bind(Bindings.size(propertyList).multiply(LIST_CELL_HEIGHT))
        }

        label { text = ACTION_TITLE }
        listview(actionList) {
            prefHeightProperty().bind(Bindings.size(propertyList).multiply(LIST_CELL_HEIGHT))

            onDoubleClick {
                executeAction(this.selectedItem)
            }
        }

        button {
            text = "Refresh"
            action { fetchPropertyData() }
        }
    }

    private fun fetchPropertyData() {
        GlobalScope.launch {
            val connection = Connection()
            val result = connection.getFromUrlAsync("${thingId}/all/properties")

            if (result.body != null) {
                val obj = Json.decodeFromString<JsonObject>(result.body!!.string())



                Platform.runLater {
                    val newList = FXCollections.observableArrayList<Property>()

                    propertyList.forEach {
                        it.value = obj[it.name].toString()
                        newList.add(it)
                    }

                    propertyList.clear()
                    propertyList.addAll(newList)
                }
            }
        }
    }

    private fun executeAction(actionName: String?, params: String = "") {
        actionName?.let { name ->
            GlobalScope.launch {
                /*val connection = Connection()
                val result = connection.postOnUrlAsync("${thingId}/actions/toggle", params)
                fetchPropertyData()*/


                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${thingId}/actions/$name")
                    .addHeader("Content-Type", "application/json")
                    .method("POST", "".toRequestBody())
                    .build()

                client.newCall(request).await()
                fetchPropertyData()
            }
        }
    }

    private fun log(message: String) {
        Platform.runLater {
            alert(Alert.AlertType.INFORMATION, "Debug", message)
        }
    }

}