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

import com.datos.vfs.FileSystemException;
import com.datos.vfs.FileSystemOptions;
import com.datos.vfs.provider.GenericFileName;
import com.datos.vfs.provider.ftp.FtpClient;
import com.datos.vfs.provider.ftp.FtpFileSystem;


/**
 * A FTPS file system.
 *
 * @since 2.1
 */
public class FtpsFileSystem extends FtpFileSystem
{
    /**
     * Create a new FtpsFileSystem.
     *
     * @param rootName The root of the file system.
     * @param ftpClient The FtpClient.
     * @param fileSystemOptions The FileSystemOptions.
     * @since 2.1
     */
    public FtpsFileSystem(
            final GenericFileName rootName, final FtpClient ftpClient, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, ftpClient, fileSystemOptions);
    }

    @Override
    protected FtpsClientWrapper createWrapper() throws FileSystemException
    {
        return new FtpsClientWrapper((GenericFileName) getRoot().getName(), getFileSystemOptions());
    }
}
