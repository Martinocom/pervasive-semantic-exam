const common = require('./common');

// Reference to local variable
let localRef = {
    isOpen: {
        name: "is-open",
        value: false,
    },
    openessTreshold: {
        name: "openess-treshold",
        value: 20,
        default: 20,
    },
    level: {
        name: "level",
        value: 0,
        minValue: 0,            // Fully closed
        maxValue: 100           // Fully opened
    },

    thing: null
}

common.createThingFromThingDescriptionFile(WoT, "./res/semantic-shutter.json", function(thing) {
    // Save reference to the thing
    localRef.thing = thing

    // Set read/write properties
    localRef.thing.writeProperty(localRef.isOpen.name, localRef.isOpen.value)
    localRef.thing.writeProperty(localRef.openessTreshold.name, localRef.openessTreshold.value)
    localRef.thing.writeProperty(localRef.level.name, localRef.level.value)

    // Overwrite behaviour on writing isOpen - it will just update it's value
    localRef.thing.setPropertyWriteHandler(localRef.isOpen.name, async(data) => {
        let level = await localRef.thing.readProperty(localRef.level.name)
        let treshold = await localRef.thing.readProperty(localRef.openessTreshold.name)
        return level > treshold
    })

    // Observe treshold changes
    localRef.thing.observeProperty(localRef.openessTreshold.name, async(data) => {
        let isOpen = await localRef.thing.readProperty(localRef.isOpen.name)
        let level = await localRef.thing.readProperty(localRef.level.name)
        checkShutterOpeness(level, data, isOpen)
    })

    // Observe level changes
    localRef.thing.observeProperty(localRef.level.name, async(data) => {
        let isOpen = await localRef.thing.readProperty(localRef.isOpen.name)
        let treshold = await localRef.thing.readProperty(localRef.openessTreshold.name)
        checkShutterOpeness(data, treshold, isOpen)
    })

    // Move it
    localRef.thing.setActionHandler("move", async(params) => {
        let level = await localRef.thing.readProperty(localRef.level.name)
        level += params.level
        return localRef.thing.writeProperty(localRef.level.name, level)
    })

    // Open it
    localRef.thing.setActionHandler("open", async(params) => {
        return localRef.thing.writeProperty(localRef.level.name, localRef.level.maxValue)
    })

    // Close it
    localRef.thing.setActionHandler("close", async(params) => {
        let treshold = await localRef.thing.readProperty(localRef.openessTreshold.name)
        return localRef.thing.writeProperty(localRef.level.name, treshold)
    })

})

async function checkShutterOpeness(currentLevel, levelTreshold, isOpen) {
    if (currentLevel > levelTreshold) {
        if (!isOpen) {
            // If current level (30) is higher than treshold (10) and shutter is NOT open, go and open it
            writeProperty(localRef.isOpen.name, true)
        }
    } else if (currentLevel <= levelTreshold) {
        if (isOpen) {
            // If current level (15) is lower than treshold (30) and shutters IS open, go and close it
            writeProperty(localRef.isOpen.name, false)
        }
    }
}

function writeProperty(name, value) {
    // Write object locally
    for (const key of Object.keys(localRef)) {
        if (key == name) {
            localRef[key].value = value
        }
    }

    // Wirte object remotely
    return localRef.thing.writeProperty(name, value)
}