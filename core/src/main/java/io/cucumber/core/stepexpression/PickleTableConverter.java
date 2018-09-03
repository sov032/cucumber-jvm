package io.cucumber.core.stepexpression;

import io.cucumber.messages.Messages.PickleTable;
import io.cucumber.messages.Messages.PickleTableCell;

import java.util.List;

import static java.util.stream.Collectors.toList;

final class PickleTableConverter {

    private PickleTableConverter() {

    }

    static List<List<String>> toTable(PickleTable pickleTable) {
        return pickleTable.getRowsList().stream()
            .map(row -> row.getCellsList().stream())
            .map(row -> row.map(PickleTableCell::getValue))
            .map(values -> values.collect(toList()))
            .collect(toList());
    }
}
