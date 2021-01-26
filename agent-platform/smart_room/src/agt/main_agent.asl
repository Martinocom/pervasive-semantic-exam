// Agent sample_agent in project smart_room

/* --------------------------------------------------------------------------------- */
/* Initial beliefs and rules */
/* --------------------------------------------------------------------------------- */
guiStatus(false).
endpoint('http://localhost:8080/').

/* Types of devices or comodities to achieve + actions to do them */
lightingType('saref:LightingDevice', ['OnOffFunction']).
safetyAbility('saref:Safety', ['OpenCloseFunction']).
comfortAbility('saref:Comfort', ['OnOffFunction', 'MultiLevelState']).


/* --------------------------------------------------------------------------------- */
/* Initial goals */
/* --------------------------------------------------------------------------------- */
!start.


/* --------------------------------------------------------------------------------- */
/* Plans */
/* --------------------------------------------------------------------------------- */

/* First goal: make Artifacts */
+!start : true
    <-  makeArtifact("c0","display.MainViewArtifact", [], MainArtifact);
        focus(MainArtifact);

        makeArtifact("c1","things.ThingArtifact", [], ThingArtifact);
        focus(ThingArtifact).


/* Second goal: wait for GUI and init all the system */
+guiStatus(Condition) : Condition == true & endpoint(URL)
    <-  .print("[AG] GUI is now ready: ", Condition);
        getAllThingDescriptions(URL, TDs);
        viewThingDescriptions(TDs).


/* Rsponding to GUI events */
+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "LightUp" & lightingType(La, AffordanceTypes)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithTypology(La, true, AffordanceTypes).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "Safety" & safetyAbility(Ha, AffordanceTypes)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithAbility(Ha, false, AffordanceTypes).

+achieveIntention(Intention) : guiStatus(X) & X == true & Intention == "GoodVibes" & comfortAbility(Na, AffordanceTypes)
    <- .print("[AG] User wants to achieve something: ", Intention);
       !achieveWithAbility(Na, false, AffordanceTypes).



+!achieveWithAbility(Ability, SelectOnlyOneThing, AffordanceTypes) : endpoint(URL)
    <-  .print("[AG] Achieving with ability: ", Ability);
        findThingWithAbility(Ability, Things);

        .print("[AG] Selecting best things for: ", Ability, Things);
        selectBestThingForAbility(Ability, SelectOnlyOneThing, Things, SelectedThings);

        .print("[AG] Doing operation for: ", Ability);
        doOperation(SelectedThings, URL, AffordanceTypes).

+!achieveWithTypology(Typology, SelectOnlyOneThing, AffordanceTypes) : endpoint(URL)
    <-  .print("[AG] Achieving with typology: ", Typology);
        findThingWithTypology(Typology, Things);

        .print("[AG] Selecting best things for: ", Typology, Things);
        selectBestThingForTypology(Typology, SelectOnlyOneThing, Things, SelectedThings);

        .print("[AG] Doing operation for: ", Typology);
        doOperation(SelectedThings, URL, AffordanceTypes).



+progressSignal(Message) : true
    <-  .print(Message).



{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
