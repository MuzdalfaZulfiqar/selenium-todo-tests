package com.devops.assignment.selenium_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import static org.junit.Assert.assertFalse;
import org.openqa.selenium.support.ui.ExpectedConditions;



public class GitHubHomePageTest {

    private WebDriver driver;

    @Before
    public void setUp() {
    	// Fix WebDriverManager cache issue in Docker container
        System.setProperty("wdm.cachePath", "/tmp/selenium-cache");

        // Set up WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");

        // Set a unique user data directory to avoid session conflicts
        String userDataDir = "/tmp/chrome-user-data-" + System.currentTimeMillis();
        options.addArguments("--user-data-dir=" + userDataDir);

        // Initialize ChromeDriver
        driver = new ChromeDriver(options);
        driver.get("http://18.207.221.226:3000"); // Your app URL
    }

    /** 🔹 Example test case #1: page title check */
    @Test
    public void verifyHomePageTitle() {
        String expected = "Todo App";           // change to the real title
        String actual   = driver.getTitle();
        assertEquals(expected, actual);
    }
    
//---------------------------------------------------------------------------------------------   
    /** 🔹 Example test case #2: Add an item */
    @Test
    public void addItem() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1 - Find the input field by its ID and enter text
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("todo-input")));
        input.sendKeys("Buy milk");

        // 2 - Find the add button and click it
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-add")));
        addButton.click();
    }

//---------------------------------------------------------------------------------------------   

    /** 🔹 Example test case #3: Verify if the added item appears on the page or not */
    @Test
    public void verifyItemAppearsAfterAddition() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1️ - Enter a unique item text
        String newTodo = "Buy milk";
        WebElement input = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("todo-input")));
        input.sendKeys(newTodo);

        // 2️ - Click the add button
        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("todo-add")));
        addBtn.click();

        // 3️ - Wait for the new item node containing that text to become visible
        WebElement addedItem = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[contains(@class,'name') and text()='" + newTodo + "']")
                ));

        // 4️ - Assert the displayed text matches what we added
        assertEquals(newTodo, addedItem.getText().trim());
    }
    
//---------------------------------------------------------------------------------------------      

    /** 🔹 Example test case #4: Verify the checkbox */
    @Test
    public void markItemAsComplete() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String todoText = "Read Book";

        // 1️ - Add the new item
        wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-input")))
            .sendKeys(todoText);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-add")))
            .click();

        // 2️ - Locate the <div class="name"> that contains the text we just added
        WebElement nameDiv = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'name') and normalize-space()='" + todoText + "']")
            )
        );

        // 3️ - Climb to its parent container with class "item"
        WebElement itemContainer = nameDiv.findElement(
            By.xpath("./ancestor::div[contains(@class,'item')]")
        );

        // 4️ - Click that row’s toggle button
        itemContainer.findElement(By.cssSelector("button.toggles")).click();

        // 5️ - Wait until the container gains the 'completed' class
        wait.until(ExpectedConditions.attributeContains(itemContainer, "class", "completed"));

        // 6️ - Assert
        assertTrue(itemContainer.getAttribute("class").contains("completed"));
    }


    
//---------------------------------------------------------------------------------------------   
    
    /** 🔹 Example test case #5: Verify item removal */

    @Test
    public void removeItem() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String todoText = "Delete Me";

        // 1️ - Add the item
        wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-input")))
            .sendKeys(todoText);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-add")))
            .click();

        // 2️ - Locate the newly‑added row container
        WebElement nameDiv = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'name') and normalize-space()='" + todoText + "']")
            )
        );
        WebElement itemContainer = nameDiv.findElement(
            By.xpath("./ancestor::div[contains(@class,'item')]")
        );

        // 3️ - Click its remove (trash) button
        itemContainer.findElement(By.cssSelector("button[aria-label='Remove Item']")).click();

        // 4️ - Wait until that row disappears
        wait.until(ExpectedConditions.stalenessOf(itemContainer));

        // 5️ - Assert it is no longer present
        boolean stillPresent = driver.findElements(
            By.xpath("//div[contains(@class,'name') and normalize-space()='" + todoText + "']")
        ).size() > 0;

        assertFalse("Item should be removed", stillPresent);
    }

    
//---------------------------------------------------------------------------------------------   
    /** 🔹 Example test case #6: Verify prevention of empty item submission */
    @Test
    public void preventEmptyItemSubmission() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("todo-input")));
        WebElement addButton = driver.findElement(By.id("todo-add"));

        // Clear the input to make sure it's empty
        input.clear();

        // Wait briefly to ensure button updates its disabled state
        new WebDriverWait(driver, Duration.ofSeconds(2)).until(driver1 -> !addButton.isEnabled());

        // Assert that the add button is disabled
        assertFalse("Add button should be disabled when input is empty", addButton.isEnabled());
    }

