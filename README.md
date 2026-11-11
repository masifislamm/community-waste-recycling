# Community Waste and Recycling Manager (CWRM)

**Module:** CSC61204 Software Construction, Taylor's University
**Assignment:** Group Assignment, Scenario 3 (Community Waste and Recycling Manager)
**Group:** G5 · **Lecturer:** Dr. Reema Bin Thabit
**Repository:** `<GitHub URL: add before submission>`

A Java desktop application that lets residents book recycling and bulky-waste pickups, lets collectors log completed collections, and rewards residents with points for recycling. It supports **UN SDG 11, Target 11.6** (reducing the environmental impact of cities through better municipal waste management).

> **Status: work in progress.** This README describes the system as designed in our technical report. Items marked **[confirm]** must be checked against the final code before submission.

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

## 3. Design overview

**Architecture:** Model-View-Controller with a service layer and an external API layer.

```
Views (Swing)            Controller              Services                 Domain model / API
BookingPanel      ─┐
CollectorPanel    ─┼──>  PickupController ──>  PickupService  ──>  PickupRequest, Resident,
RewardsPanel      ─┘                           RewardService         Collector, CollectionLog,
                                               NominatimAddressLookup   Address, Material, TimeSlot
                                                      │
                                                      └──> HttpTransport ──> Nominatim API
```

**Design patterns**

| Pattern | Where | Why |
|---|---|---|
| **Builder** | `PickupRequest.Builder` | Pickup requests have 5 mandatory and 3 optional fields. The builder names each argument and validates the whole request in one place (Single Responsibility) |
| **Strategy** | `RewardRule` with `PerKgRule`, `PerMaterialRule`, `CampaignBonusRule` | New reward rules are added as new classes without editing `RewardService` (Open/Closed). Rules can be swapped at runtime |

**Key classes (see the CRC cards in the report, Section 3.4)**
`PickupRequest`, `RewardService`, `PickupService`, `NominatimAddressLookup`, `PickupController`

## 4. Technology stack

- Java **21** **[confirm]**
- Java Swing (GUI)
- `java.net.http.HttpClient` (HTTP) and JSON parsing **[confirm: report names Jackson, CRC cards name `MiniJson`]**
- JUnit 5 (unit tests), JaCoCo (coverage, target ≥ 75%)
- GitHub Actions (CI)
- Build tool: Maven or Gradle **[confirm]**

## 5. External API

- **Service:** OpenStreetMap Nominatim (address lookup)
- **Endpoint:** `https://nominatim.openstreetmap.org/search` **[confirm exact query used]**
- **Usage policy:** requests are rate limited and send an identifying `User-Agent` header, as the Nominatim usage policy requires
- **Error handling:** 2xx is parsed and displayed; 404 and 500 map to friendly messages (never a stack trace); if the API is unreachable the last known result is used

## 6. Getting started

### Prerequisites
- JDK 21 or later
- Internet access (for address lookup)

### Run the executable JAR
```bash
java -jar cwrm.jar        # [confirm final JAR name]
```

### Build and test from source
```bash
git clone <repository-url>
cd <repository-folder>

# If Maven [confirm]
mvn clean package         # compile, run tests, build JAR
mvn test                  # run unit tests only
mvn jacoco:report         # coverage report in target/site/jacoco/index.html

# If Gradle [confirm]
./gradlew build
./gradlew test jacocoTestReport
```

## 7. Testing

- **Unit tests (JUnit 5):** pickup-request validation, reward calculation, pickup lifecycle (book, cancel, complete, reject duplicates). Test names follow `methodName_scenario_expectedBehaviour`
- **Coverage:** JaCoCo, target 75% line coverage or above
- **Integration tests:** five system test cases covering GUI action, successful API call, failed API response, end-to-end logic and edge case (report Section 5.3)
- **CI:** GitHub Actions runs the build and tests on every pull request to `main`

## 8. Project structure **[confirm against the repo]**

```
src/main/java/...      domain model, services, controller, views, API client
src/test/java/...      JUnit 5 tests
.github/workflows/     GitHub Actions CI
docs/                  report diagrams (architecture, UML)
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

Any AI assistance used for code or documentation is declared in Appendix B of the report.

## 11. References

Full APA 7th edition references are in the technical report (Section 7).
