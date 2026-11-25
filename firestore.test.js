const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_nexlink_01";
const BOB_UID = "bob_nexlink_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validEventPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    title: "NEXLINK Architecture Sync",
    dateEpochDay: 20780,
    startMinutes: 540,
    endMinutes: 600,
    allDay: false,
    category: "WORK",
    location: "Studio A",
    notes: "Review glass grid precision metrics",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any calendar events", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("events").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("events")
      .doc("evt_1")
      .set(validEventPayload(ALICE_UID))
  );
});

test("Owner can create, list, partial update, and delete calendar event", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const eventsRef = aliceDb.collection("users").doc(ALICE_UID).collection("events");

  await assertSucceeds(eventsRef.doc("evt_1").set(validEventPayload(ALICE_UID)));

  // Matches Kotlin repository query: users/{uid}/events ordered by dateEpochDay
  await assertSucceeds(
    eventsRef.orderBy("dateEpochDay", "asc").get()
  );

  // Partial update mirroring Kotlin CalendarRepository.updateEvent
  await assertSucceeds(
    eventsRef.doc("evt_1").update({
      title: "NEXLINK Design Review",
      startMinutes: 600,
      endMinutes: 690,
      category: "FOCUS",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(eventsRef.doc("evt_1").delete());
});

test("Cross-user isolation: Alice cannot read, create, update, or delete Bob's events", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("events")
      .doc("bob_evt_1")
      .set(validEventPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobEvents = aliceDb.collection("users").doc(BOB_UID).collection("events");

  await assertFails(bobEvents.get());
  await assertFails(bobEvents.doc("bob_evt_1").get());
  await assertFails(bobEvents.doc("bob_evt_2").set(validEventPayload(BOB_UID)));
  await assertFails(
    bobEvents.doc("bob_evt_1").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bobEvents.doc("bob_evt_1").delete());
});

test("Validation guards reject @DocumentId injection, ghost fields, invalid category, future timestamps, and immutable createdAt mutation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const eventsRef = aliceDb.collection("users").doc(ALICE_UID).collection("events");

  // Reject 'id' field in payload
  await assertFails(
    eventsRef.doc("evt_bad_id").set(validEventPayload(ALICE_UID, { id: "evt_bad_id" }))
  );

  // Reject ghost field
  await assertFails(
    eventsRef.doc("evt_ghost").set(validEventPayload(ALICE_UID, { isAdmin: true }))
  );

  // Reject invalid category
  await assertFails(
    eventsRef.doc("evt_cat").set(validEventPayload(ALICE_UID, { category: "UNKNOWN" }))
  );

  // Reject future createdAt
  await assertFails(
    eventsRef
      .doc("evt_future")
      .set(validEventPayload(ALICE_UID, { createdAt: new Date(Date.now() + 60000) }))
  );

  // Reject endMinutes < startMinutes
  await assertFails(
    eventsRef
      .doc("evt_time")
      .set(validEventPayload(ALICE_UID, { startMinutes: 700, endMinutes: 600 }))
  );

  // Reject immutable createdAt modification on update
  await assertSucceeds(eventsRef.doc("evt_ok").set(validEventPayload(ALICE_UID)));
  await assertFails(
    eventsRef.doc("evt_ok").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
