// Agent sample_agent in project smart_room

/* Initial beliefs and rules */
guiStatus(false).
endpoint('http://localhost:8080/').

lightingType('saref:LightingDevice').
safetyAbility('saref:Safety').
comfortAbility('saref:Comfort').


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
+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "LightUp" & lightingType(La)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithTypology(La, true).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "Safety" & safetyAbility(Ha)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithAbility(Ha, false).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "GoodVibes" & comfortAbility(Na)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithAbility(Na, false).






+!achieveWithAbility(Ability, SelectOnlyOneThing) : endpoint(URL)
    <-  .print("[AG] Achieving with ability: ", Ability);
        findThingWithAbility(Ability, Things);

        .print("[AG] Selecting best things for: ", Ability, Things);
        selectBestThingForAbility(Ability, SelectOnlyOneThing, Things, SelectedThings);

        .print("[AG] Doing operation for: ", Ability);
        doOperation(SelectedThings, URL).


+!achieveWithTypology(Typology, SelectOnlyOneThing) : endpoint(URL)
    <-  .print("[AG] Achieving with typology: ", Typology);
        findThingWithTypology(Typology, Things);

        .print("[AG] Selecting best things for: ", Ability, Things);
        selectBestThingForTypology(Typology, SelectOnlyOneThing, Things, SelectedThings);

        .print("[AG] Doing operation for: ", Ability);
        doOperation(SelectedThings, URL).






{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
