import { observable } from 'mobx';
import { Pager } from '../Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';
import { ActivityModel } from '../model/ActivityModel';
import { Fetch } from '../fetch';

export default class ActivityService {
    constructor(pagerService, sseService) {
        this.pagerService = pagerService;
        this.sseService = sseService;
        this.sseService.registerHandler((event) => this._sseEventHandler(event));
    }
    @observable
    bunker = new DataBunker(this._keyFn, this._mapperFn);

    activityPager(organization, pipeline) {
        return this.pagerService.getPager({
            key: `Activities/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.activities(organization, pipeline), 25, this.bunker),
        });
    }

    getOrAddActivity(activityData) {
        const activity = this.bunker.getItem(activityData._links.self.href);
        if (activity) {
            return activity;
        }
        
        return this.bunker.setItem(activityData);
    }

    _keyFn(item) {
        return item._links.self.href;
    }

    _mapperFn(item) {
        return new ActivityModel(item, x => x);
    }

    _sseEventHandler(event) {
        switch (event.jenkins_event) {
        case 'job_run_queue_buildable':
        case 'job_run_queue_enter':
            this.props.processJobQueuedEvent(eventCopy);
            break;
        case 'job_run_queue_left':
            this.props.processJobLeftQueueEvent(eventCopy);
            break;
        case 'job_run_queue_blocked': {
            break;
        }
        case 'job_run_started': {
            this.props.updateRunState(eventCopy, this.context.config, true);
            this.props.updateBranchState(eventCopy, this.context.config);
            break;
        }
        case 'job_run_ended': {
            this.props.updateRunState(eventCopy, this.context.config);
            this.props.updateBranchState(eventCopy, this.context.config);
            break;
        }
        default :
        // Else ignore the event.

        }
    }
}
