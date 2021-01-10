fs = require('fs');

try {
    fs.readFile("./res/semantic-bulb.json", function(error, data) {
        if (error) {
            console.log("Error when reading")
        } else {
            try {
                let obj = JSON.parse(data)
                console.log("DATA PARSED ------------------------------------------------------")
                console.log(obj)
                console.log("")
                
                WoT.produce(obj)
                .then(thing => {
                    console.log("THING EXPOSED ------------------------------------------------------")
                    console.log(thing)
                    console.log("")

                    thing.expose()
                })
                .catch(error => {
                    console.log("ERROR ------------------------------------------------------")
                    console.log("error in promise")
                    console.log(error)
                })

            } catch (error) {
                console.log("ERROR ------------------------------------------------------")
                console.log("File readed but something went wrong")
                console.log(error)
            }
        }
    })

    
} catch (error){
    console.log("ERROR ------------------------------------------------------")
    console.log("Error when creating reader/opening")
    console.log(error)
}

