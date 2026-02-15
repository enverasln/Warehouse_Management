# Testing Task Template

## Task Information
- **Task ID**: TASK-XXX
- **Agent**: tester
- **Priority**: high/medium/low
- **Estimated Time**: Xh

## Testing Scope
- [ ] Backend API tests
- [ ] Mobile UI tests
- [ ] Web E2E tests
- [ ] Integration tests
- [ ] Performance tests
- [ ] Security tests

## Technology Stack

### Backend Testing
- xUnit
- Moq
- FluentAssertions
- TestContainers (for integration tests)

### Mobile Testing
- JUnit
- Compose Testing
- Espresso (if needed)
- MockK

### Web Testing
- Jest
- React Testing Library
- Playwright / Cypress
- Vitest

## Test Categories

### 1. Unit Tests
- Test individual functions/methods
- Mock external dependencies
- Fast execution
- Target coverage: 80%+

### 2. Integration Tests
- Test component interactions
- Use real dependencies when possible
- Test database operations
- Test API contracts

### 3. E2E Tests
- Test complete user workflows
- Real browser automation
- Critical path scenarios
- Smoke tests for deployment

### 4. Performance Tests
- Load testing
- Stress testing
- Response time validation
- Resource usage monitoring

### 5. Security Tests
- SQL injection prevention
- XSS prevention
- Authentication/authorization
- OWASP Top 10 checks

## Acceptance Criteria
- [ ] All tests pass
- [ ] Code coverage meets threshold
- [ ] No flaky tests
- [ ] Test execution time acceptable
- [ ] Test reports generated

## Test Plan

### Backend API Tests
```csharp
[Fact]
public async Task CreateProduct_ValidInput_ReturnsCreated()
{
    // Arrange
    var product = new CreateProductRequest { Name = "Test", Price = 100 };
    
    // Act
    var result = await _controller.CreateProduct(product);
    
    // Assert
    result.Should().BeOfType<CreatedResult>();
}
```

### Mobile UI Tests
```kotlin
@Test
fun productList_DisplaysProducts() {
    composeTestRule.setContent {
        ProductListScreen(viewModel = mockViewModel)
    }
    
    composeTestRule.onNodeWithText("Product 1").assertExists()
}
```

### Web E2E Tests
```typescript
test('user can create product', async ({ page }) => {
    await page.goto('/products');
    await page.click('button:has-text("Add Product")');
    await page.fill('input[name="name"]', 'Test Product');
    await page.fill('input[name="price"]', '100');
    await page.click('button:has-text("Save")');
    await expect(page.locator('text=Product created')).toBeVisible();
});
```

## Test Coverage Requirements

| Component | Minimum Coverage |
|-----------|------------------|
| Backend API | 80% |
| Business Logic | 90% |
| Mobile UI | 70% |
| Web Components | 75% |
| Integration | 60% |

## Test Data Management
- [ ] Test data fixtures created
- [ ] Database seeding scripts
- [ ] Mock data generators
- [ ] Test data cleanup strategy

## Artifact Requirements
```json
{
  "artifact_type": "test",
  "test_results": {
    "backend": {
      "total": 0,
      "passed": 0,
      "failed": 0,
      "coverage": 0.0
    },
    "mobile": {
      "total": 0,
      "passed": 0,
      "failed": 0,
      "coverage": 0.0
    },
    "web": {
      "total": 0,
      "passed": 0,
      "failed": 0,
      "coverage": 0.0
    }
  },
  "execution_time_seconds": 0,
  "flaky_tests": [],
  "test_reports": []
}
```

## Dependencies
- Code artifacts from backend/mobile/web coders
- Test environment setup
- Test data preparation

## Test Execution Commands

### Backend Tests
```bash
dotnet test --collect:"XPlat Code Coverage"
```

### Mobile Tests
```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

### Web Tests
```bash
npm test -- --coverage
npm run test:e2e
```

## Failure Handling
- [ ] Screenshot on failure (E2E tests)
- [ ] Log collection on failure
- [ ] Detailed error messages
- [ ] Retry logic for flaky tests (max 2 retries)

## Test Report Format
- JUnit XML for CI integration
- HTML reports for human review
- Code coverage reports (Cobertura/LCOV)
- Performance metrics

## Quality Gates
- [ ] All tests must pass
- [ ] Coverage thresholds met
- [ ] No critical security issues
- [ ] Performance within acceptable limits
- [ ] No test execution timeout
