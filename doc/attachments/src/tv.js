const common = require('./common');

common.createThingFromThingDescriptionFile(WoT, "./res/semantic-tv.json", function(thing) {
    thing.writeProperty("status", false)
    thing.writeProperty("channel", 1)
    thing.writeProperty("volume", 30)
    thing.writeProperty("brightness", 0)
})



/*

try {
    // Read the Thing Description inside folder
    fs.readFile("./res/semantic-tv.json", function(error, data) {
        if (error) {
            // Error trying to read file
            console.log("[ERROR] In file reading")
            console.log(error)
        } else {
            try {
                // Parsing the readed file to an object.
                let obj = JSON.parse(data)
                console.log("Data parsed succesfully. Producing...")
                
                // Produce the thing
                WoT.produce(obj)
                .then(thing => {
                    console.log("Thing created succesfully. Binding to attributes...")
                   
                    // Init variables
                    status = false                          // TV is off
                    channel = 1                             // Default channel
                    volume = 30                             // Default volume
                    brightness = 0                          // Since it's off

                    // Setting properties
                    this.setPropertyHandler(thing, "status", status)
                    this.setPropertyHandler(thing, "channel", channel)
                    this.setPropertyHandler(thing, "volume", volume)
                    this.setPropertyHandler(thing, "brightness", brightness)

                    // Finally, expose thing to public
                    thing.expose().then(() => { 
                        console.info(thing.getThingDescription().title + " ready"); 
                    });

                })
                .catch(error => {
                    // Error in promise
                    console.log("[ERROR] In promise")
                    console.log(error)
                })

            } catch (error) {
                // Error when reading file
                console.log("[ERROR] In file reading")
                console.log(error)
            }
        }
    })

    
} catch (error){
    // Error
    console.log("[ERROR] Generic")
    console.log(error)
}

*/