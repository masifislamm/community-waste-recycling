# Integration test cases (Template 1)

System-level tests run by hand on the real application (`java -jar target/cwrm.jar`), for report Section 5.3.
**Actual Result, Status and the screenshots are filled in by the group from the running app.** Nothing below has been run yet.

Seed data available at start-up: residents *Aisyah Contoh*, *Daniel Sample*, *Mei Ling Demo*; collectors *Collector Hafiz (demo)*, *Collector Priya (demo)*. Active reward rule at start: *Rate per material* (Paper earns 2 points per kg).

## Summary table

| Test ID | Input / Action | Expected Output | Actual Result | Category | Status |
|---|---|---|---|---|---|
| TC-01 | On **Book pickup**, choose a resident, type an address, choose a material, keep tomorrow's date, click **Book pickup** | Green message "Pickup PU-001 booked for ... Status: PENDING."; a new row with status PENDING in the table | | GUI action | |
| TC-02 | With internet on, type a real Kuala Lumpur address (for example `Menara Kuala Lumpur`), click **Find address** (live Nominatim API) | Green message "Address found."; the address field shows the full display name; *Matched address* shows the name with latitude and longitude | | Successful API call | |
| TC-03 | After TC-02, disconnect the network (Wi-Fi off), then click **Find address** again | Red message "Address service unavailable. Showing the last address found."; the last known address stays in the form; no stack trace or technical error text | | Failed API response | |
| TC-04 | Book 5 kg of Paper for a resident. On **Collector**, select that pickup, choose a collector, enter `5`, click **Complete selected pickup**. Open **My rewards** for the same resident | Green message "Pickup PU-00x completed. 10 points credited to ..."; a row in the collection log; status COMPLETED and the balance increased by 10 points on **My rewards** | | End-to-end logic | |
| TC-05 | Straight after TC-04, click **Complete selected pickup** again for the same pickup | Rejected with a red message ("Please select a pickup first.", because a completed pickup leaves the pending list); balance unchanged; no new log row | | Edge case | |

## Detailed steps and screenshots

### TC-01: Book a pickup (GUI action)
1. Start the app and stay on the **Book pickup** tab.
2. Resident: *Aisyah Contoh*. Address: `1 Jalan Ujian, Kuala Lumpur`. Material: *Paper*. Date: leave the default (tomorrow).
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
1. Book a pickup: Material *Paper*, estimated weight `5`.
2. Open **Collector**, select the new row in *Pending pickups*, choose a collector, enter `5` in **Actual weight collected (kg)** and click **Complete selected pickup**.
3. Open **My rewards** and choose the same resident.

Screenshots: `screenshots/tc-04-collector.png`, `screenshots/tc-04-rewards.png`

### TC-05: Edge case (completing twice is rejected)
1. Straight after TC-04, click **Complete selected pickup** again. The completed pickup has left the pending list, so nothing is selected.
2. Note the red message.
3. Check the resident's balance on **My rewards** and the collection log: neither changes.

The same rule is covered by the unit tests `complete_calledTwice_doesNotCreditTwice` and `completePickup_secondTime_returnsFailureWithoutExtraPoints`.

Screenshot: `screenshots/tc-05.png`
