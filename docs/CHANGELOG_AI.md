# VUGA TV AI Change Log

This is the running change log maintained by AI assistants and developers. Append new entries at the end of the file. Do not modify previous entries except to correct typos.

## Entry Template

```markdown
### [Month Year]
- **[Feature/Fix Name]**: One-sentence summary
- **Description**: What changed and why
- **Affected Platforms**: iOS / Android Mobile / Android TV / Admin
- **Endpoints Affected**: `/api/v2/...` (list all)
- **Data Model Changes**:
  - Added fields: [...]
  - Nullable fields: [...]
  - Removed fields: None (required)
  - Type changes: None (required)
- **Compatibility Strategy**:
  - Backward compatible: Yes/No
  - If No: New versioned endpoint: `/api/v3/...` and migration notes
- **Client Updates Required**:
  - iOS: Yes/No — [summary]
  - Android Mobile: Yes/No — [summary]
  - Android TV: Yes/No — [summary]
  - Admin: Yes/No — [summary]
- **Testing Performed**:
  - API: [tests/scripts run]
  - iOS: [areas tested]
  - Android Mobile: [areas tested]
  - Android TV: [areas tested]
- **Breaking Changes**: None OR [describe migration needed]
- **Rollout/Deployment Notes**: [feature flag, cache clear, migrations, etc.]
```

---

<!-- Append new entries below this line -->

### August 2025
- **Profile Avatar Display & Initials Fix**: Fixed profile photo display and corrected initials generation
- **Description**: Fixed Edit Profile page showing color avatar instead of uploaded photo when avatar_type is "custom". Also fixed profile initials to show first letter of up to 2 words instead of only first letter.
- **Affected Platforms**: Android Mobile
- **Endpoints Affected**: `/api/v2/profile/update` (read by client, no API changes)
- **Data Model Changes**:
  - Added fields: None
  - Nullable fields: None
  - Removed fields: None
  - Type changes: None
- **Compatibility Strategy**:
  - Backward compatible: Yes
  - Client-side fix only, no API changes required
- **Client Updates Required**:
  - iOS: No — Already handles avatar display correctly
  - Android Mobile: Yes — Updated ProfileActivity, ProfileAdapter, and CreateProfileActivity
  - Android TV: No — Not affected
  - Admin: No — Not affected
- **Testing Performed**:
  - API: No changes made
  - iOS: Not tested (no changes)
  - Android Mobile: Tested on Samsung device - profile upload, display, and initials generation
  - Android TV: Not tested (no changes)
- **Breaking Changes**: None
- **Rollout/Deployment Notes**: Android app APK needs to be rebuilt and deployed to users

### August 2025 (S3 Upload)
- **Profile Avatar S3 Upload**: Android mobile app now uploads profile photos to Digital Ocean Spaces
- **Description**: Changed Android app from local server file upload to S3 upload via base64 encoding, matching iOS implementation. ProfileAvatarController fixed to use correct model and format responses properly.
- **Affected Platforms**: Android Mobile / Backend API
- **Endpoints Affected**: `/api/v2/profiles/avatar/upload` (fixed model reference)
- **Data Model Changes**:
  - Added fields: None
  - Nullable fields: None
  - Removed fields: None
  - Type changes: None
- **Compatibility Strategy**:
  - Backward compatible: Yes
  - Existing endpoints unchanged, fixed implementation only
- **Client Updates Required**:
  - iOS: No — Already using this endpoint
  - Android Mobile: Yes — Changed from multipart to base64 upload
  - Android TV: No — Benefits from S3 storage for avatar display
  - Admin: No — Not affected
- **Testing Performed**:
  - API: Fixed ProfileAvatarController model references
  - iOS: Not tested (no changes)
  - Android Mobile: Built and deployed to Samsung device
  - Android TV: Not tested (no changes)
- **Breaking Changes**: None
- **Rollout/Deployment Notes**: Deploy ProfileAvatarController.php to production server. Environment variables for Digital Ocean Spaces already configured.

### August 2024 (Session 2)
- **iOS Profile Management & Avatar Color Handling**: Fixed profile editing endpoint and null avatar_color handling
- **Description**: Changed iOS to use correct `/profile/update` endpoint instead of `/updateProfile`. Made avatar_color optional in iOS Profile model to handle null values from API. Added proper null safety throughout iOS app.
- **Affected Platforms**: iOS / Backend API
- **Endpoints Affected**: 
  - `/api/v2/profile/update` (iOS now uses this correctly)
  - `/api/v2/getUserProfiles` (returns profiles with nullable avatar_color)
