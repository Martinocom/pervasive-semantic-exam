const common = require('./common');

common.createThingFromThingDescriptionFile(WoT, "./res/semantic-bulb.json", function(thing) {
    thing.writeProperty("status", false)
    thing.writeProperty("brightness", 20)

    thing.setActionHandler("toggle", async(params) => {
        console.log("handling toggle action")
        console.log(params)
        status = !status
        console.log("Now status is " + status)
        resolve();
    });
    
    thing.setActionHandler("fade", async(params) => {
        console.log("handling fade action")
        console.log(params)
        return true
    })
})