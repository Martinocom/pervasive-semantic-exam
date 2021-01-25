package display.classes

import tornadofx.Controller

abstract class MainViewController : Controller() {
    abstract fun onLightUpRoom()
    abstract fun onNotFeelingSafe()
    abstract fun onGoodVibes()
    abstract fun onSignal(message: String)
}