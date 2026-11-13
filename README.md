# Community Waste and Recycling Manager (CWRM)

**Module:** CSC61204 Software Construction, Taylor's University
**Assignment:** Group Assignment, Scenario 3 (Community Waste and Recycling Manager)
**Group:** G5 · **Lecturer:** Dr. Reema Bin Thabit
**Repository:** https://github.com/masifislamm/community-waste-recycling

A Java desktop application that lets residents book recycling and bulky-waste pickups, lets collectors log completed collections, and rewards residents with points for recycling. It supports **UN SDG 11, Target 11.6** (reducing the environmental impact of cities through better municipal waste management).

> **Status:** all features are implemented and tested by CI. The five integration tests (TC-01 to TC-05) are run by hand on the real app; see `docs/integration-tests.md`.

---

## 1. Problem and scope

Recycling participation and collection coordination in Kuala Lumpur can be improved with better digital tooling. CWRM is a small-scale pilot prototype for a setting such as SWCorp, its collection partners and local communities (DBKL as a municipal stakeholder).

- **Pilot size (assumed):** about 500 residents, 5 collectors, 2 administrators
- **In scope:** pickup-request management, collection tracking, reward calculation, address lookup via OpenStreetMap Nominatim
- **Out of scope:** payment processing, vehicle-route optimisation, city-wide deployment

## 2. Features

| Area | What it does |
|---|---|
| Pickup booking | Residents book a pickup (mandatory: id, resident, address, material, date; optional: weight, notes, time slot) |
| Collection logging | Collectors complete a pickup; the system records a `CollectionLog` entry. A request can only be completed or cancelled once |
| Rewards | Points are calculated per material or campaign using swappable reward rules, and credited to the resident |
| Address lookup | Addresses are geocoded through the Nominatim API, with friendly error messages and a last-known-result fallback |
| Administration | Completion rate, average turnaround and kilograms collected per material; switch the reward rule at runtime |

**Screens (one tab per user role)**

| Tab | User | What it shows |
|---|---|---|
| Book pickup (`BookingPanel`) | Resident | Booking form with **Find address** (live lookup) and **Book pickup**; table of all pickups |
| Collector (`CollectorPanel`) | Collector | Pending pickups; complete (with actual weight) or cancel the selected one; collection log |
| My rewards (`RewardsPanel`) | Resident | Points balance, current reward rule and the status of the resident's pickups |
| Administrator (`AdminPanel`) | Municipal administrator, recycling partner | Pilot statistics, kilograms per material and the reward-rule switch |

## 3. Design overview

**Architecture:** Model-View-Controller with a service layer and an external API layer.

```
Views (Swing)            Controller              Services / API              Domain model
BookingPanel      ─┐                           PickupService        ──>   PickupRequest, Resident,
CollectorPanel    ─┤                           RewardService              Collector, CollectionLog,
RewardsPanel      ─┼──>  PickupController ──>  StatisticsService          Address, Material, TimeSlot
AdminPanel        ─┘     (returns Outcome)     NominatimAddressLookup
                                                      │
                                                      └──> HttpTransport ──> Nominatim API
```

- Views only display data and forward events to `PickupController`; they hold no business rules.
- `PickupController` validates every raw input and turns service errors into an `Outcome` with a friendly message.
- `model` and `service` never import Swing or AWT. The address search runs in a `SwingWorker`, off the Event Dispatch Thread.

**Design patterns**

| Pattern | Where | Why |
|---|---|---|
| **Builder** | `PickupRequest.Builder` | Pickup requests have 5 mandatory and 3 optional fields. The builder names each argument and validates the whole request in one place (Single Responsibility) |
| **Strategy** | `RewardRule` with `PerKgRule`, `PerMaterialRule`, `CampaignBonusRule` | New reward rules are added as new classes without editing `RewardService` (Open/Closed). Rules can be swapped at runtime |

**Key classes (see the CRC cards in the report, Section 3.4)**
`PickupRequest`, `RewardService`, `PickupService`, `NominatimAddressLookup`, `PickupController`

## 4. Technology stack

- Java **21**
- Java Swing (GUI)
- `java.net.http.HttpClient` (HTTP) and **Jackson** (JSON parsing)
- JUnit 5 (unit tests), JaCoCo (coverage, build fails below 75% line coverage)
- Checkstyle (clean-code rules, build fails on any violation)
- GitHub Actions (CI)
- Build tool: **Maven**

