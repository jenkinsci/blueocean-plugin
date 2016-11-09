import { observable } from 'mobx';
import { Pager } from './Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';
import { Fetch } from '../fetch';
import { BunkerService } from './BunkerService';

export class ActivityService extends BunkerService {
  
    pagerKey(organization, pipeline) {
        return `Activities/${organization}-${pipeline}`;
    }
    activityPager(organization, pipeline) {
        return this.pagerService.getPager({
            key: this.pagerKey(organization, pipeline),
            lazyPager: () => new Pager(RestPaths.activities(organization, pipeline), 25, this),
        });
    }


    getOrAddActivity(activityData) {
        const activity = this.getItem(activityData._links.self.href);
        if (activity) {
            return activity;
        }
        
        return this.setItem(activityData);
    }

    bunkerMapper(data) {
        return this._mapQueueToPsuedoRun(data);
    }
    
    fetchActivity({ organization, pipeline, branch, runId }) {
        return Fetch.fetchJSON(RestPaths.run({ organization, pipeline, branch, runId }))
            .then(data => this.setItem(data));
    }  
    /**
     * This function maps a queue item into a run instancce.
     *
     * We do this because the api returns us queued items as well
     * as runs and its easier to deal with them if they are modeled
     * as the same thing. If the raw data is needed if can be fetched
     * from _item.
     */
    _mapQueueToPsuedoRun(run) {
        if (run._class === 'io.jenkins.blueocean.service.embedded.rest.QueueItemImpl') {
            return {
                id: String(run.expectedBuildNumber),
                state: 'QUEUED',
                pipeline: run.pipeline,
                type: 'QueuedItem',
                result: 'UNKNOWN',
                job_run_queueId: run.id,
                enQueueTime: run.queuedTime,
                organization: run.organization,
                changeSet: [],
                _links: {
                    self: {
                        href: run._links.self.href
                    }
                },
                _item: run,
            };
        }
        return run;
    }
}
