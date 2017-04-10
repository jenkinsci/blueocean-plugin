import { CredentialsApi } from './CredentialsApi';
import { CredentialsManager } from './CredentialsManager';

const api = new CredentialsApi();
const manager = new CredentialsManager(api);

export { manager as credentialsManager };
