package things

import cartago.Artifact
import cartago.INTERNAL_OPERATION
import cartago.OPERATION
import cartago.OpFeedbackParam
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response
import org.apache.jena.query.Query
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.ModelFactory
import ru.gildor.coroutines.okhttp.await

class ThingArtifact : Artifact() {

    private lateinit var connection: Connection
    private val model by lazy {
        ModelFactory
            .createDefaultModel()
            .read("https://raw.githubusercontent.com/Martinocom/pervasive-semantic-exam/main/doc/attachments/td/RDF/unified.xml")
    }

    @OPERATION
    fun init() {
        connection = Connection()
    }

    /**
     * Internal operation that permit to Artifact to send messages to Agent, informing it about some [signalType] with [signalData]
     */
    @INTERNAL_OPERATION
    fun onSignal(signalType: String, signalData: String) {
        signal(signalType, signalData)
    }

    /**
     * Operation called by Agent to find all Thing Descriptions available on the endpoint
     */
    @OPERATION
    fun getAllThingDescriptions(endpointUrl: String, thingDescriptions: OpFeedbackParam<Array<String>>) {
        runBlocking {
            val response = connection.getFromUrlAsync(endpointUrl)

            if (response.body != null) {
                // Get all urls of Thing Description (but leave "servient" out)
                var tdUrls = Json.decodeFromString<List<String>>(response.body!!.string())
                tdUrls = tdUrls.filter { !it.contains("servient") }

                // Fetch TDs for every URL obtained
                val responses = mutableListOf<Response>()
                tdUrls.forEach {
                    responses.add(connection.getFromUrl(it).await())
                }

                // Set it as output for Agent
                thingDescriptions.set(responses.map { it.body!!.string() }.toTypedArray())
            }
        }
    }

    /**
     * Generic function that make possible to communicate with Web Things.
     * Given [deviceNames] and an [endpointUrl] will simulate the choose of the type of action to perform
     * (POST on actions or PUT on properties) and the desired effect (like 'toggle' or 'close').
     * Using then knowledge of the room, with SPARQL will ask for devices that can accomplish that action and
     * will choose the first action on that particular device that can do that.
     */
    @OPERATION
    fun doOperation(deviceNames: Array<*>, endpointUrl: String, affordanceTypes: Array<*>) {
        // Map affordances to string type (default is any)
        val strAffordanceTypes = affordanceTypes.map { it.toString() }

        deviceNames.forEach {
            // Take thing name
            val thingName = it.toString()

            // Simulate wanted action based of intention
            val wantedAction = acquireDesiredEffect(thingName)

            // Simulate understanding differences between property and action
            val accessParameter = if (wantedAction == null) "properties" else "actions"

            val suitableOperationUrl = if (wantedAction != null) {
                // Take name of action that is suitable for doing wanted intention
                trySearchActionNameForThing(thingName, strAffordanceTypes, wantedAction)
            } else {
                // Take name of the parameter that is stuitable for doing wanted intention
                trySearchPropertyNameForThing(thingName, strAffordanceTypes)
            }

            // In case of FAN, simulate that reading his TD I can understand max value to set
            val params = if (wantedAction == null) "3" else ""

            // The result of the query will be in form of URL, take the last param to match known URL of the endpoint
            val suitableOperationName = suitableOperationUrl?.split("/")?.last()

            // Save the url and inform agent that it's time to send message
            val operationString = "${endpointUrl}${thingName}/${accessParameter}/${suitableOperationName}"
            signalToAgent("[ART] Executing on [$operationString] with params [$params]")

            runBlocking {
            if (wantedAction != null) {
                // If there is an action = POST
                connection.postOnUrlAsync(operationString, params)
            } else {
                // If there is no action = PUT (=property set)
                connection.putOnUrlAsync(operationString, params)
            }
        }

        }
    }

