# Feature Inventory — struts2-easyui-system
**Task:** t2 | **Role:** PM | **Status:** Complete

---

## Scope Assessment
[scope] Read-only feature inventory of a Struts2/Spring/Hibernate monolith (files: ~80 source files, subsystems: 7 functional modules + 3 cross-cutting concerns)

---

## System Summary

A Java EE web application (Struts2 + Spring + Hibernate + jQuery EasyUI) serving as an enterprise management system. The application runs on Oracle 10g and Tomcat 7, uses a single-page layout with tabbed navigation, and communicates between frontend and backend via JSON.

**Default credentials:** `admin` / `admin`

---

## Module Inventory

### 1. Authentication & Session Management

**Features:**
- Login via modal dialog (username + password)
- Self-registration via modal dialog (accessible from login screen)
- Logout (invalidates HTTP session)
- Session-based authentication (30-minute timeout)
- URL-based authorization enforced on all protected endpoints

**Business Rules:**
- Username `admin` cannot be registered by public self-registration
- The built-in `admin` account bypasses all URL-based authority checks (super-user)
- Passwords are stored as MD5 hashes
- On login, the session stores: userId, loginName, IP address, assigned roleIds, roleNames, authIds, authNames, authUrls

**User Flows:**
1. User opens app → Login modal appears automatically
2. User enters credentials → POST `/userAction!doNotNeedSession_login.action`
   - Success: modal closes, welcome bar shows `Welcome [username] [IP]`, online panel refreshes after 3 seconds
   - Failure: message shown, modal stays open
3. From login modal, user clicks "Register" → registration modal opens
   - POST `/userAction!doNotNeedSession_reg.action`
   - Success: registration modal closes
   - Failure: message shown
4. User clicks "Logout" → POST `/userAction!doNotNeedSession_logout.action` → login modal reopens

**Acceptance Criteria:**
- [ ] Login with valid credentials grants access and shows welcome bar
- [ ] Login with invalid credentials shows failure message, no session created
- [ ] Register creates a new user account; username must be unique
- [ ] Username `admin` cannot be registered via self-registration
- [ ] Logout destroys session; subsequent navigation shows login modal
- [ ] Session expires after 30 minutes of inactivity
- [ ] Non-admin users cannot access endpoints not in their authority URL list

---

### 2. User Management

**Features:**
- Paginated, sortable user list (columns: ID, username, password masked, created datetime, modified datetime, roles)
- Search by username (partial match)
- Add user (with optional role assignment via multi-select combobox)
- Edit user (name, password, roles)
- Delete user(s) (single or batch via checkbox selection)
- Batch role assignment for multiple selected users
- Context menu on row right-click (Add / Delete / Edit)

**Business Rules:**
- Username `admin` cannot be added via admin interface
- User with cid=`0` (admin) cannot be edited or deleted
- Deleting a user also deletes all user-role relationships
- User-role relationship is fully replaced on each save/update

**User Flows:**
1. Navigate to User Management → GET `/userAction!user.action` → paginated grid loads
2. Search: enter username → datagrid reloads with filter
3. Add: toolbar "Add" → dialog with form (username, password, role combobox) → POST `/userAction!add.action`
4. Edit: select one row → toolbar "Edit" → dialog pre-populated → POST `/userAction!edit.action`
5. Delete: check rows → toolbar "Delete" → confirm dialog → POST `/userAction!delete.action`
6. Batch role edit: select multiple rows → "Batch edit role" → dialog with role combobox → POST `/userAction!roleEdit.action`

**Acceptance Criteria:**
- [ ] User list loads with pagination (default 10 per page; options: 10, 20, 30, 40)
- [ ] List is sortable by username column
- [ ] Search filters list by username (partial match)
- [ ] Add user creates record with hashed password and role assignments
- [ ] Username `admin` cannot be added via the admin interface
- [ ] Edit updates user name, password, and roles
- [ ] Admin user (cid=0) cannot be edited or deleted
- [ ] Delete removes user(s) and their role relationships; requires confirmation
- [ ] Batch role edit applies the selected role to all chosen users
- [ ] Password is displayed as `******` in the grid

---

### 3. Role Management

**Features:**
- Paginated, sortable role list (columns: ID, role name, description, authority names)
- Add role with authority tree selection (combotree, multi-select, hierarchical)
- Edit role
- Delete role(s)
- Role combobox for use in user forms

