/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.core.cache;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.repository.BasicResource;
import org.apache.ivy.plugins.resolver.MockResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

/**
 * @see DefaultResolutionCacheManager
 */
public class DefaultRepositoryCacheManagerTest extends TestCase {
    private DefaultRepositoryCacheManager cacheManager;

    private Artifact artifact;

    private ArtifactOrigin origin;

    protected void setUp() throws Exception {
        File f = new File("./tmp"); File.createTempFile("ivycache", ".dir");
        Ivy ivy = new Ivy();
        ivy.configureDefault();
        IvySettings settings = ivy.getSettings();
        f.delete(); // we want to use the file as a directory, so we delete the file itself
        cacheManager = new DefaultRepositoryCacheManager();
        cacheManager.setSettings(settings);
        cacheManager.setBasedir(f);

        artifact = createArtifact("org", "module", "rev", "name", "type", "ext");
        origin = new ArtifactOrigin(artifact, true, "/some/where");
        cacheManager.saveArtifactOrigin(artifact, origin);
    }

    protected void tearDown() throws Exception {
        // Delete del = new Delete();
        // del.setProject(new Project());
        // del.setDir(cacheManager.getRepositoryCacheRoot());
        // del.execute();
    }
    
    public void testFoo() throws Exception {
        ModuleId mi = new ModuleId("org", "module");
        ModuleRevisionId mridLatest = new ModuleRevisionId(mi, "trunk", "latest.integration");
        DependencyDescriptor ddLatest = new DefaultDependencyDescriptor(mridLatest,  false);
        CacheMetadataOptions options = new CacheMetadataOptions().setCheckTTL(false);
        MockResolver resolver = new MockResolver();
        
        ModuleRevisionId mrid11 = new ModuleRevisionId(mi, "trunk", "1.1");
        DependencyDescriptor dd11 = new DefaultDependencyDescriptor(mrid11,  false);
        DefaultArtifact artifact11 = new DefaultArtifact(mrid11, new Date(), "module-1.1.ivy", "ivy", "ivy", true);
        BasicResource resource11 = new BasicResource("/module-1-1.ivy", true, 1, 0, true);
        ResolvedResource mdRef11 = new ResolvedResource(resource11, "1.1");
        cacheManager.cacheModuleDescriptor(resolver, mdRef11, dd11, artifact11, null, options);
        
        cacheManager.saveResolvedRevision(mridLatest, "1.1");
        // cacheManager.saveResolvers(md, metadataResolverName, artifactResolverName)
        
        ResolvedModuleRevision rmr = cacheManager.findModuleInCache(ddLatest, mridLatest, options, "resolver1");
        System.out.println(rmr);
    }

    public void testArtifactOrigin() {
        ArtifactOrigin found = cacheManager.getSavedArtifactOrigin(artifact);
        assertEquals(origin, found);

        artifact = createArtifact("org", "module", "rev", "name", "type2", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));
    }

    public void testUniqueness() {
        cacheManager.saveArtifactOrigin(artifact, origin);

        artifact = createArtifact("org1", "module", "rev", "name", "type", "ext");
        ArtifactOrigin found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module1", "rev", "name", "type", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev1", "name", "type", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev", "name1", "type", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev", "name", "type1", "ext");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));

        artifact = createArtifact("org", "module", "rev", "name", "type", "ext1");
        found = cacheManager.getSavedArtifactOrigin(artifact);
        assertTrue(ArtifactOrigin.isUnknown(found));
    }

    protected static DefaultArtifact createArtifact(String org, String module, String rev, String name,
            String type, String ext) {
        ModuleId mid = new ModuleId(org, module);
        ModuleRevisionId mrid = new ModuleRevisionId(mid, rev);
        return new DefaultArtifact(mrid, new Date(), name, type, ext);
    }

}
