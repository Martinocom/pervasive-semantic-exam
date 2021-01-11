const { stat } = require('fs');

fs = require('fs');
let status, brightnessLevel

try {
    // Read the Thing Description inside folder
    fs.readFile("./res/semantic-bulb.json", function(error, data) {
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
                    status = false                          // Bulb is off
                    brightnessLevel = 0                     // Since it's off


                    // Reading/writing status
                    thing.setPropertyReadHandler("status", async () => {
                        console.log("Reading status: " + status)
                        return status
                    })
                    thing.setPropertyWriteHandler("status", async(value) => {
                        console.log("Writing status")
                        console.log(value)
                        if (checkPropertyWrite("boolean", value)) {
                            status = value
                        }
                    })

                    // Reading/writing brightness
                    thing.setPropertyReadHandler("brightness", async () => {
                        console.log("Reading brightness: " + brightnessLevel)
                        return brightnessLevel
                    })
                    thing.setPropertyWriteHandler("brightness", async(value) => {
                        console.log("Writing brightness")
                        console.log(value)
                        if (checkPropertyWrite("number", typeof value)) {
                         brightnessLevel = value
                        }
                    })

                    // Handler for toggle
                    thing.setActionHandler("toggle", async(parameters) => {
                        return new Promise((resolve, reject) => {
                            console.log("handling toggle action")
                            console.log(parameters)
                            status = !status
                            console.log("Now status is " + status)
                            resolve();
                        })
                    });

                    // Handler for fade
                    thing.setActionHandler("fade", async(params) => {
                        console.log("handling fade action")
                        console.log(params)
                        return true
                    })



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

function checkPropertyWrite(expected, actual) {
    let output = "Property " + expected + " written with type " + typeof actual;
    if (expected === typeof actual) {
        console.log("PASS: " + output);
        console.log(actual)
        return true
    }
    else {
        console.log("FAIL: " + output);
        console.log(actual)
        return false
    }
}