**User Flows:**
1. Navigate to Role Management → GET `/roleAction!role.action` → paginated grid loads
2. Add: toolbar "Add" → dialog with name, description, authority combotree → POST `/roleAction!add.action`
3. Edit: select one row → toolbar "Edit" → dialog pre-populated → POST `/roleAction!edit.action`
4. Delete: check rows → "Delete" → confirm → POST `/roleAction!delete.action`

**Acceptance Criteria:**
- [ ] Role list loads with pagination and sorting
- [ ] Add role with name, optional description, and selected authorities
- [ ] Edit updates role name, description, and authority assignments
- [ ] Delete removes role(s); requires confirmation
- [ ] Authority selection uses hierarchical combotree (multi-select)
- [ ] Assigned authority names visible in role list

---

### 4. Authority (Permission) Management

**Features:**
- Hierarchical treegrid display of authorities (self-referential tree)
- Expand/Collapse all nodes
- Add authority (name, URL, description, sequence number, parent authority)
- Edit authority
- Delete authority (single node)
- Tree view endpoint for combotree selection (used by role management)
- Context menu on row right-click (Add / Delete / Edit)

**User Flows:**
1. Navigate to Authority Management → GET `/authAction!auth.action` → treegrid loads
2. Add: toolbar "Add" → dialog (name, URL, description, sequence, parent dropdown) → POST `/authAction!add.action`
3. Edit: select node → toolbar "Edit" → dialog pre-populated → POST `/authAction!edit.action`
4. Delete: select node → "Delete" → confirm → POST `/authAction!delete.action`

**Acceptance Criteria:**
- [ ] Authority tree displays hierarchically; all nodes expandable/collapsible
- [ ] Add authority with name (required), URL, description, sequence, and optional parent
- [ ] Edit updates authority fields
- [ ] Delete removes the selected authority node; requires confirmation
- [ ] Authority tree available as combotree in role management forms
- [ ] URL field is used for access control enforcement

---

### 5. Equipment Management

**Features:**
- Paginated, sortable equipment list (columns: ID, model, name, producer, quantity, description)
- Search by name (partial match)
- Add equipment
- Edit equipment
- Delete equipment(s)
- Export full equipment list to Excel file (Apache POI, `.xls` format)
- Context menu on row right-click

**User Flows:**
1. Navigate to Equipment Management → GET `/equipAction!equip.action` → grid loads
2. Search: enter name → datagrid reloads
3. Add: "Add" → dialog → POST `/equipAction!add.action`
4. Edit: select one → "Edit" → dialog → POST `/equipAction!edit.action`
5. Delete: check rows → "Delete" → confirm → POST `/equipAction!delete.action`
6. Export: click "Export to Excel" → GET `/equipAction!exportToExcel.action` → `.xls` file downloaded

**Acceptance Criteria:**
- [ ] Equipment list loads with pagination and sorting
- [ ] Search filters by equipment name (partial match)
- [ ] Add equipment with model (required), name, producer, quantity, description
- [ ] Edit updates all equipment fields
- [ ] Delete removes equipment record(s); requires confirmation
- [ ] Export generates a valid `.xls` Excel file containing all equipment records
- [ ] Excel file has styled header row and data rows

---

### 6. Document Management

**Features:**
- Paginated, sortable document list (columns: ID, model, name, producer, quantity, manual link)
- Search by name (partial match)
- Add document
- Edit document
- Delete document(s)
- Upload manual file (file linked to a document record)
- Manual field displayed as a clickable download link when present
- Context menu on row right-click

**User Flows:**
1. Navigate to Document Management → GET `/docAction!doc.action` → grid loads
2. Add: "Add" → dialog → POST `/docAction!add.action`
3. Edit: select one → "Edit" → dialog → POST `/docAction!edit.action`
4. Delete: check rows → "Delete" → confirm → POST `/docAction!delete.action`
5. Upload manual: select one document → "Upload Manual" → file picker dialog → POST `/docAction!upload.action`
   - File saved to `/upload/` directory on server
   - Document record updated with filename

