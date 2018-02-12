package io.jenkins.blueocean.service.embedded.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

final class AnalyticsConfig {

    public static final AnalyticsConfig EMPTY = new AnalyticsConfig(0, new ArrayList<>());

    @JsonProperty("cohorts")
    final int cohorts;
    @JsonProperty("activeCohorts")
    final List<String> activeCohorts;

    AnalyticsConfig(
        @JsonProperty("cohorts") int cohorts,
        @JsonProperty("activeCohorts") List<String> activeCohorts
    ) {
        this.cohorts = cohorts;
        this.activeCohorts = activeCohorts;
    }

    List<Integer> allActiveCohorts() {
        List<Integer> allActiveCohorts = new ArrayList<>();
        for (String rawCohort : activeCohorts) {
            int pos = rawCohort.indexOf("-");
            if (pos > -1) {
                int lower = Integer.parseInt(rawCohort.substring(0, pos));
                int upper = Integer.parseInt(rawCohort.substring(pos+1, rawCohort.length()));
                for (int i = lower; i < (upper + 1); i++) {
                    allActiveCohorts.add(i);
                }
            } else {
                allActiveCohorts.add(Integer.parseInt(rawCohort));
            }
        }

        return allActiveCohorts;
    }
}
