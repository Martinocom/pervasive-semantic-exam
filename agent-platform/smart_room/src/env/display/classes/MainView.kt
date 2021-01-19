package display.classes

import display.classes.ViewExtensions.Companion.BIG_TEXT_STYLE
import javafx.geometry.Insets
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import tornadofx.*


class MainView : ControlledView<MainViewController>() {

    companion object {

        private const val INTENTIONS_TITLE = "Intentions"
        private const val THINGS_TITLE = "Things"

        private const val BRIGHTNESS_INTENTION_TITLE = "Light up the room"
        private const val NEWS_LISTEN_INTENTION_TITLE = "Listen to some news"
        private const val MAKE_COMFORT_INTENTION_TITLE = "Make some comfort"

        private val BORDERPANE_MARGIN = Insets(5.0, 10.0, 5.0, 10.0)
    }

    override val root = borderpane {
        left {
            vbox {
                BorderPane.setMargin(this, BORDERPANE_MARGIN)

                label {
                    text = INTENTIONS_TITLE
                    style { fontSize = BIG_TEXT_STYLE }
                }

                vbox {
                    hgrow = Priority.ALWAYS
                    spacing = 5.0

                    button {
                        hgrow = Priority.ALWAYS
                        text = BRIGHTNESS_INTENTION_TITLE
                        action { controller.onLightUpRoom() }
                    }
                    button {
                        hgrow = Priority.ALWAYS
                        text = NEWS_LISTEN_INTENTION_TITLE
                        action { controller.onListenNews() }
                    }
                    button {
                        hgrow = Priority.ALWAYS
                        text = MAKE_COMFORT_INTENTION_TITLE
                        action { controller.onMakeComfort() }
                    }
                }
            }
        }

        center {
            vbox {
                // At the center, horizontally you can scroll every object that is found
                BorderPane.setMargin(this, BORDERPANE_MARGIN)
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                label {
                    text = THINGS_TITLE
                    style { fontSize = BIG_TEXT_STYLE }
                }

                scrollpane {
                    padding = Insets(10.0)
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS

                    hbox {
                        hgrow = Priority.ALWAYS
                        vgrow = Priority.ALWAYS
                        spacing = 10.0
                        // Simulate adding children
                        children.addAll(
                            ThingView().root,
                            ThingView().root
                        )
                    }
                }

            }
        }
    }
}