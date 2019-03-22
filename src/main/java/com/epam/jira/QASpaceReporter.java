package com.epam.jira;

import com.epam.jira.core.TestResultProcessor;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.apache.commons.text.StringEscapeUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.*;

public class QASpaceReporter implements Reporter, Formatter {


    private static final String TRIGGER_TAG = "@JIRATestKey";
    private static final String TAG_FIND_EXPRESSION = String.format("(?<=(%s\\()).*(?=(\\)))", TRIGGER_TAG);
    private static final HashMap<String, String> MIME_TYPES_EXTENSIONS = new HashMap<>();

    static {
        MIME_TYPES_EXTENSIONS.put("image/bmp", "bmp");
        MIME_TYPES_EXTENSIONS.put("image/gif", "gif");
        MIME_TYPES_EXTENSIONS.put("image/jpeg", "jpg");
        MIME_TYPES_EXTENSIONS.put("image/png", "png");
        MIME_TYPES_EXTENSIONS.put("image/svg+xml", "svg");
        MIME_TYPES_EXTENSIONS.put("video/ogg", "ogg");
        MIME_TYPES_EXTENSIONS.put("text/plain", "txt");
    }

    private boolean listenedTest;
    private Pattern tagPattern = Pattern.compile(TAG_FIND_EXPRESSION);


    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    @Override
    public void uri(String s) {

    }

    @Override
    public void feature(Feature feature) {

    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {

    }

    @Override
    public void examples(Examples examples) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        if (isTagPresent(scenario)) {
            listenedTest = true;
            TestResultProcessor.startJiraAnnotatedTest(getKeyFromTag(scenario));
        }
    }

    @Override
    public void background(Background background) {

    }

    @Override
    public void scenario(Scenario scenario) {
        if (listenedTest) {
            TestResultProcessor.addToSummary("Scenario: " +
                    scenario.getName() +
                    scenario.getDescription());
        }
    }

    @Override
    public void step(Step step) {

    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void done() {

    }

    @Override
    public void close() {
        TestResultProcessor.saveResults();
    }

    @Override
    public void eof() {

    }

    @Override
    public void before(Match match, Result result) {

    }

    @Override
    public void result(Result result) {
        if (listenedTest) {
            String status = result.getStatus();
            TestResultProcessor.setStatus(status.substring(0, 1).toUpperCase() + status.substring(1, status.length()));
            TestResultProcessor.setTime(durationToString(result.getDuration()));
            if (result.getError() != null) {
                TestResultProcessor.addException(result.getError());
            }
        }
    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void match(Match match) {

    }

    @Override
    public void embedding(String s, byte[] bytes) {
        if (listenedTest) {
            try {
                String filePath = String.format("attachment_%s.%s", LocalDateTime.now().toString().replace(":", "-"), MIME_TYPES_EXTENSIONS.get(s));
                Files.write(Paths.get(filePath), bytes);
                sendTempFileProcessor(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void write(String s) {
        if (listenedTest) {
            try {
                String filePath = String.format("attachment_%s.txt", LocalDateTime.now().toString().replace(":", "-"));
                Files.write(Paths.get(filePath), StringEscapeUtils.escapeJson(s).getBytes());
                sendTempFileProcessor(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getKeyFromTag(Scenario scenario) {
        return scenario.getTags().stream().filter(tag -> isStringContainsTag(tag.getName())).findFirst()
                .map(this::extractKey).orElse("");
    }

    private String extractKey(Tag tag) {
        String tagString = tag.getName();
        String result = null;
        Matcher matcher = tagPattern.matcher(tagString);
        if (matcher.find()) {
            result = tagString.substring(matcher.start(), matcher.end());
        }
        return result;
    }

    private boolean isTagPresent(Scenario scenario) {
        return scenario.getTags().stream().anyMatch(tag -> isStringContainsTag(tag.getName()));
    }

    private boolean isStringContainsTag(String string) {
        return string.contains(TRIGGER_TAG);
    }

    private String durationToString(long duration) {
        return String.valueOf(MINUTES.convert(duration, NANOSECONDS)) +
                "m " +
                SECONDS.convert(duration, NANOSECONDS) +
                "." +
                MILLISECONDS.convert(duration, NANOSECONDS) +
                "s";
    }

    private void sendTempFileProcessor(String filePath) {
        File file = new File(filePath);
        TestResultProcessor.addAttachment(file);
        file.delete();
    }
}