import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Selenium smoke tests for the local mini shopping cart demo.
 * Each test reloads the HTML page so the cart starts empty.
 */
public class MiniShoppingCartTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static String pageUrl;
    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        try {
            URL resource = MiniShoppingCartTest.class.getClassLoader()
                    .getResource("shopping-cart.html");
            if (resource == null) {
                throw new IllegalStateException(
                        "shopping-cart.html was not found on the classpath. "
                        + "Ensure it is in src/main/resources or copied into out/.");
            }
            pageUrl = resource.toExternalForm();
            System.out.println("Shopping cart page: " + pageUrl);

            ChromeOptions options = new ChromeOptions();
            if (Boolean.getBoolean("selenium.headless")) {
                options.addArguments("--headless=new");
            }
            options.addArguments("--window-size=1280,900");
            options.addArguments("--disable-dev-shm-usage");
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                options.addArguments("--no-sandbox");
            }

            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            runTest("TEST 1 - ADD MOUSE AND VERIFY TOTAL", MiniShoppingCartTest::testMouse);
            runTest("TEST 2 - ADD KEYBOARD AND VERIFY TOTAL", MiniShoppingCartTest::testKeyboard);
            runTest("TEST 3 - ADD BOTH ITEMS AND VERIFY TOTAL", MiniShoppingCartTest::testBothItems);
            runTest("TEST 4 - ADD SAME ITEM TWICE", MiniShoppingCartTest::testQuantity);

        } catch (Exception e) {
            failed++;
            String detail = "Browser setup / test execution: " + e;
            failures.add(detail);
            System.err.println("ERROR: " + detail);
            e.printStackTrace(System.err);
        } finally {
            if (driver != null) {
                driver.quit();
            }
            printSummary();
        }

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String name, Runnable test) {
        try {
            driver.get(pageUrl); // resets the cart for each independent test
            test.run();
            passed++;
            System.out.println("[" + name + "] PASS");
        } catch (AssertionError | RuntimeException e) {
            failed++;
            String detail = "[" + name + "] FAIL - " + e.getMessage();
            failures.add(detail);
            System.out.println(detail);
        }
    }

    private static void testMouse() {
        driver.findElement(By.id("add-mouse")).click();
        assertText(By.id("cart-item-mouse"), "Wireless Mouse × 1 — ₹499");
        assertText(By.id("cartTotal"), "₹499");
        assertText(By.id("cartCount"), "Items in cart: 1");
    }

    private static void testKeyboard() {
        driver.findElement(By.id("add-keyboard")).click();
        assertText(By.id("cart-item-keyboard"), "Mechanical Keyboard × 1 — ₹999");
        assertText(By.id("cartTotal"), "₹999");
        assertText(By.id("cartCount"), "Items in cart: 1");
    }

    private static void testBothItems() {
        driver.findElement(By.id("add-mouse")).click();
        driver.findElement(By.id("add-keyboard")).click();
        assertText(By.id("cart-item-mouse"), "Wireless Mouse × 1 — ₹499");
        assertText(By.id("cart-item-keyboard"), "Mechanical Keyboard × 1 — ₹999");
        assertText(By.id("cartTotal"), "₹1498");
        assertText(By.id("cartCount"), "Items in cart: 2");
    }

    private static void testQuantity() {
        WebElement addMouse = driver.findElement(By.id("add-mouse"));
        addMouse.click();
        addMouse.click();
        assertText(By.id("cart-item-mouse"), "Wireless Mouse × 2 — ₹998");
        assertText(By.id("cartTotal"), "₹998");
        assertText(By.id("cartCount"), "Items in cart: 2");
    }

    private static void assertText(By locator, String expected) {
        String actual = wait.until(d -> {
            WebElement element = d.findElement(locator);
            String text = element.getText().trim();
            return text.equals(expected) ? text : null;
        });
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected '" + expected + "' but got '" + actual + "'");
        }
    }

    private static void printSummary() {
        System.out.println("\n========== SHOPPING CART TEST SUMMARY ==========");
        System.out.println("Tests passed: " + passed + "/" + (passed + failed));
        System.out.println("Tests failed: " + failed);
        if (failures.isEmpty()) {
            System.out.println("RESULT: ALL TESTS PASSED");
        } else {
            System.out.println("RESULT: TESTS FAILED");
            for (String failure : failures) {
                System.out.println(" - " + failure);
            }
        }
    }
}
