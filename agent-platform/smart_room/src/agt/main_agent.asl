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
    <-  .print("[AG] GUI is now ready: ", Condition);
        getAllThingDescriptions(URL, TDs);
        viewThingDescriptions(TDs).


/* Rsponding to GUI events */
+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "LightUp" & lightingAbility(La)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithAbility(La).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "Comfort" & hvacAbility(Ha)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithAbility(Ha).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "News" & newsAbility(Na)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithAbility(Na).






+!achieveWithAbility(A) : endpoint(URL) & onOffAction(Action)
    <-  .print("[AG] Achieving: ", A);
        findThingAccomplishing(A, Things);
        .print("[AG] Waiting for accomplish done...");
        .wait(5000);

        .print("[AG] Selecting best things for: ", A, Things);
        selectBestThingFor(A, Things, Thing);
        .print("[AG] Waiting for selecting done...");
        .wait(5000);

        .print("[AG] Doing operation for: ", A);
        doOperation(Thing, URL, Action, "").


+!waitForIt(T) : value(T)
    <-  .print("T is not empty: ", T).

+!waitForIt(T) : not value(T)
    <-  .print("T empty!!!", T);
        .wait(1000);
        !waitForIt(T).

/* PLANS for achieve intentions */
/*+!lightUp : lightingAbility(La) & endpoint(URL) & onOffAction(Action)
    <-  .print("[AG] Lighting up the room");
        findThingAccomplishing(La, Things);
        .print(Things);
        selectBestThingFor(La, Things, Thing);
        doOperation(Thing, URL, Action, "").*/

/*
+!makeComfort : true
    <- .print("[AG] Making comfort").

+!enableNews : true
    <- .print("[AG] Enabling news source").
*/

/* Third goal: wait for ReasoningAgent
+reasoningReady(Rr) : Rr == true
    <- .print("[AG] ReasoningAgent is ready!").
    
+reasoningReady(Rr) : Rr == false
    <- .print("[AG] ReasoningAgent is not ready yet").


+suitableThingDescription(X) : not (X == "nothing")
    <- .print("[AG] Received ", X).*/

/* Subsequent goals: react to GUI changes

+achieveIntention(Intention) : reasoningReady(true) & Intention == "LightUp"
    <- .print("[AG] User wants to achieve Lights, sending to reasoner... ");
       .send(reasoning_agent, tell, currentIntention(lightup)).

+achieveIntention(Intention) : reasoningReady(true) & Intention == "Comfort"
    <- .print("[AG] User wants to achieve Comfort, sending to reasoner... ");
       .send(reasoning_agent, tell, currentIntention(comfort)).*/

/*
+achieveIntention(Intention) : reasoningReady(true)
    <- .print("[AG] User wants to achieve something, sending to reasoner: ", Intention);
       .send(reasoning_agent, tell, currentIntention(Intention)).

+achieveIntention(Intention) : reasoningReady(false)
    <- .print("[AG] ReasoningAgent is not available yet").*/





{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
