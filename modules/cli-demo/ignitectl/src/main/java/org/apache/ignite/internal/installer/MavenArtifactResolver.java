/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.installer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import io.micronaut.core.annotation.Introspected;
import org.apache.ignite.internal.v2.builtins.SystemPathResolver;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorBuilder;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;

/**
 *
 */
@Singleton
@Introspected
public class MavenArtifactResolver {

    private final SystemPathResolver pathResolver;

    @Inject
    public MavenArtifactResolver(SystemPathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public ResolveResult resolve(
        Path mavenRoot,
        String grpId,
        String artifactId,
        String version
    ) throws IOException {
        // create an ivy instance
        File tmpDir = Files.createTempDirectory("ignite-installer-cache").toFile();
        tmpDir.deleteOnExit();

        IvySettings ivySettings = new IvySettings();
        ivySettings.setDefaultCache(tmpDir);
        ivySettings.setDefaultCacheArtifactPattern("[artifact](-[classifier]).[revision].[ext]");

        ChainResolver chainResolver = new ChainResolver();
        chainResolver.setName("chainResolver");
        // use the biblio resolver, if you consider resolving
        // POM declared dependencies
        IBiblioResolver br = new IBiblioResolver();
        br.setM2compatible(true);
        br.setUsepoms(true);
        br.setName("central");

        chainResolver.add(br);

        IBiblioResolver localBr = new IBiblioResolver();
        localBr.setM2compatible(true);
        localBr.setUsepoms(true);
        localBr.setRoot("file://" + pathResolver.osHomeDirectoryPath().resolve(".m2").resolve("repository"));
        localBr.setName("local");
        chainResolver.add(localBr);

        ivySettings.addResolver(chainResolver);
        ivySettings.setDefaultResolver(chainResolver.getName());

        Ivy ivy = Ivy.newInstance(ivySettings);


        // Step 1: you always need to resolve before you can retrieve
        //
        ResolveOptions ro = new ResolveOptions();
        // this seems to have no impact, if you resolve by module descriptor
        //
        // (in contrast to resolve by ModuleRevisionId)
        ro.setTransitive(true);
        // if set to false, nothing will be downloaded
        ro.setDownload(true);

        // 1st create an ivy module (this always(!) has a "default" configuration already)
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(
            // give it some related name (so it can be cached)
            ModuleRevisionId.newInstance(
                "org.apache.ignite",
                "installer-envelope",
                "working"
            )
        );

        // 2. add dependencies for what we are really looking for
        ModuleRevisionId ri = ModuleRevisionId.newInstance(
            grpId,
            artifactId,
            version
        );
        // don't go transitive here, if you want the single artifact
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, false, false, true);

        // map to master to just get the code jar. See generated ivy module xmls from maven repo
        // on how configurations are mapped into ivy. Or check
        // e.g. http://lightguard-jp.blogspot.de/2009/04/ivy-configurations-when-pulling-from.html
        dd.addDependencyConfiguration("default", "master");
        dd.addDependencyConfiguration("default", "runtime");
        dd.addDependencyConfiguration("default", "compile");

        md.addDependency(dd);

        try {
            // now resolve
            ResolveReport rr = ivy.resolve(md,ro);

            if (rr.hasError())
                throw new RuntimeException(rr.getAllProblemMessages().toString());

            // Step 2: retrieve
            ModuleDescriptor m = rr.getModuleDescriptor();

            RetrieveReport retrieveReport = ivy.retrieve(
                m.getModuleRevisionId(),
                new RetrieveOptions()
                    // this is from the envelop module
                    .setConfs(new String[] {"default"})
                    .setDestArtifactPattern(mavenRoot.toFile().getAbsolutePath() + "/[artifact](-[classifier]).[revision].[ext]")
            );


            return new ResolveResult(
                retrieveReport.getCopiedFiles().stream().map(File::getName).collect(Collectors.toList())
            );
        }
        catch (ParseException e) {
            // TOOD
            throw new IOException(e);
        }
    }

