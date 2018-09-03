package io.cucumber.core.filter;

import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.PickleStep;
import io.cucumber.messages.Messages.PickleTag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class NamePredicateTest {
    private static final List<PickleStep> NO_STEPS = Collections.emptyList();
    private static final List<PickleTag> NO_TAGS = Collections.emptyList();
    private static final Location MOCK_LOCATION = mock(Location.class);

    @Test
    public void anchored_name_pattern_matches_exact_name() {
        Pickle Pickle = createPickleWithName("a pickle name");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a pickle name$")));

        assertTrue(predicate.apply(Pickle));
    }

    @Test
    public void anchored_name_pattern_does_not_match_part_of_name() {
        Pickle Pickle = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a pickle name$")));

        assertFalse(predicate.apply(Pickle));
    }

    @Test
    public void non_anchored_name_pattern_matches_part_of_name() {
        Pickle Pickle = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a pickle name")));

        assertTrue(predicate.apply(Pickle));
    }

    @Test
    public void wildcard_name_pattern_matches_part_of_name() {
        Pickle Pickle = createPickleWithName("a Pickle name");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a .* name")));

        assertTrue(predicate.apply(Pickle));
    }

    private Pickle createPickleWithName(String pickleName) {
        return new Pickle("uri", new Pickle(pickleName, "en", NO_STEPS, NO_TAGS, asList(MOCK_LOCATION)));
    }
}
