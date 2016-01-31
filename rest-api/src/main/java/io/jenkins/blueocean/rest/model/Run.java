package io.jenkins.blueocean.rest.model;

import java.util.Date;

/**
 * Represents Pipeline run
 *
 * TODO: Make it immutable
 *
 * @author Vivek Pandey
 **/
public class Run {
    public String id;
    public String name;
    public String status;
    public Date startTime;
    public Date endTime;
    public long durationInMillis;
}
