package io.jenkins.blueocean.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.jenkinsci.maven.plugins.hpi.AbstractJenkinsMojo;
import org.jenkinsci.maven.plugins.hpi.MavenArtifact;

import net.sf.json.JSONObject;

/**
 * Goal which copies upstream blueocean javascript locally
 */
@Mojo(name = "process-node-dependencies", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ProcessUpstreamDependenciesMojo extends AbstractJenkinsMojo {
	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = "${project.basedir}", property = "baseDir", required = true)
	private File baseDir;

	/**
	 * Location of the node_modules
	 */
	@Parameter(defaultValue = "${project.basedir}/node_modules", property = "nodeModulesDir", required = false)
	private File nodeModulesDirectory;
	
    /**
     * The maven project.
     */
    @Component
    protected MavenProject project;
    
    @Component
    protected DependencyGraphBuilder graphBuilder;
    
	public void execute() throws MojoExecutionException {
		List<MavenArtifact> artifacts = new ArrayList<>();
		try {
			collectBlueoceanDependencies(graphBuilder.buildDependencyGraph(project, null), artifacts);
			
			File pluginsDir = nodeModulesDirectory; // new File(nodeModulesDirectory, "blueocean-plugins");
			
			if (!pluginsDir.exists()) {
				pluginsDir.mkdirs();
			}
	
			for (MavenArtifact artifact : artifacts) {
				List<Contents> jarEntries = findJarEntries(new FileInputStream(artifact.getFile()), "package.json");
				
				System.out.println("Using artifact: " + artifact.getArtifactId());
				
				JSONObject packageJson = JSONObject.fromObject(new String(jarEntries.get(0).data));
				
				String name = packageJson.getString("name");
				String[] subdirs = name.split("/");
				
				File outDir = nodeModulesDirectory;
				for (int i = 0; i < subdirs.length; i++) {
					outDir = new File(outDir, subdirs[i]);
				}
				
				File artifactFile = artifact.getFile();
				long artifactLastModified = artifactFile.lastModified();
				
				if (!outDir.exists()) {
					outDir.mkdirs();
				}

				try (ZipInputStream jar = new ZipInputStream(new FileInputStream(artifact.getFile()))) {
					ZipEntry entry;
					while ((entry = jar.getNextEntry()) != null) {
						if (entry.isDirectory()) {
							continue;
						}
						File outFile = new File(outDir, entry.getName());
						if (!outFile.exists() || outFile.lastModified() < artifactLastModified) {
							System.out.println("Copying module: " + outFile.getAbsolutePath());
							File parentFile = outFile.getParentFile();
							if (!parentFile.exists()) {
								parentFile.mkdirs();
							}
							try (FileOutputStream out = new FileOutputStream(outFile)) {
								int read = 0;
								byte[] buf = new byte[4096];
								while((read = jar.read(buf)) >= 0) {
									out.write(buf, 0, read);
								}
							}
						}
					}
				}
			}
		} catch (DependencyGraphBuilderException | IOException e) {
			throw new RuntimeException(e);
		}
		
		System.out.println("Done installing blueocean dependencies for " + project.getArtifactId());
	}
	
	private class Contents {
		public Contents(String fileName, byte[] data) {
			super();
			this.fileName = fileName;
			this.data = data;
		}
		String fileName;
		byte[] data;
	}
	
	private List<Contents> findJarEntries(InputStream jarFile, String pathGlob) throws IOException {
		List<Contents> out = new ArrayList<>();
		Pattern matcher = Pattern.compile(
			("\\Q" + pathGlob.replace("**", "\\E\\Q").replace("*", "\\E[^/]*\\Q").replace("\\E\\Q", "\\E.*\\Q") + "\\E").replace("\\Q\\E", "")
		);
		try (ZipInputStream jar = new ZipInputStream(jarFile)) {
			ZipEntry entry;
			while ((entry = jar.getNextEntry()) != null) {
				if (matcher.matcher(entry.getName()).matches()) {
					ByteArrayOutputStream bo = new ByteArrayOutputStream((int)entry.getSize());
					int read = 0;
					byte[] buf = new byte[4096];
					while((read = jar.read(buf)) >= 0) {
						bo.write(buf, 0, read);
					}
					out.add(new Contents(entry.getName(), bo.toByteArray()));
				}
			}
		}
		return out;
	}
	
	private void collectBlueoceanDependencies(DependencyNode node, List<MavenArtifact> results) throws FileNotFoundException, IOException {
		MavenArtifact artifact = wrap(node.getArtifact());
		try {
			List<Contents> jarEntries = findJarEntries(new FileInputStream(artifact.getFile()), "package.json");
			if (jarEntries.size() > 0) {
				results.add(artifact);
			}
		} catch(Exception e) {
			System.out.println("Unable to find artifact: " + artifact);

			MavenArtifact hpi = artifact.getHpi();
			try {
				if (hpi != null) {
					List<Contents> jarEntries = findJarEntries(new FileInputStream(hpi.getFile()), "WEB-INF/lib/"+artifact.getArtifactId()+".jar");
					if (jarEntries.size() > 0) {
						results.add(hpi);
					}
				}
			} catch(Exception e2) {
				System.out.println("Unable to find hpi artifact for: " + hpi);
			}
		}
		
		for (DependencyNode child : node.getChildren()) {
			collectBlueoceanDependencies(child, results);
		}
	}
}
