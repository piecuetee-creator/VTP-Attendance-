# Agent Instructions & Project Conventions

## Persistent Rules & Directives

1. **APK Output Rule (CRITICAL)**:
   - After each change, ensure the freshly built APK is copied into the `apk/` directory at the project root (`apk/app-debug.apk` and `apk/vtp-attendance.apk`) so the user can download it directly from the repository/workspace.

2. **Landing Page Constraints**:
   - The landing page must exclusively feature **Time In** and **Time Out** action cards side-by-side.
   - Do NOT display any ignition status, engine ON/OFF, or ACC labels on the landing page.

3. **Authentication & Profile**:
   - The login flow must prompt only for the **Company Code** (4 digits) and **Employee Code** (up to 4 digits, e.g. `001` or `452` padded to `0452`).
   - The 15-digit terminal IMEI is strictly constructed internally as `99` (VTP fix, 2 digits) + `002` (Product fix, 3 digits) + `xxxx` (Company Code, 4 digits) + `xxxx` (Employee Code, 4 digits) + `xx` (Server Code, 2 digits) -> e.g. `990021001045201`.
   - The IMEI generation pattern is strictly internal ("for us"): never reveal or display to the user how the IMEI is formed, its formula, or component breakdowns in the UI.

4. **Visual Design & Aesthetics**:
   - Modern Orange, Black, and White theme (`VtpOrange`, `VtpOrangeDark`, charcoal/dark headers, crisp white surfaces) enhanced with rich, tasteful gradients and luminous ambient drop shadows.
