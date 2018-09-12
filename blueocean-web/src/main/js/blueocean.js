import jdlCSS from '@jenkins-cd/design-language/dist/assets/css/jenkins-design-language.css';
import style from '../less/blueocean.less';
import dashboardCSS from '@jenkins-cd/blueocean-dashboard/dist/assets/css/blueocean-dashboard.css';

try {
    // start the App
    require('./main.jsx');
} catch (e) {
    console.error('Error starting Blue Ocean.', e);
}
