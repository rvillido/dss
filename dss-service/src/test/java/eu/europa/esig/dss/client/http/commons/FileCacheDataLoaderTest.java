/**
 * DSS - Digital Signature Services
 * Copyright (C) 2015 European Commission, provided under the CEF programme
 *
 * This file is part of the "DSS - Digital Signature Services" project.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.europa.esig.dss.client.http.commons;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileCacheDataLoaderTest {

    static final String URL_TO_LOAD = "https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl-mp.xml";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private FileCacheDataLoader dataLoader;
    private File cacheDirectory;

    @Before
    public void setUp() throws Exception {
        cacheDirectory = testFolder.newFolder("dss-file-cache");
        dataLoader = new FileCacheDataLoader();
        dataLoader.setFileCacheDirectory(cacheDirectory);
    }

    @Test
    public void getUrl_whenExpirationTimeIsNotSet_useCachedFile() throws Exception {
        long cacheCreationTime = getUrlAndReturnCacheCreationTime();
        waitOneSecond();
        long newCacheCreationTime = getUrlAndReturnCacheCreationTime();
        assertEquals(cacheCreationTime, newCacheCreationTime);
    }

    @Test
    public void getUrl_whenCacheIsNotExpired_useCachedFile() throws Exception {
        dataLoader.setCacheExpirationTime(2000L);
        long cacheCreationTime = getUrlAndReturnCacheCreationTime();
        waitOneSecond();
        long newCacheCreationTime = getUrlAndReturnCacheCreationTime();
        assertEquals(cacheCreationTime, newCacheCreationTime);
    }

    @Test
    public void getUrl_whenCacheIsExpired_downloadNewCopy() throws Exception {
        dataLoader.setCacheExpirationTime(500L);
        long cacheCreationTime = getUrlAndReturnCacheCreationTime();
        waitOneSecond();
        long newCacheCreationTime = getUrlAndReturnCacheCreationTime();
        assertTrue(cacheCreationTime < newCacheCreationTime);
    }

    private long getUrlAndReturnCacheCreationTime() {
        byte[] bytesArray = dataLoader.get(URL_TO_LOAD);
        assertTrue(bytesArray.length > 0);
        File cachedFile = getCachedFile(cacheDirectory);
        return cachedFile.lastModified();
    }

    private File getCachedFile(File cacheDirectory) {
        File cachedFile = null;
        if(cacheDirectory.exists()) {
            File[] files = cacheDirectory.listFiles();
            if(files != null && files.length > 0) {
                cachedFile = files[0];
            }
        }
        return cachedFile;
    }

    private void waitOneSecond() throws InterruptedException {
        Thread.sleep(1000); // Sleeping is necessary to verify changes in the cache creation time
    }
}
