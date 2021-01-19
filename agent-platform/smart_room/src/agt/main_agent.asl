// Agent sample_agent in project smart_room

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

/* First goal: make a view that user can use */
+!start : true
    <-  makeArtifact("c0","display.MainViewArtifact", [], MainArtifact);
        focus(MainArtifact).

/* Second goal: attend GUI; when ready print that is ready */
+guiStatus(Condition) : Condition == true
    <-  .print("GUI is now ready: ", Condition).

+guiStatus(Condition) : Condition == false
    <-  .print("GUI is no more ready: ", Condition).

/* Subsequent goals: react to GUI changes */
+achieveIntention(Intention) : true
    <- .print("User wants to achieve: ", Intention).


{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }

// uncomment the include below to have an agent compliant with its organisation
//{ include("$moiseJar/asl/org-obedient.asl") }
