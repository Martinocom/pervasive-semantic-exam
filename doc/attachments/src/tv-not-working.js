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


    // Observing status of status property (AFTER write)
    localRef.thing.observeProperty(localRef.status.name, async(data) => {
        if (data) {
            // Only if TV pass fron OFF to ON I need to bring all properties to "on" state
            await Promise.all([
                writeProperty(localRef.channel.name, localRef.channel.default),
                writeProperty(localRef.volume.name, localRef.volume.default),
                writeProperty(localRef.brightness.name, localRef.brightness.default)
            ])
        }

        // Change of status is already made, so no need to return anything
    })
    
    // Handling write of status property (BEFORE write)
    localRef.thing.setPropertyWriteHandler(localRef.status.name, async(data) => {
        if (!data) {
            // Only if TV pass from ON to OFF I need to bring all properties to "off" state
            await Promise.all([
                writeProperty(localRef.channel.name, localRef.channel.off),
                writeProperty(localRef.volume.name, localRef.volume.off),
                writeProperty(localRef.brightness.name, localRef.brightness.off)
            ])
        }
        // After writing, data will be the official status of the TV 
        return data
    })

    // Override behavior on writing status property
    localRef.thing.setPropertyWriteHandler(localRef.channel.name, async(data) => {
        return getPropertyToWrite(localRef.channel.name, data)
    })
    localRef.thing.setPropertyWriteHandler(localRef.volume.name, async(data) => {
        return getPropertyToWrite(localRef.volume.name, data)
    })
    localRef.thing.setPropertyWriteHandler(localRef.brightness.name, async(data) => {
        return getPropertyToWrite(localRef.brightness.name, data)
    })


    // In case of TV, brightness CAN be 0 since it means "no backlight"; still it should be visible
})

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

async function getPropertyToWrite(propertyName, data) {
    // Doesn't work = isTvOn will be FALSE after turning it on
    let isTvOn = await localRef.thing.readProperty(localRef.status.name)
    let currentValue = await localRef.thing.readProperty(propertyName)

    if (isTvOn) {
        return data
    } else {
        return currentValue
    }
}