    /**
     * Operation called from agent that finds all things that permit to achieve some ABILITY (ex. Safety)
     */
    @OPERATION
    fun findThingWithAbility(sarefType: String, things: OpFeedbackParam<Array<String>>) {
        signalToAgent("[ART] Finding things that accomplishes $sarefType...")

        val query = QueryFactory.create(
                "PREFIX saref: <https://w3id.org/saref#>" +
                        "SELECT ?s\n" +
                        "WHERE {\n" +
                        "   ?s saref:accomplishes \"$sarefType\" .\n" +
                        "}")

        val exec = QueryExecutionFactory.create(query, model)
        val result = exec.execSelect()

        val foundThings = mutableListOf<String>()
        while (result.hasNext()) {
            val solution = result.nextSolution()
            foundThings.add(solution["s"].toString())
        }

        exec.close()
        things.set(foundThings.toTypedArray())
        signalToAgent("[ART] Things found: ${things.get()}")
    }

    /**
     * Operation called from agent that finds all the things that are of a certain TYPOLOGY (ex. LightingDevice)
     */
    @OPERATION
    fun findThingWithTypology(sarefType: String, things: OpFeedbackParam<Array<String>>) {
        signalToAgent("[ART] Finding things that is of type $sarefType...")

        val query = QueryFactory.create(
            "PREFIX saref: <https://w3id.org/saref#>" +
                    "SELECT ?s\n" +
                    "WHERE {\n" +
                    "   ?s ?p $sarefType .\n" +
                    "}")

        val exec = QueryExecutionFactory.create(query, model)
        val result = exec.execSelect()

        val foundThings = mutableListOf<String>()
        while (result.hasNext()) {
            val solution = result.nextSolution()
            foundThings.add(solution["s"].toString())
        }

        exec.close()

        things.set(foundThings.toTypedArray())
        signalToAgent("[ART] Things found: $foundThings")
    }

    /**
     * Operation called from Agent to select the BEST thing for certain ABILITY (like Safety).
     */
    @OPERATION
    fun selectBestThingForAbility(sarefType: String, selectOnlyOneThing: Boolean, things: Array<*>, selectedThings: OpFeedbackParam<Array<String>>) {
        signalToAgent("[ART] Selecting best thing for $sarefType")
        val stringThings = things.map { it.toString().split("/").last() }

        if (selectOnlyOneThing) {
            val mapped = stringThings.map { it to 0 }.toMap().toMutableMap()

            stringThings.forEach {
                val query = QueryFactory.create(
                    "PREFIX saref: <https://w3id.org/saref#>" +
                            "SELECT ?s ?o\n" +
                            "WHERE {\n" +
                            "   ?s saref:accomplishes ?o .\n" +
                            "   FILTER REGEX(str(?s), \"${it}\", \"i\") .\n" +
                            "}"
                )

                val exec = (QueryExecutionFactory.create(query, model))
                val result = exec.execSelect()

                while (result.hasNext()) {
                    result.next()
                    if (mapped[it] != null) {
                        mapped[it] = mapped[it]!! + 1
                    }
                }

                exec.close()
            }

            // Find the min
            var min = Pair(stringThings[0], mapped[stringThings[0]]!!)

            mapped.forEach {
                if (it.value < min.second) {
                    min = Pair(it.key, it.value)
                }
            }

            selectedThings.set(listOf(min.first).toTypedArray())
        } else {
            // No condition, take them all and act!
            selectedThings.set(stringThings.toTypedArray())
        }

        signalToAgent("[ART] Found ${selectedThings.get()}")
    }

    /**
     * Operation called from Agent to select the BEST thing for certain TYPOLOGY (like LightingDevice).
     */
    @OPERATION
    fun selectBestThingForTypology(sarefType: String, selectOnlyOneThing: Boolean, things: Array<*>, selectedThings: OpFeedbackParam<Array<String>>) {
        val stringThings = things.map { it.toString().split("/").last() }

        if (selectOnlyOneThing) {
            selectedThings.set(arrayOf(stringThings[0]))
        } else {
            selectedThings.set(stringThings.toTypedArray())
        }
    }



