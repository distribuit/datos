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
package com.datos.vfs.provider.sftp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.datos.vfs.FileSystemConfigBuilder;
import com.datos.vfs.provider.GenericFileName;
import com.datos.vfs.Capability;
import com.datos.vfs.FileName;
import com.datos.vfs.FileSystem;
import com.datos.vfs.FileSystemException;
import com.datos.vfs.FileSystemOptions;
import com.datos.vfs.UserAuthenticationData;
import com.datos.vfs.provider.AbstractOriginatingFileProvider;
import com.datos.vfs.util.UserAuthenticatorUtils;

import com.jcraft.jsch.Session;

/**
 * A provider for accessing files over SFTP.
 */
public class SftpFileProvider extends AbstractOriginatingFileProvider
{
    /** User Information. */
    public static final String ATTR_USER_INFO = "UI";

    /** Authentication types. */
    public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES =
        new UserAuthenticationData.Type[]
            {
                UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD
            };

    /** The provider's capabilities. */
    protected static final Collection<Capability> capabilities =
        Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.GET_LAST_MODIFIED,
        Capability.SET_LAST_MODIFIED_FILE,
        Capability.RANDOM_ACCESS_READ
    }));

    // private JSch jSch = new JSch();

    /**
     * Constructs a new provider.
     */
    public SftpFileProvider()
    {
        super();
        setFileNameParser(SftpFileNameParser.getInstance());
    }

    /**
     * Creates a {@link FileSystem}.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // JSch jsch = createJSch(fileSystemOptions);

        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        Session session;
        UserAuthenticationData authData = null;
        try
        {
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, AUTHENTICATOR_TYPES);

            session = SftpClientFactory.createConnection(
                rootName.getHostName(),
                rootName.getPort(),
                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME,
                    UserAuthenticatorUtils.toChar(rootName.getUserName())),
                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD,
                    UserAuthenticatorUtils.toChar(rootName.getPassword())),
                fileSystemOptions);
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider.sftp/connect.error",
                name,
                e);
        }
        finally
        {
            UserAuthenticatorUtils.cleanup(authData);
        }

        return new SftpFileSystem(rootName, session, fileSystemOptions);
    }


    /**
     * Returns the JSch.
     *
     * @return Returns the jSch.
     */
    /*
    private JSch getJSch()
    {
        return this.jSch;
    }
    */

    /**
     * Initializes the component.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public void init() throws FileSystemException
    {
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder()
    {
        return SftpFileSystemConfigBuilder.getInstance();
    }

    @Override
    public Collection<Capability> getCapabilities()
    {
        return capabilities;
    }
}
