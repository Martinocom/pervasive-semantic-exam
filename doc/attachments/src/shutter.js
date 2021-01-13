const common = require('./common');

common.createThingFromThingDescriptionFile(WoT, "./res/semantic-shutter.json", function(thing) {
    thing.writeProperty("is-open", false)
    thing.writeProperty("openess-treshold", 20)
    thing.writeProperty("level", 0)
})


/*
function updateStatusOnIsOpen() {
    // If isOpen == true shutter is fully opened (100)
    if (isOpen) { level = 100 } 
    // If isOpen == false shutter is closed (=openessTreshold)
    else { level = openessTreshold }
}

function updateStatusOnLevel(thing) {
    thing.readProperty("level").then((value) => {
        if (value <= openessTreshold) {
            isOpen = false
        } else {
            isOpen = true
        }
    })

    updateData(thing)
}

function updateStatusOnOpenessTreshold(thing) {
    thing.readProperty("openess-treshold").then((value) => {
        if (level <= value) {
            isOpen = false
        } else {
            isOpen = true
        }
    })

    updateData(thing)
}

function updateData(thing) {
    thing.writeProperty("isOpen", isOpen)
    thing.writeProperty("openessTreshold", openessTreshold)
    thing.writeProperty("level", level)
}
*/