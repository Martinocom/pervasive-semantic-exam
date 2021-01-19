package display.classes

import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import tornadofx.App
import tornadofx.Controller
import tornadofx.View
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class GuiCreator {

    class Builder {
        private var title = "New Window"
        private var width = 400.0
        private var height = 400.0

        /**
         * Set title of the Window
         * @param title title of the Window, in [String]
         */
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        /**
         * Set dimensions of the window
         * @param width width of the Window, in [Double]
         * @param height height of the Window, in [Double]
         */
        fun setDimensions(width: Double, height: Double): Builder {
            this.width = width
            this.height = height
            return this
        }

        /**
         * Build the view, passing the class of the view and a simple listener that will trigger after view creation
         * @param viewClass class of the view to create that extends or is a [View]
         * @param listener lambda function that accepts [App], [Stage] and [View] objects as inputs
         */
        fun build(viewClass: KClass<out View>, controller: Controller, listener: (app: App, stage: Stage, view: View) -> Unit) {
            Platform.startup {
                val stage = Stage()
                val view = viewClass.createInstance()

                if (view is ControlledView<*>) {
                    view.setupController(controller)
                }

                stage.scene = Scene(view.root, width, height)
                stage.title = title
                val app = App()
                listener(app, stage, view)
            }
        }
    }
}