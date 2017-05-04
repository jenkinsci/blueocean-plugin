package io.jenkins.blueocean.resources;

import java.net.URL;
import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A list of resources with some utility functions
 */
class ResourceList<T> extends ArrayList<Resource<T>> {
	private static final long serialVersionUID = 1L;
	
	long getLastModified() {
		long lastModified = 0;
		for (Resource<T> res : this) {
			if (lastModified < res.getLastModified()) {
				lastModified = res.getLastModified();
			}
			if (lastModified < res.dependencies.getLastModified()) {
				lastModified = res.dependencies.getLastModified();
			}
		}
		return lastModified;
	}
	
	@CheckForNull Resource<T> find(@Nonnull URL u) {
		for (Resource<T> r : this) {
			if (u.equals(r.url)) {
				return r;
			}
		}
		return null;
	}
}