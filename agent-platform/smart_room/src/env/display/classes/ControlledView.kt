package display.classes

import tornadofx.Controller
import tornadofx.View
import java.lang.Exception
import java.lang.IllegalArgumentException

abstract class ControlledView<T> : View() where T : Controller {

    protected lateinit var controller: T

    fun setupController(controller: Controller) {
        try {
            this.controller = controller as T
        } catch (exception: Exception) {
            throw IllegalArgumentException("Controller is not of requested type!")
        }

    }
}