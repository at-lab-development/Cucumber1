# QASpaceReport for Cucumber1

QASpaceReport for Cucumber1 is a Java library for creating proper artifact for Jenkins plugin.

## Installation

Add repository in pom.xml file
```java
        <repository>
            <id>Cucumber1-mvn-repo</id>
            <url>https://raw.github.com/at-lab-development/Cucumber1/mvn-repo/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
```
Add dependency in pom.xml file
```java
        <dependency>
            <groupId>com.epam.jira</groupId>
            <artifactId>cucumber1</artifactId>
            <version>1.0</version>
        </dependency>
```
## Usage

in your Runner add QASpaceReporter as a plugin
```java
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"com.epam.jira.QASpaceReporter"}
)
public class TestRunner {
```

Mark scenario with proper JIRA key

```java
Feature: YouTube video check

@JIRATestKey(EPMRDBY-912)
  Scenario: Failed check Epam title on main youtube page
    Given I am on page with url 'https://www.youtube.com/'
    Then I should see 'EPAM Systems Global' in list video
```

## Adding a attachment file
To add a screenshot or any other attachment after scenario call embed method.
```java
@After
    public void embedScreenshot(Scenario scenario) {
            byte[] screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
                scenario.embed(screenshot, "image/png");
    }
```
supports MIME types:
```java
"image/bmp"
"image/gif"
"image/jpeg"
"image/png"
"image/svg+xml"
"video/ogg"
"text/plain"
```
To add file during step perfoming:
1. Setup scenario in @Before hook:
```java
@Before
    public void beforeScenario(Scenario scenario) {
        this.scenario = scenario;
    }
```
2. Call embed() method in a step:
```java
byte[] screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
        scenario.embed(screenshot, "image/png");
```

To add text message during step perfoming:
1. Setup scenario in @Before hook:
```java
@Before
    public void beforeScenario(Scenario scenario) {
        this.scenario = scenario;
    }
```
2. Call write() method in a step:
```java
        scenario.write("Text message");
```