//---------------------------------------------------------------------------------------------   

    /** 🔹 Example test case #7: Verify item persistance on page reload */    
    @Test
    public void verifyItemPersistenceAfterReload() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // 1️ -   Create a unique text so backend won’t reject duplicates
        String itemText = "Persist-" + System.currentTimeMillis();

        // 2️ -  Add the item
        wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-input")))
            .sendKeys(itemText);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-add")))
            .click();

        // 3️ -  Wait until it appears in the list
        String itemXpath = "//div[contains(@class,'name') and normalize-space()='" + itemText + "']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(itemXpath)));

        // 4️ - Refresh the page
        driver.navigate().refresh();

        // 5️ - Verify the same item is still visible after reload
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(itemXpath)));
    }


//---------------------------------------------------------------------------------------------   

    /** 🔹 Example test case #8: Verify item completion toggle status */ 
    @Test
    public void toggleItemCompletionStatus() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        String itemText = "ToggleTest-" + System.currentTimeMillis();

        // 1️ - Add a new item
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-input")));
        input.sendKeys(itemText);

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("todo-add")));
        addButton.click();

        // 2️ - ind the row by matching partial text and structure
        String rowXpath = "//div[contains(@class,'item') and .//div[contains(@class,'name') and contains(text(),'" + itemText + "')]]";
        WebElement itemRow = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(rowXpath)));

        // 3️ - Click the toggle button inside that row
        WebElement toggleBtn = itemRow.findElement(By.cssSelector("button.toggles"));
        toggleBtn.click();

        // 4️ - Verify the 'completed' class was added (manually check if class actually changes in your HTML)
        wait.until(ExpectedConditions.attributeContains(itemRow, "class", "completed"));

        // 5️ - Toggle again and ensure the 'completed' class is removed
        toggleBtn.click();
        wait.until(ExpectedConditions.not(
            ExpectedConditions.attributeContains(itemRow, "class", "completed")
        ));
    }


  //---------------------------------------------------------------------------------------------   

      /** 🔹 Example test case #9: Verify item deletion toggle status */ 
    @Test
    public void addMultipleItemsAndCheckCount() {
    	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String baseText = "BulkAddTest-";
        int count = 3;

        for (int i = 0; i < count; i++) {
            String itemText = baseText + i;

            // Locate input field and clear it
            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("todo-input")));
            input.clear();
            input.sendKeys(itemText);

            // Wait for the add button to be enabled after entering text
            WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("todo-add")));
            wait.until(driver -> addBtn.isEnabled()); // Ensure button is enabled
            wait.until(ExpectedConditions.elementToBeClickable(addBtn)); // Ensure button is clickable
            addBtn.click();

            // Wait until the new item appears
            String xpath = "//div[contains(@class,'name') and contains(normalize-space(),'" + itemText + "')]";
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));

            // Brief pause to allow UI to stabilize (optional, for robustness)
            try { Thread.sleep(500); } catch (InterruptedException e) { /* Ignore */ }
        }

        // Final assertion: at least `count` items now exist
        List<WebElement> items = driver.findElements(By.cssSelector("div.item"));
        assertTrue("Expected at least " + count + " items, but found " + items.size(), items.size() >= count);
    }

  
    //---------------------------------------------------------------------------------------------   

    /** 🔹 Example test case #10: Verify add button gets disabled after item has been added */ 
    @Test
    public void addButtonDisablesAfterItemIsAdded() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Locate widgets
        WebElement input     = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("todo-input")));
        WebElement addButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("todo-add")));

        // Type unique item
        String itemText = "PostAddDisableTest-" + System.currentTimeMillis();
        input.clear();
        input.sendKeys(itemText);

        // Wait until button becomes enabled, then click
        wait.until(ExpectedConditions.elementToBeClickable(addButton)).click();

        // Wait until the item actually appears
        String itemXpath = "//div[contains(@class,'name') and normalize-space()='" + itemText + "']";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(itemXpath)));

        // After React resets the form, input should be empty and button disabled again
        wait.until(driver -> input.getAttribute("value").isEmpty());
        wait.until(driver -> !addButton.isEnabled());

        assertTrue("Input should be cleared", input.getAttribute("value").isEmpty());
        assertFalse("Add button should be disabled after submitting", addButton.isEnabled());
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
