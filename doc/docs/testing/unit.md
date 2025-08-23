
# Unit Testing in Android

## What is Unit Testing?
Unit testing is the practice of testing individual components (methods, classes, or modules) of your application in isolation to ensure they behave as expected.  
Unlike UI or instrumentation tests, unit tests **do not require an emulator or device** — they run directly on the JVM, making them **fast and lightweight**.

---

## Why Unit Testing?
- ✅ Ensures correctness of business logic  
- ✅ Detects bugs early during development  
- ✅ Provides confidence when refactoring code  
- ✅ Improves maintainability and scalability  
- ✅ Helps achieve higher code coverage  

---

## Tools & Frameworks Used
- **JUnit4 / JUnit5** → Core testing framework  
- **Mockito / MockK** → For mocking dependencies  
- **Truth / AssertJ** → For readable assertions  
- **Robolectric (optional)** → For testing Android framework classes on JVM  

---

## Example: Simple Unit Test

```kotlin
// Calculator.kt (class under test)
class Calculator {
    fun add(a: Int, b: Int): Int = a + b
    fun divide(a: Int, b: Int): Int {
        require(b != 0) { "Divider cannot be zero" }
        return a / b
    }
}

// CalculatorTest.kt (unit test)
import org.junit.Assert.*
import org.junit.Test

class CalculatorTest {

    private val calculator = Calculator()

    @Test
    fun `addition should return correct sum`() {
        val result = calculator.add(2, 3)
        assertEquals(5, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `division by zero should throw exception`() {
        calculator.divide(10, 0)
    }
}
````

---

## How to Run Unit Tests

1. In **Android Studio**:

   * Right-click on the `test` directory → **Run Tests**
   * Or run individual test files/classes/methods with the green play button.

2. Using **Gradle CLI**:

   ```bash
   ./gradlew test
   ```

---

## Best Practices

* Keep tests **small and independent**
* Test one functionality at a time
* Use **mocks/stubs/fakes** for external dependencies
* Follow **Arrange–Act–Assert (AAA)** pattern in tests
* Maintain a good **naming convention** (e.g., `methodName_condition_expectedResult`)

---

## Where Unit Tests Live

* **Unit tests**:
  `app/src/test/java/...` → Runs on local JVM

* **Instrumentation/UI tests**:
  `app/src/androidTest/java/...` → Runs on device/emulator
