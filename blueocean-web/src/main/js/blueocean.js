import jdlCSS from '@jenkins-cd/design-language/less/theme.less';
import style from '../less/blueocean.less';
import dashboardCSS from '@jenkins-cd/blueocean-dashboard/src/main/less/extensions.less';
import coreCSS from '@jenkins-cd/blueocean-core-js/src/less/blueocean-core-js.less';

try {
    // start the App
    require('./main.jsx');
} catch (e) {
    console.error('Error starting Blue Ocean.', e);
}
