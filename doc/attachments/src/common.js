// Dependencies
const fs = require('fs');

class Common {
    static createThingFromThingDescriptionFile(wotModule, path, setupFunction) {
        try {
            fs.readFile(path, function(error, data) {
                if (error) {
                    Common._errorLog(error, "In file reading")
                } else {
                    // Parse data to JavaScript object and create
                    let thingDescriptionObj = JSON.parse(data)
                    Common.createThingFromThingDescription(wotModule, thingDescriptionObj, setupFunction)
                }
            })
        } catch (error) {
            // Error when trying to read
            Common._errorLog(error, "When opening file")
        }
    }

    static createThingFromThingDescription(wotModule, thingDescription, setupFunction) {
        // Producing WebTing
        wotModule.produce(thingDescription)
        .then(thing => {
            // Create prefix
            const prefix = "[" + thing.getThingDescription().title + "] "

            // Bind to variables created
            console.log(prefix + "Created succesfully. Binding to attributes...")
            setupFunction(thing)

            // Expose thing after binding
            return thing.expose().then(() => { 
                // After exposing infor that Thing is ready
                console.log(prefix + "Ready!"); 
            }).catch(error => {
                Common._errorLog(error, prefix + "When exposing")
            })
        })
        .catch(error => {
            Common._errorLog(error, "When producing thing")
        })
    }

    static _errorLog(error, message) {
        console.log("[ERROR] " + message)
        console.log(error)
    }
}

module.exports = Common