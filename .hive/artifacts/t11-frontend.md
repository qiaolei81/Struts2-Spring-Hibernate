# t11 Frontend Artifact — Equipment & Document Management UI

## Status: COMPLETE ✅

## Files Modified (2)

| File | Status | Description |
|------|--------|-------------|
| `frontend/src/pages/EquipManagement.tsx` | ✅ Implemented | Full CRUD + Excel export |
| `frontend/src/pages/DocManagement.tsx` | ✅ Implemented | Full CRUD + manual upload/download |

## Pages Fully Wired

### EquipManagement (`/equipment`)
- **Data table** via shared `DataTable` component — paginated, sortable, searchable
- **Columns**: Model (sortable), Name (sortable), Producer, Quantity (color-tagged), Description (ellipsis)
- **Add**: Modal form with Model (required), Name, Producer, Quantity, Description fields
- **Edit**: Pre-populated modal form via `ModalForm.initialValues`
- **Delete single**: Popconfirm on row action button
- **Batch delete**: Checkbox selection + "Delete Selected" toolbar button (disabled when none selected)
- **Export Excel**: "Export Excel" button → calls `GET /api/equipment/export` → triggers browser download via `downloadBlob`

### DocManagement (`/documents`)
- **Data table** via shared `DataTable` component — paginated, sortable, searchable
- **Columns**: Model (sortable), Name (sortable), Producer, Quantity (color-tagged), Manual (download link or upload button)
- **Add**: Modal form with Model (required), Name, Producer, Quantity fields
- **Edit**: Pre-populated modal form
- **Delete single / batch**: Same pattern as EquipManagement
- **Upload manual**: Ant Design `Upload` component in a modal → calls `POST /api/documents/{id}/manual` (multipart)
- **Download manual**: Filename rendered as a download link → calls `GET /api/documents/manual/{filename}`

## API Contracts Used

All contracts consumed from `src/api/modules.ts`:

```
GET    /api/equipment?page&size&sort&name   → PageResult<EquipDto>
POST   /api/equipment                       → EquipDto
PUT    /api/equipment/{id}                  → EquipDto
DELETE /api/equipment?ids=…                 → void
GET    /api/equipment/export                → blob (xls)

GET    /api/documents?page&size&sort&name   → PageResult<DocDto>
POST   /api/documents                       → DocDto
PUT    /api/documents/{id}                  → DocDto
DELETE /api/documents?ids=…                 → void
POST   /api/documents/{id}/manual           → DocDto (multipart)
GET    /api/documents/manual/{filename}     → blob
```

## Contract Fit Assessment

No mismatches detected. All types align with `src/types/index.ts`:
- `EquipDto` fields used: `id`, `model`, `name`, `producer`, `quantity`, `description`
- `DocDto` fields used: `id`, `model`, `name`, `producer`, `quantity`, `manualFilename`

## Build Verification

```
✓ tsc: 0 errors in t11 files
✓ vite build: EquipManagement-*.js (3.33 kB), DocManagement-*.js (4.70 kB)
```

Pre-existing TS errors in `UserStats.tsx` (recharts formatter types) are unrelated to this task.

## User Paths

| Path | Status |
|------|--------|
| View paginated equipment list | ✅ |
| Search equipment by name | ✅ |
| Add new equipment | ✅ |
| Edit existing equipment | ✅ |
| Delete single equipment | ✅ |
| Batch delete equipment | ✅ |
| Export equipment to Excel | ✅ |
| View paginated document list | ✅ |
| Search documents by name | ✅ |
| Add new document | ✅ |
| Edit existing document | ✅ |
| Delete single document | ✅ |
| Batch delete documents | ✅ |
| Upload equipment manual PDF/doc | ✅ |
| Download equipment manual | ✅ |
