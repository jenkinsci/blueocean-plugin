import React from 'react';
import executorInfoService from './ExecutorInfoService';
import { observer } from 'mobx-react';
import { Icon } from '@jenkins-cd/design-language';

@observer
export class ItemExecutorInfo extends React.Component {
    render() {
        const { pipeline } = this.props;
        if (!pipeline || !pipeline.latestRun) {
            return null;
        }
        const executors = executorInfoService.computers &&
            [].concat.apply([], executorInfoService.computers.map(computer => {
                return computer.executors.map(executor => {
                    if (!executor.idle && executor.run) {
                        if (pipeline.latestRun._links.self.href === executor.run._links.self.href) {
                            return executor;
                        }
                    }
                    return null;
                });
            })).filter(executor => executor !== null);
        if (executors.length === 0) {
            return null;
        }
        return (
            <span className="executor-info" title={`${pipeline.name} is using: ${executors.map(e => e.displayName).join(', ')}`}>
                <Icon size={24} icon="HardwareComputer" color="rgba(53, 64, 82, 0.5)" />
            </span>
        );
    }
};

export default ItemExecutorInfo;
