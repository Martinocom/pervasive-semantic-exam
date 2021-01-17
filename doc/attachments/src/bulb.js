const common = require('./common');
const Ajv = require('ajv');

// Reference to local variable
let localRef = {
    status: {
        name: "status",
        value: false
    },
    brightness: {
        name: "brightness",
        value: 0,
        maxValue: 100,
        minValue: 0
    },
    thing: null
}

let ajv = new Ajv();

common.createThingFromThingDescriptionFile(WoT, "./res/semantic-bulb.json", function(thing) {
    // Save reference to the thing
    localRef.thing = thing
    
    // Set write/read properties
    localRef.thing.writeProperty(localRef.status.name, localRef.status.value)
    localRef.thing.writeProperty(localRef.brightness.name, localRef.brightness.value)

    // Observing status
    localRef.thing.observeProperty(localRef.status.name, async(data) => {
        // If status changes, look at the brightness
        let brightness = await localRef.thing.readProperty(localRef.brightness.name)

        // If bulb is on
        if (data == true) {
            // Brightness shoud be higher than 0
            if (brightness <= 0) {
                writeProperty(localRef.brightness.name, localRef.brightness.maxValue)
            }
        } else {
            // Bulb is off, brightness should be 0
            if (brightness > 0) {
                writeProperty(localRef.brightness.name, localRef.brightness.minValue)
            }
        }
    })

    // Observing brightness
    localRef.thing.observeProperty(localRef.brightness.name, async(data) => {
        let status = await localRef.thing.readProperty(localRef.status.name)
        
        // If brightness is higher than 0, bulb is on
        if (data > 0) {
            if (status == false) {
                writeProperty(localRef.status.name, true)
            }
        } else if (data <= 0 ) {
            // If brightness is 0, bulb is off
            if (status == true) {
                writeProperty(localRef.status.name, false)
            }
        }
    })

    // Toggle reads current status and switch it
    localRef.thing.setActionHandler("toggle", async(params) => {
        return localRef.thing.readProperty(localRef.status.name)
        .then((data) => {
            return writeProperty(localRef.status.name, !data)
        })
    });
    
    // Fade increment/decrement every 150ms brightness, to give the desired value
    localRef.thing.setActionHandler("fade", async(params) => {
        if (!ajv.validate(localRef.thing.getThingDescription().actions.fade.input, params)) {
            return new Error ("Invalid input");
        }
        else {
            fade(params.level);
        }
    })
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

async function fade(desiredLevel) {
    localRef.thing.readProperty(localRef.brightness.name)
    .then((data) => {
        // Exit out of recursion
        if (data == desiredLevel) return

        // Getting to result: if lower add, if higher subtract
        var promise = null
        if (data < desiredLevel) {
            promise = localRef.thing.writeProperty(localRef.brightness.name, data + 1)
        } else if (data > desiredLevel) {
            promise = localRef.thing.writeProperty(localRef.brightness.name, data - 1)
        }

        // After writing sleep 150ms and do it again
        promise.then(async() => {
            await common.sleep(150)
            fade(desiredLevel)
        })
    })
}