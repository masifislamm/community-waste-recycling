# Integration test cases (Template 1)

System-level tests run by hand on the real application (`java -jar target/cwrm.jar`), for report Section 5.3.
**Actual Result, Status and the screenshots are filled in by the group from the running app.** Nothing below has been run yet.

Seed data available at start-up: residents *Aisyah Contoh*, *Daniel Sample*, *Mei Ling Demo*; collectors *Collector Hafiz (demo)*, *Collector Priya (demo)*; seven fictional sample pickups PU-001 to PU-007 (three completed, one cancelled, three pending), so the first new booking is **PU-008** and *Aisyah Contoh* starts with **13 points**. Active reward rule at start: *Rate per material* (Paper earns 2 points per kg).

## Summary table

| Test ID | Input / Action | Expected Output | Actual Result | Category | Status |
|---|---|---|---|---|---|
| TC-01 | On **Book a pickup**, choose a resident, type an address, choose a material, keep tomorrow as the pickup day, click **Book pickup** | Green message "Pickup PU-008 booked for ... Status: PENDING."; a new row with a PENDING badge in *All pickups* | | GUI action | |
| TC-02 | With internet on, type a real Kuala Lumpur address (for example `Menara Kuala Lumpur`), click **Find address** (live Nominatim API) | Green message "Address found."; the address field shows the full display name; the hint under it says "Found on the map at (latitude, longitude)." | | Successful API call | |
| TC-03 | After TC-02, disconnect the network (Wi-Fi off), then click **Find address** again | Red message "Address service unavailable. Showing the last address found."; the last known address stays in the form and the hint under it turns amber ("Showing the last address found, at ..."); no stack trace or technical error text | | Failed API response | |
| TC-04 | Book 5 kg of Paper for a resident. On **Collector**, select that pickup, choose a collector, check the weight is `5.0`, click **Complete pickup**. Open **My rewards** for the same resident | Green message "Pickup PU-00x completed. 10 points credited to ..."; a row in the collection log; on **My rewards** a COMPLETED badge, "+10" under *Points earned* and the balance 10 points higher | | End-to-end logic | |
| TC-05 | Straight after TC-04, click **Complete pickup** again for the same pickup | Rejected with a red message ("Please select a pickup first.", because a completed pickup leaves the pending list); balance unchanged; no new log row | | Edge case | |

## Detailed steps and screenshots

### TC-01: Book a pickup (GUI action)
1. Start the app and open the **Book a pickup** tab (or click **Book a pickup** on Home).
2. Resident: *Aisyah Contoh*. Address: `1 Jalan Ujian, Kuala Lumpur`. Material: *Paper*. Pickup day: leave the default (tomorrow).
3. Click **Book pickup**.

Screenshot: `screenshots/tc-01.png`

### TC-02: Successful API call
1. Make sure the computer is online.
2. Type `Menara Kuala Lumpur` in **Address** and click **Find address**.
3. Wait for the status line (the window stays responsive while it searches).

Screenshot: `screenshots/tc-02.png`

### TC-03: Failed API response and graceful degradation
1. Complete TC-02 first, so there is a last known address.
2. Turn off Wi-Fi or unplug the network cable.
3. Click **Find address** again.
4. Turn the network back on afterwards.

Screenshot: `screenshots/tc-03.png`

### TC-04: End-to-end logic
1. Book a pickup: Material *Paper*, estimated weight `5` (the form shows "About 10 points with the current rule").
2. Open **Collector**, select the new row in *Waiting for collection*, choose a collector, check **Weight (kg)** shows `5.0` (pre-filled from the estimate) and click **Complete pickup**.
3. Open **My rewards** and choose the same resident.

Screenshots: `screenshots/tc-04-collector.png`, `screenshots/tc-04-rewards.png`

### TC-05: Edge case (completing twice is rejected)
1. Straight after TC-04, click **Complete pickup** again. The completed pickup has left the waiting list, so nothing is selected.
2. Note the red message.
3. Check the resident's balance on **My rewards** and the collection log: neither changes.

The same rule is covered by the unit tests `complete_calledTwice_doesNotCreditTwice` and `completePickup_secondTime_returnsFailureWithoutExtraPoints`.

Screenshot: `screenshots/tc-05.png`
