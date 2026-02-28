# RecognitionApp — Session Notes

## What This App Is
A peer-to-peer employee recognition system where users give each other awards using a quarterly points bank. Recipients accumulate earned points and spend them in a company store.

## Points Rules
- Every user gets **20 giving points per quarter** (resets Jan 1, Apr 1, Jul 1, Oct 1 — no rollover)
- Giving points are spent FROM the giver's bank when they recognize a colleague
- Recipients EARN points, which they can redeem in the company store
- Spendable balance = total points earned (received) minus total points spent in store

---

## Tech Stack
| Layer | Technology |
|---|---|
| Language | Java 17 |
| Build | Maven (Spring Boot 3.2.3 parent) |
| Database | SQLite (file: `recognition.db`) |
| Backend API | Spring Boot (REST, port 8080) |
| Frontend | React 18 + Vite (port 5173) |
| Routing | React Router v6 |

---

## Database Schema (5 tables in recognition.db)

| Table | Purpose |
|---|---|
| `Users` | Employees — id, name, email, created_at |
| `AwardTypes` | Award catalogue — id, name, description, points_cost |
| `AwardsGiven` | Recognition events — giver_id, recipient_id, award_type_id, points, message, given_at |
| `StoreItems` | Company store — id, name, description, points_cost, quantity_available (NULL = unlimited) |
| `Purchases` | Store redemptions — user_id, store_item_id, points_spent, purchased_at |

---

## Java Source Files (`src/main/java/org/example/`)

| File | Role |
|---|---|
| `App.java` | Spring Boot entry point (`@SpringBootApplication`), calls `DatabaseInitializer.initialize()` on startup |
| `DatabaseInitializer.java` | Creates all 5 tables (IF NOT EXISTS), enables FK enforcement |
| `User.java` | Model |
| `AwardType.java` | Model |
| `AwardGiven.java` | Model |
| `AwardFeedItem.java` | DTO — joined feed item with giver/recipient/award type names |
| `StoreItem.java` | Model |
| `Purchase.java` | Model |
| `UserRepository.java` | CRUD for Users (`@Repository`) |
| `AwardTypeRepository.java` | CRUD for AwardTypes (`@Repository`) |
| `AwardGivenRepository.java` | Awards + quarterly points query + `getRecentFeed()` JOIN query (`@Repository`) |
| `StoreItemRepository.java` | Store items + stock decrement (`@Repository`) |
| `PurchaseRepository.java` | Purchase history + total spent (`@Repository`) |
| `PointsService.java` | Business logic — enforces giving bank limit, spendable balance, validates purchases (`@Service`) |
| `dto/PointsSummary.java` | Response DTO — givingAllowance, givingBalance, totalEarned, totalSpent, spendableBalance |
| `config/CorsConfig.java` | Allows CORS from `localhost:5173` (React dev server) |
| `controller/UserController.java` | `GET/POST/PUT /api/users` |
| `controller/AwardTypeController.java` | `GET/POST /api/award-types` |
| `controller/AwardController.java` | `GET /api/awards/feed`, `GET /api/awards/received/{id}`, `GET /api/awards/given/{id}`, `POST /api/awards` |
| `controller/PointsController.java` | `GET /api/points/{userId}` → PointsSummary |
| `controller/StoreController.java` | `GET/POST /api/store/items`, `POST /api/store/purchase`, `GET /api/store/purchases/{userId}` |

---

## REST API Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/users` | All users |
| POST | `/api/users` | Create user `{name, email}` |
| PUT | `/api/users/{id}` | Update user |
| GET | `/api/award-types` | All award types |
| POST | `/api/award-types` | Create award type `{name, description, pointsCost}` |
| GET | `/api/awards/feed?limit=50` | Recent recognitions (joined with names) |
| GET | `/api/awards/received/{userId}` | Awards a user received |
| GET | `/api/awards/given/{userId}` | Awards a user gave |
| POST | `/api/awards` | Give an award `{giverId, recipientId, awardTypeId, message}` |
| GET | `/api/points/{userId}` | Full points summary for a user |
| GET | `/api/store/items` | All store items |
| POST | `/api/store/items` | Add store item `{name, description, pointsCost, quantityAvailable}` |
| POST | `/api/store/purchase` | Purchase item `{userId, storeItemId}` |
| GET | `/api/store/purchases/{userId}` | Purchase history for a user |

---

## React Frontend (`frontend/`)

| File | Page/Purpose |
|---|---|
| `src/context/UserContext.jsx` | Global "current user" context — populates sidebar user switcher |
| `src/components/Navbar.jsx` | Fixed sidebar with nav links + "Viewing as" user dropdown |
| `src/api.js` | All fetch calls to the backend API |
| `src/pages/Feed.jsx` | Recognition feed (all recent awards with names, badges, messages) |
| `src/pages/Dashboard.jsx` | Per-user stats: giving balance, spendable balance, awards history, purchases |
| `src/pages/GiveAward.jsx` | Form: pick recipient, pick award type (grayed if unaffordable), add message |
| `src/pages/Store.jsx` | Store grid: shows items, redeem button, handles out-of-stock and insufficient balance |
| `src/pages/Admin.jsx` | Tabbed admin: Add/list Users, Award Types, and Store Items |

---

## How to Run

### Backend (Terminal 1)
```powershell
# From RecognitionApp/ folder
del recognition.db        # only needed if schema changed
mvn compile exec:java -Dexec.mainClass=org.example.App
```
API runs at `http://localhost:8080`

### Frontend (Terminal 2)
```powershell
# From RecognitionApp/frontend/ folder
npm install               # first time only
npm run dev
```
UI runs at `http://localhost:5173`

---

## Current State
- Backend: fully working (verified with mvn compile + exec)
- Frontend: all files written, **not yet run** — next session should start by running `npm install && npm run dev` and fixing any issues

---

## Possible Next Steps
- Fix any frontend startup issues (run `npm run dev` and check console)
- Add authentication / real login instead of the "Viewing as" switcher
- Add notifications (e.g. email or in-app when you receive an award)
- Reporting page (top givers, top recipients, most popular awards by quarter)
- Admin: edit/delete award types and store items
- Unit tests (JUnit for PointsService business logic)
