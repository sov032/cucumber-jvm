package io.cucumber.core.runner;

import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.PickleTag;
import io.cucumber.core.event.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class TestCase implements cucumber.api.TestCase {
    private final Pickle pickle;
    private final List<PickleStepTestStep> testSteps;
    private final boolean dryRun;
    private final List<HookTestStep> beforeHooks;
    private final List<HookTestStep> afterHooks;

    public TestCase(List<PickleStepTestStep> testSteps,
                    List<HookTestStep> beforeHooks,
                    List<HookTestStep> afterHooks,
                    Pickle pickle,
                    boolean dryRun) {
        this.testSteps = testSteps;
        this.beforeHooks = beforeHooks;
        this.afterHooks = afterHooks;
        this.pickle = pickle;
        this.dryRun = dryRun;
    }

    void run(EventBus bus) {
        boolean skipNextStep = this.dryRun;
        Long startTime = bus.getTime();
        bus.send(new TestCaseStarted(startTime, this));
        Scenario scenario = new Scenario(bus, this);

        for (HookTestStep before : beforeHooks) {
            skipNextStep |= before.run(this, bus, scenario, dryRun);
        }

        for (PickleStepTestStep step : testSteps) {
            skipNextStep |= step.run(this, bus, scenario, skipNextStep);
        }

        for (HookTestStep after : afterHooks) {
            after.run(this, bus, scenario, dryRun);
        }

        Long stopTime = bus.getTime();
        bus.send(new TestCaseFinished(stopTime, this, new Result(scenario.getStatus(), stopTime - startTime, scenario.getError())));
    }

    @Override
    public List<TestStep> getTestSteps() {
        List<TestStep> testSteps = new ArrayList<TestStep>(beforeHooks);
        for (PickleStepTestStep step : this.testSteps) {
            testSteps.addAll(step.getBeforeStepHookSteps());
            testSteps.add(step);
            testSteps.addAll(step.getAfterStepHookSteps());
        }
        testSteps.addAll(afterHooks);
        return testSteps;
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public String getScenarioDesignation() {
        return fileColonLine(pickle.getLocations(0)) + " # " + getName();
    }

    @Override
    public String getUri() {
        return pickle.getUri();
    }

    @Override
    public int getLine() {
        return pickle.getLocations(0).getLine();
    }

    public List<Integer> getLines() {
        return pickle.getLocationsList()
            .stream()
            .map(Location::getLine)
            .collect(Collectors.toList());
    }

    private String fileColonLine(Location location) {
        return pickle.getUri() + ":" + location.getLine();
    }

    @Override
    public List<PickleTag> getTags() {
        return pickle.getTagsList();
    }
}
