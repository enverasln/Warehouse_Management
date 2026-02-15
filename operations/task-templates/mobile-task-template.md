# Mobile Task Template

## Task Information
- **Task ID**: TASK-XXX
- **Agent**: mobile-coder
- **Priority**: high/medium/low
- **Estimated Time**: Xh

## Requirements
- [ ] Create/update Compose UI screens
- [ ] Implement ViewModel logic
- [ ] Add data layer integration
- [ ] Handle loading and error states
- [ ] Write Compose UI tests

## Technology Stack
- Kotlin
- Jetpack Compose
- Android Architecture Components
- Coroutines & Flow
- Hilt for DI

## Acceptance Criteria
- [ ] UI screens render correctly on different screen sizes
- [ ] Navigation works as expected
- [ ] Data loading and error states handled
- [ ] UI tests pass (min 70% coverage)
- [ ] No memory leaks

## UI/UX Requirements
- Follow Material Design 3 guidelines
- Support dark mode
- Responsive layouts
- Accessibility compliance (TalkBack support)
- Proper loading indicators
- User-friendly error messages

## Artifact Requirements
```json
{
  "artifact_type": "code",
  "language": "kotlin",
  "platform": "android",
  "files": [],
  "build_status": "success",
  "tests_passed": true,
  "coverage": 0.0,
  "ui_components": []
}
```

## Dependencies
- Backend API contract/endpoints
- Design mockups/specifications
- List other dependent tasks here

## Testing Checklist
- [ ] Unit tests for ViewModels
- [ ] Compose UI tests for screens
- [ ] Navigation tests
- [ ] Repository layer tests
- [ ] Edge cases (empty state, error state)

## Screenshots Required
- [ ] Light mode
- [ ] Dark mode
- [ ] Loading state
- [ ] Error state
- [ ] Empty state