    public void resolveDeps(
        Path moduleRoot,
        Path cliRoot,
        String grpId,
        String artifactId,
        String version
    ) throws IOException {
        // create an ivy instance
        File tmpDir = Files.createTempDirectory("ignite-installer-cache").toFile();
        tmpDir.deleteOnExit();

        IvySettings ivySettings = new IvySettings();
        ivySettings.setDefaultCache(tmpDir);
        ivySettings.setDefaultCacheArtifactPattern("[artifact](-[classifier]).[revision].[ext]");

        ChainResolver chainResolver = new ChainResolver();
        chainResolver.setName("chainResolver");
        // use the biblio resolver, if you consider resolving
        // POM declared dependencies
        IBiblioResolver br = new IBiblioResolver();
        br.setM2compatible(true);
        br.setUsepoms(true);
        br.setName("central");

        chainResolver.add(br);

        IBiblioResolver localBr = new IBiblioResolver();
        localBr.setM2compatible(true);
        localBr.setUsepoms(true);
        localBr.setRoot("file://" + pathResolver.osHomeDirectoryPath().resolve(".m2").resolve("repository/"));
        localBr.setName("local");
        chainResolver.add(localBr);

        ivySettings.addResolver(chainResolver);
        ivySettings.setDefaultResolver(chainResolver.getName());

        Ivy ivy = Ivy.newInstance(ivySettings);


        // Step 1: you always need to resolve before you can retrieve
        //
        ResolveOptions ro = new ResolveOptions();
        // this seems to have no impact, if you resolve by module descriptor
        //
        // (in contrast to resolve by ModuleRevisionId)
        ro.setTransitive(true);
        // if set to false, nothing will be downloaded
        ro.setDownload(true);

        // 1st create an ivy module (this always(!) has a "default" configuration already)
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(
            // give it some related name (so it can be cached)
            ModuleRevisionId.newInstance(
                "org.apache.ignite",
                "installer-envelope",
                "working"
            )
        );

        // 2. add dependencies for what we are really looking for
        ModuleRevisionId ri = ModuleRevisionId.newInstance(
            grpId,
            artifactId,
            version
        );
        // don't go transitive here, if you want the single artifact
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, false, true, true);

        // map to master to just get the code jar. See generated ivy module xmls from maven repo
        // on how configurations are mapped into ivy. Or check
        // e.g. http://lightguard-jp.blogspot.de/2009/04/ivy-configurations-when-pulling-from.html
        dd.addDependencyConfiguration("default", "master");
        dd.addDependencyConfiguration("default", "runtime");
        dd.addDependencyConfiguration("default", "compile");

        md.addDependency(dd);

        try {
            // now resolve base -all artifact
            ResolveReport rr = ivy.resolve(md,ro);

            if (rr.hasError())
                throw new RuntimeException(rr.getAllProblemMessages().toString());

            // Step 2: retrieve
            DependencyDescriptor m = rr.getModuleDescriptor().getDependencies()[0];

            // resolve two artifacts for module and module-cli
            List<IvyNode> deps = ivy.resolve(m.getDependencyRevisionId(), ro.setTransitive(true), true)
                .getDependencies();
            deps = Arrays.asList(deps.get(1), deps.get(2));

            assert(deps.size() == 2);
            assert(deps.stream().allMatch(d -> d.getAllArtifacts().length == 1));
            assert(deps.stream().filter(d -> d.getModuleRevision().getId().getName().endsWith("-cli")).count() == 1);

            for (IvyNode dep: deps) {
                Artifact artifact = dep.getAllArtifacts()[0];
                if (dep.getModuleRevision().getId().getName().endsWith("-cli")) {
                    String id = ivy.resolve(
                        dep.getResolvedId(),
                        ro, true
                    ).getResolveId();
                    RetrieveReport retrieveReport = ivy.retrieve(
                        dep.getId(),
                        new RetrieveOptions()
                            // this is from the envelop module
                            .setConfs(new String[] {"default"})
                            .setResolveId(id)
                            .setDestArtifactPattern(cliRoot.toFile().getAbsolutePath() + "/[artifact](-[classifier]).[revision].[ext]")
                    );
                }
                else {
                    String id = ivy.resolve(
                        dep.getResolvedId(),
                        ro, true
                    ).getResolveId();
                    ivy.retrieve(
                        dep.getResolvedId(),
                        new RetrieveOptions()
                            // this is from the envelop module
                            .setResolveId(id)
                            .setConfs(new String[] {"default"})
                            .setDestArtifactPattern(moduleRoot.toFile().getAbsolutePath() + "/[artifact](-[classifier]).[revision].[ext]")
                    );
                }
            }
        }
        catch (ParseException e) {
            // TOOD
            throw new IOException(e);
        }
    }
}
