package io.cucumber.core.filter;

import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.PickleStep;
import io.cucumber.messages.Messages.PickleTag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinePredicateTest {
    private static final String NAME = "pickle_name";
    private static final String LANGUAGE = "en";
    private static final List<PickleStep> NO_STEPS = Collections.<PickleStep>emptyList();
    private static final List<PickleTag> NO_TAGS = Collections.<PickleTag>emptyList();

    @Test
    public void matches_pickles_from_files_not_in_the_predicate_map() {
        // the argument "path/file.feature another_path/file.feature:8"
        // results in only line predicates only for another_path/file.feature,
        // but all pickles from path/file.feature shall also be executed.
        Pickle Pickle = createPickleWithLocations("path/file.feature", asList(Location(4)));
        LinePredicate predicate = new LinePredicate(singletonMap("another_path/file.feature", asList(8L)));

        assertTrue(predicate.apply(Pickle));
    }

    @Test
    public void matches_pickles_for_any_line_in_predicate() {
        Pickle Pickle = createPickleWithLocations("path/file.feature", asList(Location(8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(4L, 8L)));

        assertTrue(predicate.apply(Pickle));
    }

    @Test
    public void matches_pickles_on_any_location_of_the_pickle() {
        Pickle Pickle = createPickleWithLocations("path/file.feature", asList(Location(4), Location(8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(8L)));

        assertTrue(predicate.apply(Pickle));
    }

    @Test
    public void does_not_matches_pickles_not_on_any_line_of_the_predicate() {
        Pickle Pickle = createPickleWithLocations("path/file.feature", asList(Location(4), Location(8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(10L)));

        assertFalse(predicate.apply(Pickle));
    }

    private Pickle createPickleWithLocations(String uri, List<Location> locations) {
        return new Pickle(uri, new Pickle(NAME, LANGUAGE, NO_STEPS, NO_TAGS, locations));
    }

    private Location Location(int line) {
        return new Location(line, 0);
    }


}
