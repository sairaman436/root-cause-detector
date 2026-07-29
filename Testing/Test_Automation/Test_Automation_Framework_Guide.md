# Test Automation Framework Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Test Automation Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Technical Framework Architecture & Developer Guide  

---

# Test Automation Framework Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Test Automation Framework Guide |
| Domain | Software Test Engineering |
| Version | 1.0 |
| Status | Approved |
| Owner | Automation QA Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This guide defines the architectural design, folder layout, design patterns, coding conventions, and setup procedures for developing and executing automated tests in the AI Rural Root Cause Discovery System test automation framework.

---

# Architecture & Tech Stack

```text
+-------------------------------------------------------------------------+
|                      Test Automation Orchestration                      |
+------------------------------------+------------------------------------+
                                     |
           +-------------------------+-------------------------+
           |                                                   |
           ▼                                                   ▼
+-------------------------------------+   +-------------------------------------+
|  Playwright E2E UI Framework (TS)   |   |  PyTest API Automation (Python)     |
|  - Page Object Model (POM) Pattern  |   |  - Requests & Pydantic Schema Check |
|  - Headless Chrome / Firefox / WebKit|   |  - Parallel execution via pytest-xdist|
+-------------------------------------+   +-------------------------------------+
           |                                                   |
           +-------------------------+-------------------------+
                                     |
                                     ▼
+-------------------------------------------------------------------------+
|                  Allure Unified Reporting & Artifact Storage            |
+-------------------------------------------------------------------------+
```

---

# Framework Design Patterns

### Page Object Model (POM)
All UI automation code must encapsulate page selectors and user interactions inside page classes residing in `tests/e2e/pages/`. Direct selector querying within test files is prohibited.

```typescript
// Example Page Object Class (TypeScript)
export class SurveyFormPage {
  readonly page: Page;
  readonly villageDropdown: Locator;
  readonly submitButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.villageDropdown = page.locator('#village-select');
    this.submitButton = page.locator('button[type="submit"]');
  }

  async fillSurvey(villageCode: string) {
    await this.villageDropdown.selectOption(villageCode);
    await this.submitButton.click();
  }
}
```

---

# Quickstart Setup & Execution

### Prerequisites
- Node.js ≥ 20.x
- Python ≥ 3.11

### Installation & Execution Commands

```bash
# Install UI Automation Dependencies
npm ci
npx playwright install --with-deps

# Run Headless End-to-End Tests
npx playwright test --config=playwright.config.ts

# Install & Run API Test Suite
pip install -r requirements-test.txt
pytest tests/api/ -n auto --alluredir=reports/allure-results

# Generate Allure HTML Report
allure generate reports/allure-results -o reports/html-report --clean
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| Test Automation Lead | Karen Davis | 2026-07-28 |
| QA Lead | David Miller | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Test Automation Framework Guide | Automation QA Team |

---

# End of Document
