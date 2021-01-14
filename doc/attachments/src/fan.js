const common = require('./common');

// Reference to local variable
let localRef = {
    status: {
        name: "status",
        value: 0
    },
    thing = null
}

// Temp variable for controlling the SpeedUp function
let isReadyToSpeedUp = false
let COOLDOWN_TIMEOUT = 5000
let MAX_STATUS = 3
let MAX_OC_STATUS = 4

common.createThingFromThingDescriptionFile(WoT, "./res/semantic-fan.json", function(thing) {
    // Save reference to the thing
    localRef.thing = thing
    
    // Set write/read properties
    localRef.thing.writeProperty(localRef.status.name, localRef.status.value)

    // Setting actions
    localRef.thing.setActionHandler("cool-down", async(parameters) => {
        if (isReadyToSpeedUp) {
            speedUp()
        }
    })

    cooldown()
})

// Wait 10 seconds before another speedup can be achieved
function cooldown() {
    setTimeout(function(){ 
        isReadyToSpeedUp = true
    }, 10000);
}

// Speed up the fan!
function speedUp() {
    // Signal that is no more ready to be speeded up
    isReadyToSpeedUp = false

    // Update thing property (overclock to 4)
    writeProperty(localRef.status.name, MAX_OC_STATUS).then(() => {
        // After updating, wait 5 seconds
        setTimeout(function(){ 
            // And then update property again (go back on max)
            writeProperty(localRef.status.name, MAX_STATUS).then(() => {
                // After updating start the cooldown countdown
                cooldown()
            })
        }, COOLDOWN_TIMEOUT);
    })
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