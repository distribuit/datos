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
package com.datos.vfs.provider.res;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.datos.vfs.provider.AbstractFileProvider;
import com.datos.vfs.provider.UriParser;
import com.datos.vfs.Capability;
import com.datos.vfs.FileObject;
import com.datos.vfs.FileSystem;
import com.datos.vfs.FileSystemConfigBuilder;
import com.datos.vfs.FileSystemException;
import com.datos.vfs.FileSystemOptions;

/**
 * The Resource provider.
 */
public class ResourceFileProvider extends AbstractFileProvider
{
    /** The provider's capabilities */
    protected static final Collection<Capability> capabilities =
        Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.DISPATCHER
    }));

    private static final int BUFFER_SIZE = 80;

    public ResourceFileProvider()
    {
        super();
    }

    /**
     * Locates a file object, by absolute URI.
     * @param baseFile The base file.
     * @param uri The URI of the file to locate.
     * @param fileSystemOptions The FileSystem options.
     * @return the FileObject.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileObject findFile(final FileObject baseFile,
                               final String uri,
                               final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        final StringBuilder buf = new StringBuilder(BUFFER_SIZE);
        UriParser.extractScheme(uri, buf);
        final String resourceName = buf.toString();

        ClassLoader cl = ResourceFileSystemConfigBuilder.getInstance().getClassLoader(fileSystemOptions);
        if (cl == null)
        {
            cl = getClass().getClassLoader();
        }
        final URL url = cl.getResource(resourceName);

        if (url == null)
        {
            throw new FileSystemException("vfs.provider.url/badly-formed-uri.error", uri);
        }

        return getContext().getFileSystemManager().resolveFile(url.toExternalForm());
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder()
    {
        return ResourceFileSystemConfigBuilder.getInstance();
    }

    @Override
    public void closeFileSystem(final FileSystem filesystem)
    {
        // no filesystem created here - so nothing to do
    }

    @Override
    public Collection<Capability> getCapabilities()
    {
        return capabilities;
    }
}
