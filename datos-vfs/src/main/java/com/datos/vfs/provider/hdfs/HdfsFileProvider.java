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
package com.datos.vfs.provider.hdfs;

import com.datos.vfs.*;
import com.datos.vfs.provider.AbstractFileProvider;
import com.datos.vfs.provider.AbstractOriginatingFileProvider;
import com.datos.vfs.provider.http.HttpFileNameParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * FileProvider for HDFS files.
 *
 * @since 2.1
 */
public class HdfsFileProvider extends AbstractOriginatingFileProvider {
    static final Collection<Capability> CAPABILITIES = Collections.unmodifiableCollection(Arrays
            .asList(new Capability[]
                    {
                            Capability.GET_TYPE,
                            Capability.READ_CONTENT,
                            Capability.URI,
                            Capability.GET_LAST_MODIFIED,
                            Capability.ATTRIBUTES,
                            Capability.RANDOM_ACCESS_READ,
                            Capability.DIRECTORY_READ_CONTENT,
                            Capability.LIST_CHILDREN,
                            Capability.APPEND_CONTENT}));

    /**
     * Constructs a new HdfsFileProvider.
     */
    public HdfsFileProvider() {
        super();
        this.setFileNameParser(HttpFileNameParser.getInstance());
    }

    /**
     * Create a new HdfsFileSystem instance.
     *
     * @param rootName          Name of the root file.
     * @param fileSystemOptions Configuration options for this instance.
     * @throws FileSystemException if error occurred.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return new HdfsFileSystem(rootName, fileSystemOptions);
    }

    /**
     * Get Capabilities of HdfsFileSystem.
     *
     * @return The capabilities (unmodifiable).
     */
    @Override
    public Collection<Capability> getCapabilities() {
        return CAPABILITIES;
    }

    /**
     * Return config builder.
     *
     * @return A config builder for HdfsFileSystems.
     * @see AbstractFileProvider#getConfigBuilder()
     */
    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return HdfsFileSystemConfigBuilder.getInstance();
    }

}
