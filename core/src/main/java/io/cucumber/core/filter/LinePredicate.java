package io.cucumber.core.filter;

import io.cucumber.messages.Messages.Pickle;
import io.cucumber.messages.Messages.Location;

import java.util.List;
import java.util.Map;

final class LinePredicate implements PicklePredicate {
    private Map<String, List<Integer>> lineFilters;

    LinePredicate(Map<String, List<Integer>> lineFilters) {
        this.lineFilters = lineFilters;
    }

    @Override
    public boolean apply(Pickle pickle) {
        String picklePath = pickle.getUri();
        if (!lineFilters.containsKey(picklePath)) {
            return true;
        }
        List<Integer> lines = lineFilters.get(picklePath);
        return pickle.getLocationsList()
            .stream()
            .map(Location::getLine)
            .anyMatch(lines::contains);
    }
}
