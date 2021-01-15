const common = require('./common');

// Reference to local variable
let localRef = {
    status: {
        name: "status",
        value: false,
        isPendingRequest: false
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

    // Observing status
    localRef.thing.observeProperty(localRef.status.name, async(data) => {
        reactoToStatusChange(data)
    })

    // In case of TV, brightness CAN be 0 since it means "no backlight"; still it should be visible
})

async function reactoToStatusChange(data) {
    if (localRef.status.isPendingRequest == false) {
        localRef.status.isPendingRequest = true
        if (data != localRef.status.value) {
            // If it was off and now it's true, I need to turn on all params
            if (status) {
                Promise.all([
                    writeProperty(localRef.channel.name, localRef.channel.default),
                    writeProperty(localRef.volume.name, localRef.volume.default),
                    writeProperty(localRef.brightness.name, localRef.brightness.default)
                ]).then((values) => {
                    localRef.status.value = data
                    localRef.status.isPendingRequest = false
                    return
                })
            } else {
                // If it was on and now it's false, I need to turn off all params
                Promise.all([
                    writeProperty(localRef.channel.name, localRef.channel.off),
                    writeProperty(localRef.volume.name, localRef.volume.off),
                    writeProperty(localRef.brightness.name, localRef.brightness.off)
                ]).then((values) => {
                    localRef.status.value = data
                    localRef.status.isPendingRequest = false
                    return
                })
            }
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