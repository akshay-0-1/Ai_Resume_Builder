# LaTeX Resume Generation Implementation Plan

## Overview
We're implementing LaTeX-based resume generation using the YtoTech LaTeX HTTP API (https://github.com/YtoTech/latex-on-http).

## API Requirements

### Request Format
The API requires a specific JSON format:
```json
{
    "compiler": "pdflatex",
    "resources": [
        {
            "main": true,
            "content": "..."
        }
    ]
}
```

### Key Points
1. **Compiler**: Must be specified as "pdflatex"
2. **Resources**: Array of resource objects
3. **Main Document**: Exactly one resource must have `main: true`
4. **Content**: The LaTeX source code

## Implementation Steps

### 1. Create LaTeX Template Service
- Create new service: `LatexTemplateService.java`
- Implement template loading from resources
- Implement placeholder replacement
- Add template validation

### 2. Create LaTeX API Client
- Create new client: `LatexApiClient.java`
- Implement proper request formatting
- Handle API errors
- Add retry mechanism

### 3. Update Resume Generation Flow
- Modify `ResumeService` to use LaTeX template
- Add template selection logic
- Implement placeholder replacement
- Handle PDF generation

## Dependencies
Add these dependencies to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

## Error Handling
1. Missing compilation specification
2. Invalid LaTeX syntax
3. API connection errors
4. Template loading failures

## Security Considerations
1. API key management
2. Input validation
3. Rate limiting
4. Error logging

## Testing Requirements
1. Unit tests for template service
2. Integration tests for API client
3. End-to-end tests for resume generation
4. Performance testing

## Next Steps
1. Implement basic template loading
2. Create API client with proper request format
3. Test with sample resume data
4. Add error handling and logging
5. Implement template customization
6. Add PDF preview functionality

---
##  Phase 2
## Edit Functionality Implementation Plan

### 1 · Back-End API Additions

1. **`GET /api/resumes/{id}/editable`**  
   Returns the complete resume in an editable DTO (reuse / extend `ResumeResponseDTO`).

2. **`PUT /api/resumes/{id}`**  
   Accepts the edited DTO, validates it, persists changes, and kicks off LaTeX regeneration if anything changed.

Key points
* Apply `@Valid` bean validation.  
* Handle child collection updates (add/update/delete) via `ResumeService.updateResume()`.
* Enforce `userId == resume.ownerId` for security.

### 2 · Front-End “Edit” View (new tab)

1. **Trigger:** The existing *Edit* button in preview performs `window.open('/edit-resume/{id}')`.
2. **Route:** `/edit-resume/:id` renders **EditResumePage**.
3. **Layout:**
   * Left (≈30%): vertical tabs or accordion with section forms (Personal, Education, Work, Projects, Certificates, Skills).
   * Right: live PDF preview (optional) or placeholder.
4. **Form:** Implement with Formik+Yup (React) or equivalent.
   * Initial values come from `GET /editable` payload.
   * Dynamic list controls for Work, Projects, etc. (add / remove rows).
5. **Actions:**
   | Button | Action |
   |--------|--------|
   | **Save** | `PUT /api/resumes/{id}` then close tab & refresh preview |
   | **Cancel** | Simply close tab |
   | **Generate PDF** | Optional manual compile trigger |

### 3 · Database Notes

* Keep order of list items with `@OrderColumn` if needed.
* Use `orphanRemoval = true` to cascade deletes when items are removed on edit.


### 4 · Security & Validation

* Reuse JWT filters.  
* Server-side validation mirrors front-end Yup schema to avoid mismatches.

### 5 · Milestone Checklist

- [ ] DTOs include IDs for existing child entities.
- [ ] Implement the two REST endpoints with unit tests.
- [ ] Build EditResumePage skeleton.
- [ ] Connect forms → API, add validation.
- [ ] Verify LaTeX regeneration and PDF download shows updates.
- [ ] (Optional) Add websocket or polling for “PDF ready” notifications.

---

## Phase 0 – Hot-Fix (Stabilise Preview, Download & Edit)

> **Deadline:** ASAP – same sprint *(25 Jun 2025)*

### Confirmed Root-Causes
1. React issues `/download` immediately – backend hasn’t stored `latexPdf` yet ⇒ 404.
2. LaTeX compilation errors are logged but never surfaced, so UI only sees 404.
3. No status endpoint, therefore no safe polling; edit flow suffers the same race.

### Hot-Fix Checklist
- **Backend – Status API**  
  `GET /api/resumes/{id}/status` → `{ status, failureMessage? }`  
  Codes: `200` when `PDF_GENERATED`, `202` while processing, `409` on `FAILED`.
- **Backend – Download Endpoint**  
  When PDF missing → `202 Accepted` + `Retry-After: 3`; when failed → `409` + log.
- **Backend – Exception⇢HTTP Mapper**  
  Map `LatexCompilationException` ⇒ `409` with 1 KB log excerpt.
- **Frontend – Poll Until Ready**  
  React hook (or React Query) polls `/status` every 3 s, then calls `/download`.
- **Frontend – User Feedback**  
  Spinner while compiling; toast showing `failureMessage` on error.
- **Retry Logic**  
  Wrap `LatexApiClient.compileLatex()` in 3× retry (1 s → 2 s → 4 s).

**Acceptance**
1. Upload resume → progress indicator updates → preview renders when ready.
2. Edit resume → save → new PDF generated & downloadable.
3. Inject LaTeX error → UI shows clear compilation message.

---

## Phase 3 – Preview / Edit UX Polish

### Objectives
* Real-time feedback; no broken previews.
* Efficient bandwidth via caching.

### Tasks
1. **Server-Sent Events (SSE)** – push `PDF_GENERATED` event; client unsubscribes after receipt.
2. **Debounced autosave** in edit view; compile only on explicit click or 5 s idle.
3. **ETag caching** – serve `ETag` headers on `/download`; frontend adds `If-None-Match`.
4. **Template versioning** – store `templateVersion`; skip compile when unchanged.
5. **E2E tests** – Cypress flow from upload → edit → preview → download.

### Deliverables
- Updated API docs in `README.md` & Swagger.
- Cypress scripts under `/frontend/e2e`.
- Architecture diagram in `/docs/diagrams/latex-flow.drawio`.

---
