package io.cucumber.core.runner;

import cucumber.api.Scenario;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.PickleStep;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class AmbiguousStepDefinitionMatchsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final PickleStep pickleStep = PickleStep.newBuilder().build();
    private final AmbiguousStepDefinitionsException e = new AmbiguousStepDefinitionsException(pickleStep, Collections.emptyList());
    private final AmbiguousPickleStepDefinitionsMatch match = new AmbiguousPickleStepDefinitionsMatch("uri", PickleStep.newBuilder().build(), e);

    @Test
    public void throws_ambiguous_step_definitions_exception_when_run() {
        expectedException.expect(AmbiguousStepDefinitionsException.class);
        match.runStep(mock(Scenario.class));
    }

    @Test
    public void throws_ambiguous_step_definitions_exception_when_dry_run() {
        expectedException.expect(AmbiguousStepDefinitionsException.class);
        match.dryRunStep(mock(Scenario.class));
    }
}
