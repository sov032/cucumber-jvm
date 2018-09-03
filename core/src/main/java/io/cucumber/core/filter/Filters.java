package io.cucumber.core.filter;

import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.messages.Messages.Pickle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Filters {

    private final List<PicklePredicate> filters;
    private final RuntimeOptions runtimeOptions;
    private final RerunFilters rerunFilters;

    public Filters(RuntimeOptions runtimeOptions, RerunFilters rerunFilters) {
        this.runtimeOptions = runtimeOptions;
        this.rerunFilters = rerunFilters;

        filters = new ArrayList<>();
        List<String> tagFilters = this.runtimeOptions.getTagFilters();
        if (!tagFilters.isEmpty()) {
            this.filters.add(new TagPredicate(tagFilters));
        }
        List<Pattern> nameFilters = runtimeOptions.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<String, List<Integer>> lineFilters = runtimeOptions.getLineFilters();
        Map<String, List<Integer>> rerunlineFilters = rerunFilters.processRerunFiles();
        for (Map.Entry<String,List<Integer>> line: rerunlineFilters.entrySet()) {
            addLineFilters(lineFilters, line.getKey(), line.getValue());
        }
        if (!lineFilters.isEmpty()) {
            this.filters.add(new LinePredicate(lineFilters));
        }
    }

    public boolean matchesFilters(Pickle Pickle) {
        for (PicklePredicate filter : filters) {
            if (!filter.apply(Pickle)) {
                return false;
            }
        }
        return true;
    }

    private void addLineFilters(Map<String, List<Integer>> parsedLineFilters, String key, List<Integer> lines) {
        if (parsedLineFilters.containsKey(key)) {
            parsedLineFilters.get(key).addAll(lines);
        } else {
            parsedLineFilters.put(key, lines);
        }
    }

}
