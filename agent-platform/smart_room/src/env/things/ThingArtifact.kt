package things

import cartago.Artifact
import cartago.INTERNAL_OPERATION
import cartago.OPERATION
import cartago.OpFeedbackParam
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response
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

    @INTERNAL_OPERATION
    fun onSignal(signalType: String, signalData: String) {
        signal(signalType, signalData)
    }

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

    @OPERATION
    fun doOperation(deviceNames: Array<*>, endpointUrl: String) {
        runBlocking {
            deviceNames.forEach {
                val deviceName = it.toString()
                val opType = when(deviceName) {
                    "bulb" -> Pair("actions", "toggle")
                    "fan" -> Pair("properties", "status")
                    "tv" -> Pair("actions", "toggle")
                    "shutter" -> Pair("actions", "close")
                    else -> Pair("", "")
                }

                val params = when(deviceName) {
                    "bulb" -> ""
                    "fan" -> "3"
                    "tv" -> ""
                    "shutter" -> ""
                    else -> ""
                }

                signalToAgent("Executing on ${endpointUrl}${deviceName}/${opType.first}/${opType.second} with params [$params]")

                if (opType.first == "actions") {
                    connection.postOnUrlAsync("${endpointUrl}${deviceName}/${opType.first}/${opType.second}", params)
                } else {
                    connection.putOnUrlAsync("${endpointUrl}${deviceName}/${opType.first}/${opType.second}", params)
                }
            }

        }
    }

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

    @OPERATION
    fun selectBestThingForTypology(sarefType: String, selectOnlyOneThing: Boolean, things: Array<*>, selectedThings: OpFeedbackParam<Array<String>>) {
        val stringThings = things.map { it.toString().split("/").last() }

        if (selectOnlyOneThing) {
            selectedThings.set(arrayOf(stringThings[0]))
        } else {
            selectedThings.set(stringThings.toTypedArray())
        }
    }

    private fun signalToAgent(message: String) {
        execInternalOp("onSignal", "progressSignal", message)
    }
}
