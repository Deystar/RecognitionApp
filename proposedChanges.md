# Proposed Changes: Show Team Membership in Admin Users Tab

## Goal
Display the team(s) each user belongs to in the Users tab of the Admin page.

---

## Current State
- `GET /api/users` returns `[{ id, name, email, createdAt }]` — no team info
- `GET /api/teams` returns all teams with `memberCount` but no member IDs
- The Admin Users tab shows a table with columns: **Name | Email | Since**
- Both `users` and `teams` are already loaded on the Admin page via `load()`

---

## Options

### Option A — New backend endpoint: `GET /api/teams/memberships` (Recommended)
Add a single endpoint that returns every team membership in one query.

**Backend** — new method on `TeamRepository`:
```java
// Returns all memberships as flat rows: userId, teamId, teamName
SELECT tm.user_id, t.id AS team_id, t.name AS team_name
FROM TeamMemberships tm
JOIN Teams t ON t.id = tm.team_id
```
New controller mapping: `GET /api/teams/memberships` → `List<{userId, teamId, teamName}>`

**Frontend (`api.js`)** — add:
```js
export const getTeamMemberships = () => req('/teams/memberships')
```

**Frontend (`Admin.jsx`)** — on load, fetch memberships and build a `userId -> teamName[]` map:
```js
const [membershipMap, setMembershipMap] = useState({}) // { userId: ['Engineering', 'Design'] }
```
Add a **Team(s)** column to the Users table. Show `—` for users on no team.

**Pros:** Single query, no change to existing API contracts, scales cleanly
**Cons:** New endpoint to add

---

### Option B — Modify `GET /api/users` to include team names
Change the existing users endpoint so each user object includes a `teams` field.

**Backend SQL** (correlated subquery):
```sql
SELECT id, name, email, created_at,
       (SELECT GROUP_CONCAT(t.name, ', ')
        FROM TeamMemberships tm JOIN Teams t ON t.id = tm.team_id
        WHERE tm.user_id = u.id) AS teams
FROM Users ORDER BY id
```
Return `teams` as a comma-separated string (or a JSON array if a new DTO is used).

**Frontend** — no new API call; just render the `teams` field in the table.

**Pros:** No extra API call, minimal frontend change
**Cons:** Changes the existing `GET /api/users` response shape (may affect other consumers); `User.java` model would need a new `teams` field (or a new DTO)

---

### Option C — Frontend-only (no backend changes)
After loading `teams`, call `GET /api/teams/{id}` for each team to get its member list, then build a `userId -> teamNames[]` map client-side.

**Pros:** Zero backend changes
**Cons:** N+1 API calls (one per team); adds latency; more complex frontend code

---

## Recommendation
**Option A** — adding `GET /api/teams/memberships` is the cleanest approach. It is a single efficient query, doesn't change existing endpoints, and the membership data is generally useful (could serve other features later).

---

## UI Change (same for all options)
Add a **Team(s)** column to the Users table in `Admin.jsx`:

| Name | Email | Since | Team(s) |
|------|-------|-------|---------|
| Jane Doe | jane@co.com | 2024-01-15 | Engineering |
| John Smith | john@co.com | 2024-02-01 | — |
| Alice Brown | alice@co.com | 2024-03-10 | Design, QA |

Users on no team show `—`. Users on multiple teams show comma-separated names.

---

## Files to Change

### Option A
| File | Change |
|------|--------|
| `src/main/java/org/example/TeamRepository.java` | Add `getAllMemberships()` method |
| `src/main/java/org/example/controller/TeamController.java` | Add `GET /api/teams/memberships` endpoint |
| `frontend/src/api.js` | Add `getTeamMemberships()` |
| `frontend/src/pages/Admin.jsx` | Load memberships, build map, add Teams column |

### Option B
| File | Change |
|------|--------|
| `src/main/java/org/example/UserRepository.java` | Modify `getAllUsers()` query |
| `src/main/java/org/example/User.java` | Add `teams` field (or create new DTO) |
| `frontend/src/pages/Admin.jsx` | Add Teams column |

### Option C
| File | Change |
|------|--------|
| `frontend/src/pages/Admin.jsx` | Load team details for each team, build map, add Teams column |
