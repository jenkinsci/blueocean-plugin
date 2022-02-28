package io.jenkins.blueocean.service.embedded.rest;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.Run;
import io.jenkins.blueocean.commons.IterableUtils;
import io.jenkins.blueocean.commons.ServiceException.BadRequestException;
import io.jenkins.blueocean.commons.ServiceException.NotFoundException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory;
import io.jenkins.blueocean.rest.factory.BlueTestResultFactory.Result;
import io.jenkins.blueocean.rest.model.BlueTestResult;
import io.jenkins.blueocean.rest.model.BlueTestResult.State;
import io.jenkins.blueocean.rest.model.BlueTestResult.Status;
import io.jenkins.blueocean.rest.model.BlueTestResultContainer;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class BlueTestResultContainerImpl extends BlueTestResultContainer {
    protected final Run<?, ?> run;

    public BlueTestResultContainerImpl(Reachable parent, Run<?, ?> run) {
        super(parent);
        this.run = run;
    }

    protected Result resolve() {
        return BlueTestResultFactory.resolve(run, parent);
    }

    @Override
    public BlueTestResult get(final String name) {
        Result resolved = resolve();

        if (resolved.summary == null || resolved.results == null) {
            throw new NotFoundException("no tests");
        }
        BlueTestResult testResult = IterableUtils.find(resolved.results,
             blueTestResult -> blueTestResult != null && blueTestResult.getId().equals(name),
            null);
        if (testResult == null) {
            throw new NotFoundException("not found");
        }
        return testResult;
    }

    @NonNull
    @Override
    public Iterator<BlueTestResult> iterator() {
        Result resolved = resolve();
        if (resolved.summary == null || resolved.results == null) {
            throw new NotFoundException("no tests");
        }
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request != null) {
            String status = request.getParameter("status");
            String state = request.getParameter("state");
            String age = request.getParameter("age");
            return getBlueTestResultIterator(resolved.results, status, state, age);

        }
        return resolved.results.iterator();
    }

    // for testing purpose
    public Iterator<BlueTestResult> getBlueTestResultIterator(Iterable<BlueTestResult> results, String status, String state, String age) {
        if (isEmpty(status) && isEmpty(state) && isEmpty(age)) {
            return results.iterator();
        }

        Predicate<BlueTestResult> predicate = blueTestResult -> true;

        if (!isEmpty(status)) {
            predicate =  predicate.and(filterByStatus(status));
        }
        if (!isEmpty(state)) {
            predicate = predicate.and(filterByState(state));
        }
        if (!isEmpty(age)) {
            predicate = predicate.and(filterByAge(age));
        }
        return StreamSupport.stream(results.spliterator(), false).filter(predicate).iterator();
    }

    // for testing purpose
    public static Predicate<BlueTestResult> filterByAge(String age) {
        int _age;
        try {
            _age = Integer.parseInt(age);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("age is not a number");
        }
        return new AgePredicate(_age);
    }


    // for testing purpose
    public static Predicate<BlueTestResult> filterByStatus(String status) {
        String[] statusAtoms = StringUtils.split(status, ',');
        Predicate<BlueTestResult> predicate = blueTestResult -> false;
        if (statusAtoms == null || statusAtoms.length == 0) {
            throw new BadRequestException("status not provided");
        }
        for (String statusString : statusAtoms) {
            Predicate<BlueTestResult> statusPredicate;
            try {
                if (statusString.startsWith("!")) {
                    StatusPredicate tmp = new StatusPredicate(Status.valueOf(statusString.toUpperCase().substring(1)));
                    statusPredicate = blueTestResult -> !tmp.test(blueTestResult);
                } else {
                    statusPredicate = new StatusPredicate(Status.valueOf(statusString.toUpperCase()));
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("bad status " + status, e);
            }
            predicate = predicate.or(statusPredicate);
        }
        return predicate;
    }

    // for testing purpose
    public static Predicate<BlueTestResult> filterByState(String state) {
        String[] stateAtoms = StringUtils.split(state, ',');
        Predicate<BlueTestResult> predicate = blueTestResult -> false;
        if (stateAtoms == null || stateAtoms.length == 0) {
            throw new BadRequestException("state not provided");
        }

        for (String stateString : stateAtoms) {
            Predicate<BlueTestResult> statePredicate;
            try {
                if (stateString.startsWith("!")) {
                    StatePredicate tmp = new StatePredicate(State.valueOf(stateString.toUpperCase().substring(1)));
                    statePredicate = blueTestResult -> !tmp.test(blueTestResult);
                } else {
                    statePredicate = new StatePredicate(State.valueOf(stateString.toUpperCase()));
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("bad state " + state, e);
            }
            predicate = predicate.or(statePredicate);
        }
        return predicate;
    }

    static class StatusPredicate implements Predicate<BlueTestResult> {

        private final Status status;

        StatusPredicate(Status status) {
            this.status = status;
        }

        @Override
        public boolean test(@Nullable BlueTestResult input) {
            return input != null && input.getStatus().equals(status);
        }
    }

    static class StatePredicate implements Predicate<BlueTestResult> {
        private final State state;

        StatePredicate(State state) {
            this.state = state;
        }

        @Override
        public boolean test(@Nullable BlueTestResult input) {
            return input != null && input.getTestState().equals(state);
        }
    }

    static class AgePredicate implements Predicate<BlueTestResult> {
        private final Integer age;

        public AgePredicate(Integer age) {
            this.age = age;
        }

        @Override
        public boolean test(@Nullable BlueTestResult input) {
            if (input == null) { return false; }

            // positive means more age
            if (this.age > 0) {
                return input.getAge() >= this.age;
            }

            // negative means less age
            if (this.age < 0) {
                return input.getAge() <= Math.abs(this.age);
            }

            return input.getAge() == age;
        }
    }
}
