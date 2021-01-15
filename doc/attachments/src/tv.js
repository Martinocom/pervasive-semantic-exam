const common = require('./common');

// Reference to local variable
let localRef = {
    status: {
        name: "status",
        value: false,
    },
    channel: {
        name: "channel",
        value: null,
        default: 1,
        off: null
    },
    volume: {
        name: "volume",
        value: 0,
        default: 30,
        off: 0
    },
    brightness: {
        name: "brightness",
        value: 0,
        default: 50,
        off: 0
    },
    thing: null
}


common.createThingFromThingDescriptionFile(WoT, "./res/semantic-tv.json", function(thing) {
    // Save reference to the thing
    localRef.thing = thing

    // Set write/read properties
    localRef.thing.writeProperty(localRef.status.name, localRef.status.value)
    localRef.thing.writeProperty(localRef.channel.name, localRef.channel.value)
    localRef.thing.writeProperty(localRef.volume.name, localRef.volume.value)
    localRef.thing.writeProperty(localRef.brightness.name, localRef.brightness.value)

    // Overwrite behaviour on writing properties: just update them with local data
    localRef.thing.setPropertyWriteHandler(localRef.status.name, async(data) => {
        return localRef.status.value
    })
    localRef.thing.setPropertyWriteHandler(localRef.channel.name, async(data) => {
        return localRef.channel.value
    })
    localRef.thing.setPropertyWriteHandler(localRef.volume.name, async(data) => {
        return localRef.volume.value
    })
    localRef.thing.setPropertyWriteHandler(localRef.brightness.name, async(data) => {
        return localRef.brightness.value
    })


    // Observe status changes
    localRef.thing.observeProperty(localRef.status.name, async(data) => {
        if (data) {
            // TV changes its state to ON -> set default values
            writeProperty(localRef.brightness.name, localRef.brightness.default)
            writeProperty(localRef.volume.name, localRef.volume.default)
            writeProperty(localRef.channel.name, localRef.channel.default)
        } else {
            // TV changes its state to OFF -> set OFF values
            writeProperty(localRef.brightness.name, localRef.brightness.off)
            writeProperty(localRef.volume.name, localRef.volume.off)
            writeProperty(localRef.channel.name, localRef.channel.off)
        }
    })


    // Toggle it
    localRef.thing.setActionHandler("toggle", async(params) => {
        let status = await localRef.thing.readProperty(localRef.status.name)
        return writeProperty(localRef.status.name, !status)
    })

    // Change Channel
    localRef.thing.setActionHandler("set-channel", async(params) => {
        return checkIfCanWrite(localRef.channel.name, params.channel)
    })

    // Change Brightness
    localRef.thing.setActionHandler("set-brightness", async(params) => {
        return checkIfCanWrite(localRef.brightness.name, params.brightness)
    })

    // Change Volume
    localRef.thing.setActionHandler("set-volume", async(params) => {
        return checkIfCanWrite(localRef.volume.name, params.volume)
    })
})

async function checkIfCanWrite(propertyName, valueToWrite) {
    let status = await localRef.thing.readProperty(localRef.status.name)

    if (status) {
        return writeProperty(propertyName, valueToWrite)
    } else {
        return "Tv is turned OFF. Impossible to perform [" + propertyName + "] update."
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