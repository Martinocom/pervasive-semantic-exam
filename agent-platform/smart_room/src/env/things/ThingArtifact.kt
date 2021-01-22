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
            connection.postOnUrl("${endpointUrl}${deviceName}/actions/$actionName")
        }
    }

    @OPERATION
    fun findThingAccomplishing(sarefType: String, things: OpFeedbackParam<Array<String>>) {
        // Create model from RDF
        signalToAgent( "Finding things that accomplishes $sarefType...")

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
        signalToAgent( "...Query done!")

        val foundThings = mutableListOf<String>()
        while (result.hasNext()) {
            val solution = result.nextSolution()
            foundThings.add(solution["s"].toString())
        }

        exec.close()
        model.close()

        signalToAgent( "Things found: $foundThings")

        things.set(foundThings.toTypedArray())
    }

    @OPERATION
    fun selectBestThingFor(sarefType: String, things: Array<*>, thing: OpFeedbackParam<String>) {
        signalToAgent( "Selecting best thing for $sarefType")
        val model = ModelFactory
            .createDefaultModel()
            .read("https://raw.githubusercontent.com/Martinocom/pervasive-semantic-exam/main/doc/attachments/td/RDF/unified.xml")


        // Map every Thing to count = 0
        val stringThings = things.map { it.toString() }
        signalToAgent( "...Converted to $stringThings")
        val mapped = stringThings.map { it to 0 }.toMap()
        signalToAgent( "...Mapped to $mapped")


        // Foreach Thing create query of his accomplishing types
        stringThings.forEach {
            val lastElem = it.split("/").last()
            signalToAgent( "Last element is $lastElem")

            val query = QueryFactory.create(
                "PREFIX saref: <https://w3id.org/saref#>" +
                        "SELECT ?s\n" +
                        "WHERE {\n" +
                        "   ?s saref:accomplishes \"$sarefType\" .\n" +
                        "   FILTER REGEX(str(?s), \"${lastElem}\", \"i\") .\n" +
                        "}")

            signalToAgent( "Query is $query")

            val exec = QueryExecutionFactory.create(query, model)
            val result = exec.execSelect()

            // For each result inc the mapped value
            while (result.hasNext()) {
                mapped[it]?.inc()
            }

            exec.close()
        }
        model.close()

        // Find the min
        mapped.minByOrNull { it.value }?.let {
            thing.set(it.key)
        }. run {
            thing.set("")
        }

    }

    private fun signalToAgent(message: String) {
        execInternalOp("onSignal", "progressSignal", message)
    }


}
