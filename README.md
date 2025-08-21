# PES Android App – UI/UX Revamp, Motion System, and Supabase-first Plan

**App:** Panaon Elementary School (PES)

**Goal:** Deliver a polished, accessible, offline‑first, multi‑tenant (per‑school) Android app with smooth motion, clear microcopy, and a secure foundation built directly on **Supabase**—ready to scale from 1 to 100+ schools. (Note: No Firebase integration exists in the current codebase, so we will build directly with Supabase instead of migrating.)

---

## Table of Contents

- [Product Principles & Success Metrics](#1-product-principles--success-metrics)
- [Information Architecture & Navigation](#2-information-architecture--navigation)
- [Design System](#3-design-system-material-3--compose)
- [Microcopy Guidelines](#4-microcopy-guidelines-labels-buttons-errors)
- [Motion & Animation System](#5-motion--animation-system)
- [Offline‑First Architecture](#6-offlinefirst-architecture--auto-sync)
- [Multi‑Tenant Scalability](#7-multitenant-per-school-scalability)
- [Supabase Backend Design](#8-supabasefirst-backend-design)
- [Security & Privacy](#9-security--privacy)
- [Performance & Observability](#10-performance--observability)
- [Testing Strategy](#11-testing-strategy)
- [CI/CD & Feature Flags](#12-cicd--feature-flags)
- [Implementation Roadmap](#13-implementation-roadmap-1012-weeks)
- [Example Code](#14-example-kotlin-pseudocode)
- [Risks & Mitigations](#15-risks--mitigations)
- [Deliverables](#16-deliverables-checklist)

---

## 1) Product Principles & Success Metrics

**Principles**

* **Clarity first:** Minimal taps to task completion, plain-language labels, visible status.
* **Trust & safety:** Explicit permissions by role, strong auth, transparent errors.
* **Offline-first:** Everything reads offline; edits queue and sync automatically.
* **Motion with purpose:** Animations inform state change, never distract.
* **Tenant-ready:** Per-school branding, data isolation, and flexible policies.

**North-star metrics**

* TTR (Time-To-Read) announcements < 3s on average network.
* Crash-free sessions ≥ 99.7%.
* Sync success rate ≥ 99% within 5 minutes of connectivity return.
* First meaningful paint ≤ 1.5s on mid-tier devices.
* Accessibility: 100% screens TalkBack-labeled, 48dp targets, WCAG AA contrast.

---

## 2) Information Architecture & Navigation

**Bottom navigation (Material 3):**

* **Home (Announcements)**
* **Events**
* **Messages (optional, future)**
* **Financials** *(role-restricted)*
* **Profile**

**Role-specific surfaces**

* **Guest:** Public announcements, events, school profile.
* **Parent:** Above + SPTA info, child records, messages.
* **Teacher:** Above + class sections, grading (future), post announcements/events.
* **Admin:** Above + admin console (users, roles, school settings, reports).

**Key flows**

* **First-run:** Splash → Onboarding (school picker/QR) → Login/SSO → Role-based landing.
* **Add announcement (Teacher/Admin):** FAB → Compose sheet → Attachments → Preview → Post → Local cache → Sync.
* **Event RSVP (Parent):** Event details → CTA → RSVP → Offline queue → Sync.
* **Admin user management:** Admin Console → Users list (search, filter by role/school) → Invite/Change role.

**Deep links**

* `pes://school/{school_id}/announcement/{id}`
* `pes://school/{school_id}/event/{id}`

---

## 3) Design System (Material 3 + Compose)

**Branding (per school tenant)**

* **Theme tokens:** primary/secondary/tertiary, neutral, error; typography scale; shape (rounded-2xl default), elevation.
* **Dynamic Theming:** Remote theme payload from Supabase (per school) → DataStore cache → Compose `MaterialTheme`.
* **Dark mode:** Automatic support, with contrast checks.

**Core components**

* Buttons, Inputs, Lists & Cards, Chips, Dialogs & Sheets, Feedback banners/snackbars.
* Loading skeletons, Empty states with illustrations, Error states with retry actions.

**Accessibility**

* 48dp min hit targets, 8dp spacing, focus order, semantic roles, `contentDescription`, dynamic font scaling up to 200%.

---

## 4) Microcopy Guidelines (Labels, Buttons, Errors)

* Use short, action-oriented verbs. Avoid jargon. Filipino/English localization.
* Examples:

  * Buttons: **Post**, **Save draft**, **Try again**, **Mark as paid**, **RSVP**.
  * Empty states: "Walang anunsyo pa. Maging una sa pagbabahagi." / "No announcements yet. Be the first to post."
  * Errors: "Hindi maisave ngayon. Naka-queue para sa sync." / "Can't save now—queued for sync."
  * Offline banner: "Offline ka. Lahat ng pagbabago ay ise-sync kapag may internet."

---

## 5) Motion & Animation System

* **Informative, subtle, performant** animations.
* Screen transitions: fade-through; FAB → sheet with container transform; swipe-to-archive with spring.
* Pull-to-refresh: progress indicator using theme colors.
* Success: scale+fade tick; Error: shake on invalid input.

---

## 6) Offline‑First Architecture & Auto Sync

* **Local data:** Room/SQLDelight as source of truth with sync metadata.
* **Sync engine:** WorkManager jobs triggered on connectivity or intervals.
* **Conflict resolution:** Server wins with merge fields; conflict UI when needed.
* **Blob handling:** Upload media first (Supabase Storage), then link in records.

---

## 7) Multi‑Tenant (Per School) Scalability

* `schools` table; all domain data has `school_id` foreign key.
* **Branding:** themes/logos fetched per school.
* **User mapping:** `profiles` link auth users to roles per school.
* **RLS:** Row-Level Security ensures per-school isolation.
* Target: 100+ schools, 10k+ MAU.

---

## 8) Supabase-first Backend Design

**Supabase services used:**

* **Auth:** Supabase GoTrue (password, magic link, SSO).
* **Database:** Postgres with PostgREST & Realtime.
* **Storage:** Supabase Storage.
* **Edge Functions:** Custom logic in Deno/TypeScript.
* **Notifications:** FCM for push.
* **Analytics:** Firebase Analytics/Crashlytics or Sentry.

**Schema:** Tables for `schools`, `profiles`, `announcements`, `events`, `rsvps`, `financials` with RLS policies.

**Android integration:** Supabase Kotlin SDK (auth, postgrest, storage, realtime).

---

## 9) Security & Privacy

* Minimum PII collection; encryption at rest and in transit.
* RLS policies strictly enforce role/school permissions.
* Secrets managed securely (not in APK).
* Admins can export data per school.

---

## 10) Performance & Observability

* Startup optimized for <1.5s FMP.
* Images handled with Coil caching.
* Monitoring via Crashlytics/Sentry + custom sync metrics.
* Structured logs (school + user, no PII).

---

## 11) Testing Strategy

* Unit, UI (Compose), Integration (sync engine), RLS policy validation, Load testing.

---

## 12) CI/CD & Feature Flags

* GitHub Actions CI: lint, tests, builds.
* Play Store CD with staged rollout.
* Feature flags table in Supabase for controlled releases.

---

## 13) Implementation Roadmap (10–12 weeks)

**Phase 0:** UX audit, schema confirmation.  
**Phase 1:** Design system + navigation.  
**Phase 2:** Offline engine + announcements/events.  
**Phase 3:** Supabase integration (auth, DB, storage).  
**Phase 4:** Financials + admin console.  
**Phase 5:** Polish, accessibility QA, theming per school.

---

## 14) Example Kotlin Pseudocode

**Sync Work (Announcements)**

```kotlin
class SyncAnnouncementsWorker(
  appContext: Context,
  params: WorkerParameters,
  private val repo: AnnouncementsRepository,
) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result = runCatching {
    repo.pushPendingChanges()
    repo.pullServerUpdates()
    Result.success()
  }.getOrElse { Result.retry() }
}
```

**Compose: Offline Banner**

```kotlin
@Composable
fun OfflineBanner(isOffline: Boolean) {
  AnimatedVisibility(visible = isOffline) {
    Surface(tonalElevation = 2.dp) {
      Text(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        text = "Offline ka. Ise-sync ang mga pagbabago kapag may internet.",
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}
```

---

## 15) Risks & Mitigations

* **Auth onboarding friction:** Smooth with magic link/SSO.
* **RLS misconfig:** Policy tests + staging checks.
* **Sync conflicts:** Merge strategies + conflict UI.
* **Media costs:** Compression + lifecycle policies.
* **Device variability:** Optimize for low-end devices.

---

## 16) Deliverables Checklist

* Design tokens + Figma kit.
* Compose component library.
* Offline engine + sync system.
* Supabase schema, RLS, Edge Functions.
* Accessibility & performance report.
* Per-school theming + admin exports.

---

**Made with ❤️ for Panaon Elementary School**
