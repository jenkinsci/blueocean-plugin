import { action, observable } from 'mobx';


class PipelinesActive {

    @observable
    isActive = false;

    @action
    setActive() {
        this.isActive = true;
    }

    @action
    setInactive() {
        this.isActive = false;
    }

}

export default new PipelinesActive();