**Acceptance Criteria:**
- [ ] Document list loads with pagination and sorting
- [ ] Search filters by document name (partial match)
- [ ] Add document with model (required), name, producer, quantity
- [ ] Edit updates all document fields
- [ ] Delete removes document record(s); requires confirmation
- [ ] Upload manual attaches a file to a document; filename stored in record
- [ ] Manual appears as a clickable download link in the grid
- [ ] File upload supports files up to 100 MB
- [ ] Spaces removed from uploaded filenames; existing file overwritten if same name

---

### 7. Access Log Viewer

**Features:**
- Paginated, sortable log list (columns: ID, username, IP, timestamp, message)
- Search by username (partial match)
- Logs are read-only (no add/edit/delete from UI)

**What is automatically logged (AOP):**
- Login attempt: records username, IP, datetime, "Log: Login successfully" or "Log: Login unsuccessfully"
- User registration attempt: records username, IP, datetime, "Log: Register successfully" or "Log: Register unsuccessfully"

**User Flows:**
1. Navigate to Log → GET `/logAction!log.action` → log grid loads
2. Search by username → filtered results

**Acceptance Criteria:**
- [ ] Log list loads with pagination and sorting (default sort: datetime asc)
- [ ] Search filters by username (partial match)
- [ ] Each login attempt (success and failure) generates a log entry with username, IP, timestamp, message
- [ ] Each registration attempt generates a log entry with username, IP, timestamp, message
- [ ] Log entries are read-only; no modification from UI

---

### 8. User Statistics Chart

**Features:**
- 3D bar chart: "User Type Statistics" — number of users per role
- X-axis: role names; Y-axis: user count
- Chart generated server-side as JPEG (JFreeChart) and served as an image

**User Flows:**
1. Navigate to chart → GET `/userAction!chart.action`
2. Chart page displays the generated bar chart image

**Acceptance Criteria:**
- [ ] Chart displays count of users grouped by role name
- [ ] Chart renders as a 3D bar chart with labeled axes and value labels on bars
- [ ] Chart image is a valid JPEG

---

### 9. Online Users Monitor

**Features:**
- Right-side panel showing currently logged-in users
- Columns: username, IP address, login time
- Panel title shows total online user count
- Refresh button on panel
- Auto-refreshes 3 seconds after a successful login
- Calendar widget in east panel

**Behavior:**
- On login: user entry created or updated (upsert by loginName + IP)
- On logout/session expiry: user entry removed

**Acceptance Criteria:**
- [ ] Online panel shows all currently logged-in users (name, IP, time)
- [ ] Count in panel title matches displayed rows
- [ ] Panel refreshes 3 seconds after login
- [ ] User entry appears in panel after successful login
- [ ] User entry disappears from panel after logout

---

### 10. Navigation Menu

**Features:**
- Left-side accordion menu tree loaded from TMENU database table
- Hierarchical tree (self-referential: parent-child)
- Click on menu item opens content in a new tab in center area
- Double-click toggles expand/collapse
- Tabs have: refresh, close, close-other, close-all options (via right-click context menu)
- Tab is reused if already open (no duplicate tabs)

**Acceptance Criteria:**
- [ ] Menu tree loads from database on page load
- [ ] All tree nodes expand on initial load
- [ ] Clicking a node with a URL opens the target page in a center tab
- [ ] Clicking a node already open selects the existing tab (no duplicate)
- [ ] Double-click on node toggles expand/collapse
- [ ] Tab right-click menu: Refresh, Close, Close Other, Close All

---

## Cross-Cutting Concerns

### Security / Access Control
- All endpoints protected by session check; methods prefixed `doNotNeedSession_*` are public
- Non-admin users checked against a URL whitelist stored in their session (`authUrls`)
- Admin user (`loginName == "admin"`) bypasses authority check entirely
- Unauthorized access returns HTTP 404 page (not 403)

### Error Pages
| Condition | Page |
|---|---|
| No session | `/error/404.jsp` |
| No authority | `/error/404.jsp` |
| HTTP 404 | `/error/404.jsp` |
| HTTP 500 | `/error/404.jsp` |
| Struts exception | `/error/strutsException.jsp` |

### Database Repair (Startup Utility)
- `RepairListener` calls `repairService.repair()` automatically on every server startup
- Endpoints: `/repairAction!doNotNeedSession_repairAction.action` (repair), `/repairAction!doNotNeedSession_deleteAndRepair.action` (delete + repair)
- These are not exposed in the UI navigation; they are internal utility endpoints

