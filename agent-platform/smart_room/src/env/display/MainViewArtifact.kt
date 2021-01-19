package display

import cartago.Artifact
import cartago.INTERNAL_OPERATION
import cartago.OPERATION
import display.classes.GuiCreator
import display.classes.MainView
import display.classes.MainViewController
import javafx.stage.Stage
import tornadofx.App
import tornadofx.View

class MainViewArtifact : Artifact() {

    private var app: App? = null
    private var stage: Stage? = null
    private var view: View? = null

    companion object {
        // View things
        private const val GUI_TITLE = "Personal Agent"
        private val GUI_DIMENSION = Pair(600.0, 400.0)                          // Width + Height

        // Properties
        private const val GUI_STATUS_PROPERTY = "guiStatus"

        // Operations
        private const val GUI_STATUS_OPERATION = "onGuiStatusChange"
        private const val ACHIEVE_INTENTION_OPERATION = "onAchieveIntention"

        private const val LIGHT_UP_INTENTION = "LightUp"
        private const val NEWS_INTENTION = "News"
        private const val COMFORT_INTENTION = "Comfort"

        // Events
        private const val INTENTION_SIGNAL = "achieveIntention"
    }

    @OPERATION
    fun init () {
        // Observe gui ready state
        this.defineObsProperty(GUI_STATUS_PROPERTY, false)

        // Create gui
        with(GuiCreator.Builder()) {
            setTitle(GUI_TITLE)
            setDimensions(GUI_DIMENSION.first, GUI_DIMENSION.second)
        }.build(MainView::class, guiController) { app, stage, view ->
            // After view creation, save references and show view
            this.app = app
            this.stage = stage
            this.view = view
            stage.show()
            execInternalOp(GUI_STATUS_OPERATION, true)
        }
    }

    @INTERNAL_OPERATION
    fun onGuiStatusChange(status: Boolean) {
        getObsProperty(GUI_STATUS_PROPERTY).updateValue(status)
    }

    @INTERNAL_OPERATION
    fun onAchieveIntention(intention: String) {
        signal(INTENTION_SIGNAL, intention)
    }

    private val guiController = object : MainViewController() {
        override fun onLightUpRoom() {
            execInternalOp(ACHIEVE_INTENTION_OPERATION, LIGHT_UP_INTENTION)
        }

        override fun onListenNews() {
            execInternalOp(ACHIEVE_INTENTION_OPERATION, NEWS_INTENTION)
        }

        override fun onMakeComfort() {
            execInternalOp(ACHIEVE_INTENTION_OPERATION, COMFORT_INTENTION)
        }
    }
}