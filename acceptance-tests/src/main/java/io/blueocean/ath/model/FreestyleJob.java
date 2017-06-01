package io.blueocean.ath.model;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class FreestyleJob extends Pipeline {
    @AssistedInject
    public FreestyleJob(@Assisted String name) {
        super(name);
    }

    @AssistedInject
    public FreestyleJob(@Assisted Folder folder, @Assisted String name) {
        super(folder, name);
    }
}
