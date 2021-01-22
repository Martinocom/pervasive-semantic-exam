// Agent sample_agent in project smart_room

/* Initial beliefs and rules */
guiStatus(false).
endpoint('http://localhost:8080/').

onOffAction('toggle').
lightingAbility('saref:Lighting').
newsAbility('saref:WellBeing').
hvacAbility('saref:Comfort').


/* Initial goals */

!start.

/* Plans */

/* First goal: make a view that user can use */
+!start : true
    <-  makeArtifact("c0","display.MainViewArtifact", [], MainArtifact);
        focus(MainArtifact);

        makeArtifact("c1","things.ThingArtifact", [], ThingArtifact);
        focus(ThingArtifact).

+progressSignal(Message) : true
    <-  .print(Message).


/* Second goal: attend GUI; when ready print that is ready */
+guiStatus(Condition) : Condition == true & endpoint(URL)
    <-  .print("GUI is now ready: ", Condition);
        getAllThingDescriptions(URL, TDs);
        viewThingDescriptions(TDs).


/* Rsponding to GUI events */
+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "LightUp" & lightingAbility(La)
    <- .print("User wants to achieve something: ", Intention);
       !achieveWithAbility(La).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "Comfort" & hvacAbility(Ha)
    <- .print("User wants to achieve something: ", Intention);
       !achieveWithAbility(Ha).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "News" & newsAbility(Na)
    <- .print("User wants to achieve something: ", Intention);
       !achieveWithAbility(Na).






+!achieveWithAbility(A) : endpoint(URL) & onOffAction(Action)
    <-  .print("Achieving: ", A);
        findThingAccomplishing(A, Things);
        .print(Things);
        selectBestThingFor(A, Things, Thing);
        doOperation(Thing, URL, Action, "").

/* PLANS for achieve intentions */
/*+!lightUp : lightingAbility(La) & endpoint(URL) & onOffAction(Action)
    <-  .print("Lighting up the room");
        findThingAccomplishing(La, Things);
        .print(Things);
        selectBestThingFor(La, Things, Thing);
        doOperation(Thing, URL, Action, "").*/

/*
+!makeComfort : true
    <- .print("Making comfort").

+!enableNews : true
    <- .print("Enabling news source").
*/

/* Third goal: wait for ReasoningAgent
+reasoningReady(Rr) : Rr == true
    <- .print("ReasoningAgent is ready!").
    
+reasoningReady(Rr) : Rr == false
    <- .print("ReasoningAgent is not ready yet").


+suitableThingDescription(X) : not (X == "nothing")
    <- .print("Received ", X).*/

/* Subsequent goals: react to GUI changes

+achieveIntention(Intention) : reasoningReady(true) & Intention == "LightUp"
    <- .print("User wants to achieve Lights, sending to reasoner... ");
       .send(reasoning_agent, tell, currentIntention(lightup)).

+achieveIntention(Intention) : reasoningReady(true) & Intention == "Comfort"
    <- .print("User wants to achieve Comfort, sending to reasoner... ");
       .send(reasoning_agent, tell, currentIntention(comfort)).*/

/*
+achieveIntention(Intention) : reasoningReady(true)
    <- .print("User wants to achieve something, sending to reasoner: ", Intention);
       .send(reasoning_agent, tell, currentIntention(Intention)).

+achieveIntention(Intention) : reasoningReady(false)
    <- .print("ReasoningAgent is not available yet").*/





{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
