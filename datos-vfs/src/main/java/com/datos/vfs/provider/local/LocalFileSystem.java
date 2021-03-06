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
package com.datos.vfs.provider.local;

import java.io.File;
import java.io.FilePermission;
import java.util.Collection;

import com.datos.vfs.Capability;
import com.datos.vfs.FileName;
import com.datos.vfs.FileObject;
import com.datos.vfs.FileSelector;
import com.datos.vfs.FileSystemException;
import com.datos.vfs.FileSystemOptions;
import com.datos.vfs.provider.AbstractFileName;
import com.datos.vfs.provider.AbstractFileSystem;
import com.datos.vfs.util.FileObjectUtils;

/**
 * A local file system.
 */
public class LocalFileSystem
    extends AbstractFileSystem
{
    private final String rootFile;

    public LocalFileSystem(final FileName rootName,
                           final String rootFile,
                           final FileSystemOptions opts)
    {
        super(rootName, null, opts);
        this.rootFile = rootFile;
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException
    {
        // Create the file
        return new LocalFile(this, rootFile, name);
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps)
    {
        caps.addAll(DefaultLocalFileProvider.capabilities);
    }

    /**
     * Creates a temporary local copy of a file and its descendants.
     */
    @Override
    protected File doReplicateFile(final FileObject fileObject,
                                   final FileSelector selector)
        throws Exception
    {
        final LocalFile localFile = (LocalFile) FileObjectUtils.getAbstractFileObject(fileObject);
        final File file = localFile.getLocalFile();
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null)
        {
            final FilePermission requiredPerm = new FilePermission(file.getAbsolutePath(), "read");
            sm.checkPermission(requiredPerm);
        }
        return file;
    }

}