## 5. External API

- **Service:** OpenStreetMap Nominatim (address lookup)
- **Endpoint:** `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=<url-encoded address>`
- **Usage policy:** at most one request per second, with an identifying `User-Agent` header (set in `JdkHttpTransport`), as the Nominatim usage policy requires
- **Error handling:** 2xx is parsed and displayed; 404 and 500 map to friendly messages (never a stack trace); if the API is unreachable the last known result is used

## 6. Getting started

### Prerequisites
- JDK 21 or later
- Maven 3.9 or later, to build from source (IntelliJ IDEA's bundled Maven also works)
- Internet access, for the address lookup only; everything else works offline

### Build and run the executable JAR
```bash
mvn -B package
java -jar target/cwrm.jar
```
`mvn package` runs the unit tests, then builds `target/cwrm.jar`, a single "fat" JAR that already contains Jackson. It can be copied and run on any computer with Java 21 or later.

### Using the app
The app starts with fictional seed data: three residents and two collectors. All data is kept in memory and is reset when the app closes.
1. **Book pickup:** choose a resident, type an address (optionally click **Find address** to check it), choose a material and a date (`yyyy-MM-dd`), then click **Book pickup**.
2. **Collector:** choose a collector, select a pending pickup, enter the weight collected and click **Complete selected pickup** (or **Cancel selected pickup**).
3. **My rewards:** choose a resident to see their points and pickup statuses.
4. **Administrator:** view the statistics and switch the reward rule.

### Build and test from source
```bash
git clone https://github.com/masifislamm/community-waste-recycling.git
cd community-waste-recycling

mvn clean verify          # Checkstyle, compile, unit tests, JAR, coverage report and 75% check
mvn test                  # run unit tests only
mvn package               # build target/cwrm.jar
mvn test -Pshow-limitation   # run ONLY the deliberately failing test (expected to fail)
```
The coverage report is written to `target/site/jacoco/index.html`.

## 7. Testing

- **Unit tests (JUnit 5):** pickup-request validation, reward calculation, pickup lifecycle, statistics, the Nominatim lookup (with a fake `HttpTransport`, so no test uses the network) and controller input validation. Test names follow `methodName_scenario_expectedBehaviour`
- **Coverage:** JaCoCo; the build fails below 75% line coverage. The GUI (`view` package and `Main`) is excluded because it is not unit-tested
- **Deliberately failing test:** `RewardRuleTest.calculatePoints_perKgRuleFractionalWeight_roundsToNearestPoint` shows that `PerKgRule` truncates fractional points. It is tagged `known-limitation` and left out of the normal build; run it with `mvn test -Pshow-limitation`
- **Appendix A table:** after `mvn verify`, run `python scripts/gen-appendix-a.py` to write `docs/appendix-a-unit-tests.md` from the test reports
- **Integration tests:** five system test cases covering GUI action, successful API call, failed API response, end-to-end logic and edge case, in `docs/integration-tests.md` (report Section 5.3)
- **CI:** GitHub Actions runs `mvn -B verify` on every pull request and on every push to `main`, and uploads the JaCoCo report and the JAR

## 8. Project structure

```
src/main/java/cwrm/
  model/               domain classes (PickupRequest + Builder, Resident, RewardAccount, ...)
  service/             PickupService, RewardService, reward rules, StatisticsService
  api/                 Nominatim address lookup (HttpTransport, Jackson)
  controller/          PickupController, Outcome, BookingForm, StatisticsSummary
  view/                Swing panels and MainFrame
  Main.java            entry point and seed data
src/test/java/cwrm/    JUnit 5 tests (same packages)
docs/integration-tests.md  integration test cases TC-01 to TC-05
scripts/gen-appendix-a.py  builds the Appendix A unit-test table
config/checkstyle.xml  clean-code rules
.github/workflows/     GitHub Actions CI
pom.xml
README.md
```

## 9. Team

| Name | Student ID | Main responsibility |
|---|---|---|
| `<member 1>` | `<ID>` | `<area>` |
| `<member 2>` | `<ID>` | `<area>` |
| `<member 3>` | `<ID>` | `<area>` |
| `<member 4>` | `<ID>` | `<area>` |

## 10. AI use declaration

AI use in the written report is declared in Appendix B of the report.

## 11. References

Full APA 7th edition references are in the technical report (Section 7).