    /**
     * This function simulate the fact that Agent knows what actions are to perform.
     * In this example, we know that we want to turn on the light (lightUp), TV (GoodVibes) and close the shutter (Safety),
     * so functions are those specified in the TD. In a real-case scenario, this is a knowledge to discover.
     */
    private fun acquireDesiredEffect(thingName: String): String? {
        return when(thingName) {
            "bulb" -> "toggle"
            "tv" -> "toggle"
            "shutter" -> "close"
            else -> null
        }
    }

    /**
     * This function applies sone SPARQL to ask the endpoint what is the action name that can be triggered to achieve wanted action.
     * Not simulated.
     */
    private fun trySearchActionNameForThing(thingName: String, forActions: List<String>, wantedAction: String) : String? {
        /*
            SELECT ?thing ?actionName ?command ?actionType
            WHERE {
              ?thing saref:hasFunction ?actionName .
              ?actionName saref:hasCommand ?command .
              ?actionName rdf:type ?actionType .
              FILTER REGEX(str(?actionType), "OpenCloseFunction", "i") .
              FILTER REGEX(str(?command), "open", "i") .
              + Thing filter
            }
        */
        forActions.forEach { action ->
            val query = QueryFactory.create(
                "PREFIX saref: <https://w3id.org/saref#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "SELECT ?thing ?actionName ?command ?actionType \n" +
                "WHERE {\n" +
                "   ?thing saref:hasFunction ?actionName . \n" +
                "   ?actionName saref:hasCommand ?command ; rdf:type ?actionType . \n"+
                "   FILTER REGEX(str(?actionType), \"${action}\", \"i\") . \n" +
                "   FILTER REGEX(str(?command), \"${wantedAction}\", \"i\") . \n" +
                "   FILTER REGEX(str(?thing), \"${thingName}\", \"i\") . \n" +
                "}\n"
            )

            val result = executeQueryAndTakeFirst(query, "actionName")
            if (result != null) return  result
        }

        return null
    }

    /**
     * This function applies sone SPARQL to ask the endpoint what is the property name that can be triggered to achieve wanted action.
     * Not simulated.
     */
    private fun trySearchPropertyNameForThing(thingName: String, forProperties: List<String>) : String? {
        /*
            SELECT ?s ?o ?c
            WHERE {
                ?s saref:hasState ?o .
                ?o rdf:type ?c .
                FILTER REGEX(str(?c), "MultiLevelState", "i") .
                FILTER REGEX(str(?s), "fan", "i") .
            }
        */
        forProperties.forEach { property ->
            val query = QueryFactory.create(
                "PREFIX saref: <https://w3id.org/saref#> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "SELECT ?s ?o ?c\n" +
                "WHERE {\n" +
                "    ?s saref:hasState ?o .\n" +
                "    ?o rdf:type ?c .\n" +
                "    FILTER REGEX(str(?c), \"${property}\", \"i\") .\n" +
                "    FILTER REGEX(str(?s), \"${thingName}\", \"i\") .\n" +
                "}"
            )

            val result = executeQueryAndTakeFirst(query, "o")
            if (result != null) return  result
        }

        return null
    }

    /**
     * Helper function that executes the query, and if there are results, takes the first one specified by [paramToTake]
     */
    private fun executeQueryAndTakeFirst(query: Query, paramToTake: String) : String? {
        val exec = (QueryExecutionFactory.create(query, model))
        val result = exec.execSelect()

        while (result.hasNext()) {
            return result.next().toString()
        }

        return null
    }

    /**
     * Helper function that trigger "onSignal" plan of the Agent to write a message.
     * Useful for debug.
     */
    private fun signalToAgent(message: String) {
        execInternalOp("onSignal", "progressSignal", message)
    }
}
