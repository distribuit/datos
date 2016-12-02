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
package com.datos.vfs.provider.ftps;

import com.datos.vfs.FileSystemConfigBuilder;
import com.datos.vfs.provider.ftp.FtpFileProvider;
import com.datos.vfs.FileName;
import com.datos.vfs.FileSystem;
import com.datos.vfs.FileSystemException;
import com.datos.vfs.FileSystemOptions;
import com.datos.vfs.provider.GenericFileName;

/**
 * A provider for FTP file systems.
 *
 * NOTE: Most of the heavy lifting for FTPS is done by the com.datos.vfs.provider.ftp package since
 * they both use commons-net package
 *
 * @since 2.0
 */
public class FtpsFileProvider extends FtpFileProvider
{
    public FtpsFileProvider()
    {
        super();
    }

    /**
     * Creates the filesystem.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        final FtpsClientWrapper ftpClient = new FtpsClientWrapper(rootName, fileSystemOptions);

        return new FtpsFileSystem(rootName, ftpClient, fileSystemOptions);
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder()
    {
        return FtpsFileSystemConfigBuilder.getInstance();
    }
}
