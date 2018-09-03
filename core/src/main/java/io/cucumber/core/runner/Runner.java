package io.cucumber.core.runner;

import cucumber.api.HookType;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.SnippetsSuggestedEvent;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.PickleStep;
import io.cucumber.messages.Messages.PickleTag;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Runner {
    private final CachingGlue glue = new CachingGlue();
    private final EventBus bus;
    private final Collection<? extends Backend> backends;
    private final RuntimeOptions runtimeOptions;

    public Runner(EventBus bus, Collection<? extends Backend> backends, RuntimeOptions runtimeOptions) {
        this.bus = bus;
        this.runtimeOptions = runtimeOptions;
        this.backends = backends;
        for (Backend backend : backends) {
            backend.loadGlue(glue, runtimeOptions.getGlue());
        }

    }

    public EventBus getBus() {
        return bus;
    }

    public void runPickle(Pickle pickle) {
        buildBackendWorlds(); // Java8 step definitions will be added to the glue here
        TestCase testCase = createTestCaseForPickle(pickle);
        testCase.run(bus);
        disposeBackendWorlds();
        glue.removeScenarioScopedGlue();
    }

    public void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        glue.reportStepDefinitions(stepDefinitionReporter);
    }

    private TestCase createTestCaseForPickle(Pickle pickle) {
        List<PickleStepTestStep> testSteps = new ArrayList<>();
        List<HookTestStep> beforeHooks = new ArrayList<>();
        List<HookTestStep> afterHooks = new ArrayList<>();
        if (!pickle.getStepsList().isEmpty()) {
            addTestStepsForBeforeHooks(beforeHooks, pickle.getTagsList());
            addTestStepsForPickleSteps(testSteps, pickle);
            addTestStepsForAfterHooks(afterHooks, pickle.getTagsList());
        }
        return new TestCase(testSteps, beforeHooks, afterHooks, pickle, runtimeOptions.isDryRun());
    }

    private void addTestStepsForPickleSteps(List<PickleStepTestStep> testSteps, Pickle pickle) {
        for (PickleStep step : pickle.getStepsList()) {
            PickleStepDefinitionMatch match;
            try {
                match = glue.stepDefinitionMatch(pickle.getUri(), step);
                if (match == null) {
                    List<String> snippets = new ArrayList<>();
                    for (Backend backend : backends) {
                        List<String> snippet = backend.getSnippet(step, "**KEYWORD**", runtimeOptions.getSnippetType().getFunctionNameGenerator());
                        snippets.addAll(snippet);
                    }
                    if (!snippets.isEmpty()) {
                        bus.send(new SnippetsSuggestedEvent(bus.getTime(), pickle.getUri(), step.getLocationsList(), snippets));
                    }
                    match = new UndefinedPickleStepDefinitionMatch(step);
                }
            } catch (AmbiguousStepDefinitionsException e) {
                match = new AmbiguousPickleStepDefinitionsMatch(pickle.getUri(), step, e);
            } catch (Throwable t) {
                match = new FailedPickleStepInstantiationMatch(pickle.getUri(), step, t);
            }


            List<HookTestStep> afterStepHookSteps = getAfterStepHooks(pickle.getTagsList());
            List<HookTestStep> beforeStepHookSteps = getBeforeStepHooks(pickle.getTagsList());
            testSteps.add(new PickleStepTestStep(pickle.getUri(), step, beforeStepHookSteps, afterStepHookSteps, match));
        }
    }

    private void addTestStepsForBeforeHooks(List<HookTestStep> testSteps, List<PickleTag> tags) {
        addTestStepsForHooks(testSteps, tags, glue.getBeforeHooks(), HookType.Before);
    }

    private void addTestStepsForAfterHooks(List<HookTestStep> testSteps, List<PickleTag> tags) {
        addTestStepsForHooks(testSteps, tags, glue.getAfterHooks(), HookType.After);
    }

    private void addTestStepsForHooks(List<HookTestStep> testSteps, List<PickleTag> tags, List<HookDefinition> hooks, HookType hookType) {
        for (HookDefinition hook : hooks) {
            if (hook.matches(tags)) {
                HookTestStep testStep = new HookTestStep(hookType, new HookDefinitionMatch(hook));
                testSteps.add(testStep);
            }
        }
    }

    private List<HookTestStep> getAfterStepHooks(List<PickleTag> tags) {
        List<HookTestStep> hookSteps = new ArrayList<>();
        addTestStepsForHooks(hookSteps, tags, glue.getAfterStepHooks(), HookType.AfterStep);
        return hookSteps;
    }

    private List<HookTestStep> getBeforeStepHooks(List<PickleTag> tags) {
        List<HookTestStep> hookSteps = new ArrayList<>();
        addTestStepsForHooks(hookSteps, tags, glue.getBeforeStepHooks(), HookType.BeforeStep);
        return hookSteps;
    }

    private void buildBackendWorlds() {
        for (Backend backend : backends) {
            backend.buildWorld();
        }
    }

    private void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }
}