- **Data Model Changes**:
  - Added fields: `avatar_type` and `avatar_color` parameters to ProfileController
  - Nullable fields: `avatar_color` (now optional in iOS, still required in Android)
  - Removed fields: None
  - Type changes: None
- **Compatibility Strategy**:
  - Backward compatible: Yes
  - API continues returning avatar_color for Android compatibility
  - iOS adapted to handle optional avatar_color
- **Client Updates Required**:
  - iOS: Yes — Profile model updated, API endpoint corrected, null safety added
  - Android Mobile: No — Continues working unchanged
  - Android TV: No — Continues working unchanged
  - Admin: No — Not affected
- **Testing Performed**:
  - API: Verified profile update endpoint handles color avatars
  - iOS: Login flow tested, profile selection working, profile editing functional
  - Android Mobile: Verified compatibility maintained
  - Android TV: Not tested (no changes required)
- **Breaking Changes**: None
- **Rollout/Deployment Notes**: iOS app archived and ready for TestFlight deployment

### August 2025 (Popup Tracking System)
- **One-Time Popup Tracking System**: Architected and implemented scalable popup tracking for 1M+ users
- **Description**: Created comprehensive backend and iOS popup system to track one-time messages per user account (not per device). System includes database schema with partitioning, caching strategy, API endpoints, and iOS UI components. Popups are shown once per user and tracked server-side. Fixed Xcode project file issues to properly integrate popup files.
- **Affected Platforms**: iOS / Backend API / Future: Android Mobile / Android TV
- **Endpoints Affected**: 
  - `/api/v2/popup/pending` (fetch pending popups)
  - `/api/v2/popup/dismiss` (mark popup dismissed)
  - `/api/v2/popup/acknowledge` (mark popup acknowledged)
  - `/api/v2/popup/history` (get user popup history)
  - `/api/v2/popup/admin/*` (admin endpoints)
- **Data Model Changes**:
  - Added fields: None to existing models
  - Nullable fields: None
  - Removed fields: None
  - Type changes: None
  - New tables: popup_definition, app_user_popup_status, popup_analytics
- **Compatibility Strategy**:
  - Backward compatible: Yes
  - New feature addition, no changes to existing endpoints
- **Client Updates Required**:
  - iOS: Yes — Added PopupView, PopupService, Popup model, integrated in HomeView
  - Android Mobile: No — Future implementation planned
  - Android TV: No — Future implementation planned
  - Admin: No — Admin endpoints available for managing popups
- **Testing Performed**:
  - API: Created migration files, models, and controllers
  - iOS: Successfully builds with popup system integrated, Xcode project references fixed
  - Android Mobile: Not tested
  - Android TV: Not tested
- **Breaking Changes**: None
- **Rollout/Deployment Notes**: Run database migration `2025_08_16_create_popup_system_tables.php` or execute SQL from `run_popup_migration.sql`. Deploy backend changes. iOS app successfully builds and is ready for deployment.

### August 2025 (Profile Delete Button Fix)
- **Profile Delete Button Sizing**: Fixed truncated delete button in Who's Watching screen
- **Description**: Reduced size of red X delete button in ProfileSelectionView from 24 to 20 points and adjusted offset to prevent truncation by profile avatar frame
- **Affected Platforms**: iOS
- **Endpoints Affected**: None
- **Data Model Changes**: None
- **Compatibility Strategy**:
  - Backward compatible: Yes
  - UI-only change
- **Client Updates Required**:
  - iOS: Yes — ProfileSelectionView.swift updated
  - Android Mobile: No
  - Android TV: No
  - Admin: No
- **Testing Performed**:
  - API: No changes
  - iOS: Visual fix applied to ProfileSelectionView
  - Android Mobile: No changes
  - Android TV: No changes
- **Breaking Changes**: None
- **Rollout/Deployment Notes**: iOS app needs rebuild and deployment

### August 2024 (Documentation Setup)
- **System Documentation Infrastructure**: Created comprehensive documentation system for AI assistants
- **Description**: Established CLAUDE.md for system architecture, AI_SYSTEM_PROMPT.md for AI instructions, .ai-instructions for quick reference, and CHANGELOG_AI.md for tracking changes
- **Affected Platforms**: All (documentation only)
- **Endpoints Affected**: None
- **Data Model Changes**: None
- **Compatibility Strategy**:
  - Backward compatible: N/A (documentation only)
- **Client Updates Required**:
  - iOS: No
  - Android Mobile: No
  - Android TV: No
  - Admin: No
- **Testing Performed**: N/A (documentation)
- **Breaking Changes**: None
- **Rollout/Deployment Notes**: Documentation files committed to repository root and docs folder

