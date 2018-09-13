import jdlCSS from '@jenkins-cd/design-language/dist/assets/css/jenkins-design-language.css';
import style from '../less/blueocean.less';
import dashboardCSS from '@jenkins-cd/blueocean-dashboard/dist/assets/css/blueocean-dashboard.css';
import coreCSS from '@jenkins-cd/blueocean-core-js/dist/assets/css/blueocean-core-js.css';

try {
    // start the App
    require('./main.jsx');
} catch (e) {
    console.error('Error starting Blue Ocean.', e);
}
