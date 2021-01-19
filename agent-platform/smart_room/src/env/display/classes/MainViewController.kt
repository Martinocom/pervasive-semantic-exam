package display.classes

import tornadofx.Controller

abstract class MainViewController : Controller() {
    abstract fun onLightUpRoom()
    abstract fun onListenNews()
    abstract fun onMakeComfort()
}