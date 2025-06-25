# LaTeX Compilation Service – Integration Guide

This project relies on the public **Ytotech LaTeX compilation API** to convert raw LaTeX templates into finished PDF resumes.

The service is language-agnostic: any HTTP-capable runtime (Java, Python, Go, …) can call it as long as the request is formatted correctly.

---

## 1. Endpoint

```
POST https://latex.ytotech.com/builds/sync
```
* `sync` = request blocks until compilation completes (usually < 5 s for a resume).
* A 200 response contains a JSON body with the compiled PDF (Base64-encoded) plus logs; non-2xx indicates compilation failure or bad request.

---

## 2. Request Body (JSON)

```jsonc
{
  "resources": [           // Optional extra files (images, .cls, .sty …)
    {
      "path": "professional-resume.tex", // File name as seen by LaTeX
      "content": "…Base64-encoded string…", // Raw file bytes, Base64
      "encoding": "base64"                  // Only supported value today
    }
    // Additional resources may follow
  ],

  "main": "professional-resume.tex",     // The file LaTeX will compile
  "inputs": {},                           // Arbitrary key/value; ignored by service but useful to carry meta
  "compiler": "xelatex",                 // Recommended for modern fonts; pdfLaTeX also allowed
  "compilerOptions": ["-interaction=nonstopmode", "-halt-on-error"],
  "timeout": 40                           // Seconds before the job is killed
}
```

### Minimal Example (Java ‑ using `RestTemplate`)
```java
HttpHeaders h = new HttpHeaders();
h.setContentType(MediaType.APPLICATION_JSON);
String tex = Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of("professional-resume.tex")));
Map<String,Object> body = Map.of(
    "resources", List.of(Map.of("path","professional-resume.tex","content",tex,"encoding","base64")),
    "main", "professional-resume.tex",
    "compiler", "xelatex"
);
ResponseEntity<String> resp = restTemplate.postForEntity("https://latex.ytotech.com/builds/sync", new HttpEntity<>(body,h), String.class);
```

---

## 3. Response Body

```jsonc
{
  "id": "e0fd…",            // Build identifier
  "status": "success",      // success | error
  "outputFiles": [           // Present when success == true
    {
      "path": "output.pdf",  // Always output.pdf
      "content": "…Base64…",
      "encoding": "base64"
    }
  ],
  "log": "Full compiler log …" // Always returned, even on failure
}
```
Decode the first (and only) `outputFiles[].content` value to obtain the PDF bytes.

---

## 4. Important Constraints / Notes

1. **File paths are virtual.** Everything must live under the virtual root (`resources[].path`). Use Linux-style forward slashes.
2. **Base64 only.** Both request and response resources are Base64-encoded; set the `encoding` field **exactly** to `"base64"`.
3. **Size limits.** Max request payload ≈ 10 MB, compiled PDF ≤ 2 MB. Keep graphics compressed.
4. **Timeout.** Jobs exceeding the `timeout` setting (default 40 s, max 120 s) are aborted with status `error`.
5. **Deterministic output names.** The compiled PDF is always returned as `output.pdf`; ignore other paths.
6. **Logs are gold.** Always parse `log`; LaTeX errors are sent there, _not_ in `status`.
7. **No state.** Each call is standalone; the service does not persist files between builds.
8. **Rate limiting.** Public endpoint allows ~30 builds/minute per IP. Cache PDFs or host a private compiler for heavy traffic.

---

## 5. Using From Other Languages

* **Python (requests)**
  ```python
  import base64, json, requests, pathlib
  tex_b64 = base64.b64encode(pathlib.Path('professional-resume.tex').read_bytes()).decode()
  payload = {
      "resources": [{"path": "professional-resume.tex", "content": tex_b64, "encoding": "base64"}],
      "main": "professional-resume.tex",
      "compiler": "xelatex"
  }
  r = requests.post('https://latex.ytotech.com/builds/sync', json=payload, timeout=40)
  pdf = base64.b64decode(r.json()['outputFiles'][0]['content'])
  open('resume.pdf','wb').write(pdf)
  ```
* **cURL**
  ```bash
  base64 -w0 professional-resume.tex > tex.b64
  curl -X POST https://latex.ytotech.com/builds/sync \
       -H 'Content-Type: application/json' \
       -d '{"resources":[{"path":"professional-resume.tex","content":"'$(cat tex.b64)'","encoding":"base64"}],"main":"professional-resume.tex","compiler":"xelatex"}'
  ```

---

## 6. Troubleshooting Checklist

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| 400 Bad Request | JSON malformed / missing fields | Validate JSON with a linter; check commas & quotes |
| `status: error` & log shows `! LaTeX Error` | Template compilation problem | Inspect `log` field; reproduce locally with `latexmk` |
| Long build time, then timeout | Large images / infinite loop | Compress resources; increase `timeout` if really needed |
| PDF corrupt | Forgot `base64` decoding or wrong file index | Use first `outputFiles` entry and decode properly |

---

## 7. Keeping It Future-Proof

* Wrap service URL & options in a config file / env vars.
* Cache successful PDFs—resume content rarely changes.
* Consider self-hosting with [`github.com/ytotech/docker-latex`](https://github.com/ytotech/docker-latex) for unlimited builds.

---

© 2025 Resume Tracker – Feel free to copy / adapt this guide.
