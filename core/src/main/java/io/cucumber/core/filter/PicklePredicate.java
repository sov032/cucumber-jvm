package io.cucumber.core.filter;

import io.cucumber.messages.Messages.Pickle;

interface PicklePredicate {

    boolean apply(Pickle Pickle);
}
