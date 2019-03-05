import React from 'react';
import executorInfoService from './ExecutorInfoService';
import { observer } from 'mobx-react';
import { JTable, TableHeaderRow, TableRow, TableCell, StatusIndicator, Icon } from '@jenkins-cd/design-language';

const columns = [
    JTable.column(10, '', false),
    JTable.column(60, 'Computers', true),
];

@observer
export class ExecutorInfoPage extends React.Component {
    render() {
        return (
            <JTable columns={columns} className="executor-info-table">
                <TableHeaderRow />
                {executorInfoService.computers && executorInfoService.computers.map(computer => [
                    <TableRow>
                        <TableCell>
                            <Icon size={24} icon="HardwareComputer" color="rgba(53, 64, 82, 0.5)" />
                        </TableCell>
                        <TableCell>
                            {computer.displayName}
                        </TableCell>
                    </TableRow>].concat(computer.executors.map(executor =>
                    <TableRow>
                        <TableCell>
                        {!executor.idle && <StatusIndicator result="running" percentage={1000} />}
                        </TableCell>
                        <TableCell className="executor-info-cell">
                            <Icon size={18} icon="NavigationSubdirectoryArrowRight" color="rgba(53, 64, 82, 0.5)" />
                            {executor.displayName}
                        </TableCell>
                    </TableRow>))
                )}
            </JTable>
        );
    }
};