---

## Domain Context

This is an **electronics engineering lab management system**. The two content modules have a specific domain meaning:

- **Equipment catalog** = physical test & measurement instruments (Oscilloscopes, Function Generators, Logic Analyzers, Multimeters, Power Supplies/Modules). Producers are instrument vendors (Tektronix, Agilent).
- **Document catalog** = electronic component datasheets stored as PDFs. Producers are IC manufacturers (Texas Instruments, STMicroelectronics, Microchip).

This domain context drives several field-level requirements:
- `cmodel` is a **part number / model number** (e.g. `DPO70404C`, `TMS320C6678`) — alphanumeric, no spaces
- `cdesc` for equipment = **technical specification string** (e.g. `"4 GHz Bandwidth, 25 GS/s Sample Rate, 4 Channels"`) — minimum 100-char field
- `cmanual` for documents = **PDF datasheet filename** — the upload/download feature is specifically for PDF files
- `cno` = **physical unit count** (integer), representing inventory quantity of instruments or component stock

The seed data (23 equipment items, 16 documents) runs on every server startup via `RepairListener`. The rewrite must include equivalent seed/migration scripts to reproduce this demo state.

---

## Data Model Summary

| Table | Key Fields | Domain Meaning |
|---|---|---|
| TUSER | cid (PK), cname (unique, required), cpwd (required, MD5), ccreatedatetime, cmodifydatetime | System users |
| TROLE | cid (PK), cname (required), cdesc | User roles (e.g. Engineer, Admin) |
| TAUTH | cid (PK), cname (required), cdesc, cseq, curl, cpid (FK→TAUTH) | URL-level permissions, hierarchical |
| TUSERTROLE | cid (PK), cuserid (FK→TUSER), croleid (FK→TROLE) | User–Role join |
| TROLETAUTH | cid (PK), cauthid (FK→TAUTH), croleid (FK→TROLE) | Role–Authority join |
| TEQUIP | cid (PK), cmodel (required, part#), cname (category), cproducer (vendor), cno (qty), cdesc (specs) | Lab test instruments |
| TDOC | cid (PK), cmodel (required, part#), cname (component type), cproducer (IC maker), cno (qty), cmanual (PDF filename) | Component datasheets |
| TLOG | cid (PK), cname (required), cip (required), cdatetime, cmsg | Access audit log |
| TMENU | cid (PK), cname (required), curl, ciconcls, cseq, cpid (FK→TMENU) | Navigation tree |
| TONLINE | cid (PK), cname (required), cip (required), cdatetime (required) | Active sessions |

---

## Complete Endpoint Reference

### Endpoints — no session required
| Endpoint | Method | Description |
|---|---|---|
| `/userAction!doNotNeedSession_login.action` | POST | Login |
| `/userAction!doNotNeedSession_reg.action` | POST | Register |
| `/userAction!doNotNeedSession_logout.action` | POST | Logout |
| `/userAction!doNotNeedSession_combobox.action` | GET | Users combobox data |
| `/userAction!doNotNeedSession_datagrid.action` | GET | Users datagrid (no auth) |
| `/authAction!doNotNeedSession_tree.action` | GET | Auth tree (flat) |
| `/authAction!doNotNeedSession_treeRecursive.action` | GET | Auth tree (recursive) |
| `/roleAction!doNotNeedSession_combobox.action` | GET | Roles combobox data |
| `/menuAction!doNotNeedSession_tree.action` | GET | Menu tree (flat) |
| `/menuAction!doNotNeedSession_treeRecursive.action` | GET | Menu tree (recursive) |
| `/onlineAction!doNotNeedSession_onlineDatagrid.action` | GET | Online users list |
| `/repairAction!doNotNeedSession_repairAction.action` | GET | Repair DB |
| `/repairAction!doNotNeedSession_deleteAndRepair.action` | GET | Delete + repair DB |

### Endpoints — session + authority required
| Endpoint | Method | Description |
|---|---|---|
| `/userAction!user.action` | GET | User management page |
| `/userAction!userAdd.action` | GET | User add form |
| `/userAction!userEdit.action` | GET | User edit form |
| `/userAction!userRoleEdit.action` | GET | Batch role edit form |
| `/userAction!chart.action` | GET | User statistics chart |
| `/userAction!add.action` | POST | Add user |
| `/userAction!edit.action` | POST | Edit user |
| `/userAction!delete.action` | POST | Delete user(s) |
| `/userAction!roleEdit.action` | POST | Batch role edit |
| `/userAction!datagrid.action` | GET | Users datagrid |
| `/roleAction!role.action` | GET | Role management page |
| `/roleAction!roleAdd.action` | GET | Role add form |
| `/roleAction!roleEdit.action` | GET | Role edit form |
| `/roleAction!add.action` | POST | Add role |
| `/roleAction!edit.action` | POST | Edit role |
| `/roleAction!delete.action` | POST | Delete role(s) |
| `/roleAction!datagrid.action` | GET | Roles datagrid |
| `/authAction!auth.action` | GET | Authority management page |
| `/authAction!authAdd.action` | GET | Auth add form |
| `/authAction!authEdit.action` | GET | Auth edit form |
| `/authAction!add.action` | POST | Add authority |
| `/authAction!edit.action` | POST | Edit authority |
| `/authAction!delete.action` | POST | Delete authority |
| `/authAction!treegrid.action` | GET | Auth treegrid |
| `/equipAction!equip.action` | GET | Equipment management page |
| `/equipAction!equipAdd.action` | GET | Equipment add form |
| `/equipAction!equipEdit.action` | GET | Equipment edit form |
| `/equipAction!add.action` | POST | Add equipment |
| `/equipAction!edit.action` | POST | Edit equipment |
| `/equipAction!delete.action` | POST | Delete equipment(s) |
| `/equipAction!datagrid.action` | GET | Equipment datagrid |
| `/equipAction!exportToExcel.action` | GET | Export equipment to Excel |
| `/docAction!doc.action` | GET | Document management page |
| `/docAction!docAdd.action` | GET | Document add form |
| `/docAction!docEdit.action` | GET | Document edit form |
| `/docAction!docUpload.action` | GET | Manual upload form |
| `/docAction!add.action` | POST | Add document |
| `/docAction!edit.action` | POST | Edit document |
| `/docAction!delete.action` | POST | Delete document(s) |
| `/docAction!datagrid.action` | GET | Documents datagrid |
| `/docAction!upload.action` | POST | Upload manual file |
| `/logAction!log.action` | GET | Access log page |
| `/logAction!datagrid.action` | GET | Log datagrid |
| `/menuAction!treegrid.action` | GET | Menu treegrid |

---

## What Must Be Preserved (Parity Requirements)

1. All 7 management modules with full CRUD as documented
2. URL-based RBAC: admin super-user bypass, URL whitelist per user session
3. AOP access logging for login and registration events
4. Equipment Excel export (`.xls` format, styled headers)
5. Document manual file upload and download (files served from `/upload/`)
6. User statistics 3D bar chart grouped by role
7. Online users monitor (real-time session tracking)
8. Navigation menu loaded from database
9. Protected `admin` account (cannot edit/delete, cannot register duplicate)
10. MD5 password hashing on all password storage
11. Paginated datagrids with server-side sorting and filtering

---

## Open Items / Unverified Behaviors

[notify:tester] The following behaviors need explicit proof during t13 testing:

1. **Password encryption**: Verify MD5 is applied consistently (register, add user, login comparison)
2. **Excel export content**: Verify the exported file contains correct columns and all records (not just the current page)
3. **File upload overwrite**: Verify that uploading a file with an existing filename silently overwrites the old file
4. **Concurrent online tracking**: Verify the upsert logic for online users correctly handles the same user logging in from multiple IPs
5. **AOP logging scope**: Only `UserServiceImpl.login()` and `UserServiceImpl.save()` generate log entries — all other UserServiceImpl methods pass through without logging. Verify this is intentional.
6. **repairAction endpoints**: These have no authority check and no session check. Verify whether these need to be secured in the rewrite.
7. **Error page behavior**: Both noAuth and noSession redirect to `/error/404.jsp`. The rewrite should clarify if this is intentional or if distinct 401/403 responses are needed.
8. **Chart file cleanup**: Chart JPEG files are written to disk on each page load with timestamp-based names. Verify whether old chart files accumulate and whether cleanup is needed.

---

## Sign-Off Status

**NOT ready for sign-off** — this is the baseline inventory for t2. Sign-off occurs at t14 after rewrite verification.
