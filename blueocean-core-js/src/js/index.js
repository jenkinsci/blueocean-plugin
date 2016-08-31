
export { Fetch, FetchFunctions } from './fetch';
export UrlConfig from './urlconfig';
export JWT from './jwt';
export TestUtils from './testutils';
export Utils from './utils';

/**
 * Created by cmeyers on 8/18/16.
 */
import { ToastService }  from './ToastService';

// export ToastService as a singleton so all plugins will use the same instance
// otherwise toasts from plugins will not be displayed in blueocean-web UI
const toastService = new ToastService();
export { toastService as ToastService };
