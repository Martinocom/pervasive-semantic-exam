package things

import cartago.Artifact
import cartago.INTERNAL_OPERATION
import cartago.OPERATION
import cartago.OpFeedbackParam
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.ModelFactory
import ru.gildor.coroutines.okhttp.await

class ThingArtifact : Artifact() {

    private lateinit var connection: Connection

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
    fun doOperation(deviceName: String, endpointUrl: String, actionName: String, params: String) {
        runBlocking {
            signalToAgent("${endpointUrl}${deviceName}/actions/$actionName")
            connection.postOnUrlAsync("${endpointUrl}${deviceName}/actions/$actionName", params)
        }
    }

    @OPERATION
    fun findThingAccomplishing(sarefType: String, things: OpFeedbackParam<Array<String>>) {
        // Create model from RDF
        signalToAgent("[ART] Finding things that accomplishes $sarefType...")

        val model = ModelFactory
            .createDefaultModel()
            .read("https://raw.githubusercontent.com/Martinocom/pervasive-semantic-exam/main/doc/attachments/td/RDF/unified.xml")

        val query = QueryFactory.create(
                "PREFIX saref: <https://w3id.org/saref#>" +
                        "SELECT ?s\n" +
                        "WHERE {\n" +
                        "   ?s saref:accomplishes \"$sarefType\" .\n" +
                        "}")

        val exec = QueryExecutionFactory.create(query, model)
        val result = exec.execSelect()
        signalToAgent("[ART] ...Query done!")

        val foundThings = mutableListOf<String>()
        while (result.hasNext()) {
            val solution = result.nextSolution()
            foundThings.add(solution["s"].toString())
        }

        exec.close()
        model.close()

        signalToAgent("[ART] Things found: $foundThings")
        things.set(foundThings.toTypedArray())
    }

    @OPERATION
    fun selectBestThingFor(sarefType: String, things: Array<*>, thing: OpFeedbackParam<String>) {

        signalToAgent("[ART] Selecting best thing for $sarefType")
        val model = ModelFactory
            .createDefaultModel()
            .read("https://raw.githubusercontent.com/Martinocom/pervasive-semantic-exam/main/doc/attachments/td/RDF/unified.xml")

        things.forEach {
            signalToAgent("...I'm $it")
        }

        val stringThings = things.map { it.toString().split("/").last() }
        signalToAgent("[ART] ...Converted to $stringThings")
        val mapped = stringThings.map { it to 0 }.toMap().toMutableMap()
        signalToAgent("[ART] ...Mapped to $mapped")

        val executionList = mutableListOf<QueryExecution>()

        stringThings.forEach {
            val query = QueryFactory.create(
                "PREFIX saref: <https://w3id.org/saref#>" +
                        "SELECT ?s ?o\n" +
                        "WHERE {\n" +
                        "   ?s saref:accomplishes ?o .\n" +
                        "   FILTER REGEX(str(?s), \"${it}\", \"i\") .\n" +
                        "}"
            )

            signalToAgent("[ART] Query is $query")
            val exec = (QueryExecutionFactory.create(query, model))
            val result = exec.execSelect()

            while (result.hasNext()) {
                val solution = result.next()
                if (mapped[it] != null) {
                    mapped[it] = mapped[it]!! + 1
                }
            }

            exec.close()
        }
        model.close()

        // Find the min
        var min = Pair(stringThings[0], mapped[stringThings[0]]!!)

        mapped.forEach {
            if (it.value < min.second) {
                min = Pair(it.key, it.value)
            }
        }

        thing.set(min.first)


        signalToAgent("[ART] Finally found ${thing.get()}")
    }

    private fun signalToAgent(message: String) {
        execInternalOp("onSignal", "progressSignal", message)
    }


}
