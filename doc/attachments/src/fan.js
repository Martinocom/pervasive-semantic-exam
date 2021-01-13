const common = require('./common');

// Variables for Thing
let isReadyToSpeedUp = false

common.createThingFromThingDescriptionFile(WoT, "./res/semantic-fan.json", function(thing) {
    thing.writeProperty("status", 0)

    // Setting actions
    thing.setActionHandler("cool-down", async(parameters) => {
        if (isReadyToSpeedUp) {
            speedUp(thing)
        }
    })

    cooldown()
})

// Wait 10 seconds before another speedup can be achieved
function cooldown(thing) {
    setTimeout(function(){ 
        isReadyToSpeedUp = true
    }, 10000);
}

// Speed up the fan!
function speedUp(thing) {
    // Signal that is no more ready to be speeded up
    isReadyToSpeedUp = false

    // Update thing property (overclock to 4)
    thing.writeProperty("status", status).then(() => {
        // After updating, wait 5 seconds
        setTimeout(function(){ 
            // And then update property again (go back on max)
            thing.writeProperty("status", status).then(() => {
                // After updating start the cooldown countdown
                cooldown(thing)
            })
        }, 5000);
    })

}