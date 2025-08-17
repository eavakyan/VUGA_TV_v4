# DEFAULT AI ASSISTANT SYSTEM PROMPT

---

## SYSTEM CONTEXT

You are working on the VUGA TV multi-platform streaming system. This project consists of:
- Laravel PHP backend API (shared by all clients)
- iOS mobile app (SwiftUI)
- Android mobile app (Java)
- Android TV app (Leanback)
- Admin panel (React)

## CRITICAL INSTRUCTIONS

### 1. BEFORE YOU START
**MANDATORY**: Read these files in order:
1. `CLAUDE.md` - Complete system documentation and compatibility matrix
2. `.ai-instructions` - Quick reference for AI assistants

These files contain:
- Which platforms use which API endpoints
- Field compatibility requirements
- Known issues and workarounds
- Build and deployment commands

### 2. WORKING RULES

#### API Changes
- **NEVER** remove fields from API responses (will break apps)
- **NEVER** change field types without versioning
- **ALWAYS** check CLAUDE.md compatibility matrix before modifying endpoints
- **SEARCH** all platform directories before changing shared code
- **PREFER** client-side fixes over API changes

#### Cross-Platform Compatibility
- Before changing an API endpoint, search for usage in:
  - `/ios-mobile-app/`
  - `/android-mobile-app/`
  - `/android-tv-app/`
  - `/admin-app/`
- If breaking change is required, create new versioned endpoint (e.g., v3)
- Test or document required updates for each platform

#### Code Patterns
- iOS: Uses Codable with optional fields via `decodeIfPresent`
- Android: Uses Gson, expects all declared fields present
- API: Must maintain backward compatibility

### 3. AFTER MAKING CHANGES

**MANDATORY**: Update CLAUDE.md when you:
- ✅ Add or modify API endpoints → Update compatibility matrix
- ✅ Change data models → Update "Field Compatibility Requirements"
- ✅ Fix bugs revealing compatibility issues → Update "Known Issues"
- ✅ Add features affecting multiple platforms → Update "Recent Changes & Issues"

Format for updates:
```markdown
### [Month Year]
- **[Feature/Fix Name]**: Brief description
- **Affected Platforms**: iOS / Android Mobile / Android TV / Admin
- **Breaking Changes**: None OR describe migration needed
```

### 4. VERIFICATION CHECKLIST

Before completing any task, confirm:
- [ ] I read CLAUDE.md for system context
- [ ] I checked which platforms use affected endpoints
- [ ] I maintained backward compatibility OR created versioned endpoint
- [ ] I updated CLAUDE.md with my changes
- [ ] I documented any new patterns or workarounds
 - [ ] I appended a Change Log Entry to `docs/CHANGELOG_AI.md`

## KEY SYSTEM INFO

- **API Base URL**: https://iosdev.gossip-stone.com/api/v2/
- **API Key Header**: `apikey: jpwc3pny`
- **Primary Database**: MySQL (vuga_tv_app)
- **File Storage**: Digital Ocean Spaces (NYC3)
- **Profile Images Path**: `profile-avatars/[user_id]/[profile_id]/`

## CURRENT KNOWN ISSUES
1. iOS uses `/profile/update` while Android uses `/updateProfile`
2. Some API responses return `"<null>"` as string instead of null
3. Avatar color required by Android but optional in iOS

## CRITICAL COMPATIBILITY RULES
1. Profile `avatar_color` must ALWAYS be returned from API (Android requirement)
2. All datetime fields must be in ISO 8601 format
3. Boolean fields may be returned as 0/1 or true/false (handle both)
4. Image URLs must be absolute paths including domain

## DEVELOPMENT COMMANDS

See CLAUDE.md for complete command reference including:
- iOS build and archive commands
- Android build and deployment
- Laravel server and migration commands
- Testing device connections

### 5. CHANGE LOG ENTRIES (MANDATORY)
All agents must append a Change Log Entry to `docs/CHANGELOG_AI.md` for any change that affects API endpoints, data models, cross-platform behavior, or shared code. Append to the end of the file using this template:

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

END OF SYSTEM PROMPT

Remember: The goal is to maintain a stable, multi-platform system. When in doubt, preserve backward compatibility and document thoroughly in CLAUDE.md.