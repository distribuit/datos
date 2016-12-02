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
import com.datos.vfs.provider.AbstractFileObject;
import com.datos.vfs.util.RandomAccessMode;
import com.datos.vfs.provider.AbstractFileName;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A VFS representation of an HDFS file.
 *
 * @since 2.1
 */
public class HdfsFileObject extends AbstractFileObject<HdfsFileSystem> {
    private final HdfsFileSystem fs;
    private final FileSystem hdfs;
    private final Path path;
    private FileStatus stat;

    /**
     * Constructs a new HDFS FileObject
     *
     * @param name FileName
     * @param fs   HdfsFileSystem instance
     * @param hdfs Hadoop FileSystem instance
     * @param p    Path to the file in HDFS
     */
    protected HdfsFileObject(final AbstractFileName name, final HdfsFileSystem fs, final FileSystem hdfs, final Path p) {
        super(name, fs);
        this.fs = fs;
        this.hdfs = hdfs;
        this.path = p;
    }

    /**
     * @see AbstractFileObject#canRenameTo(FileObject)
     */
    @Override
    public boolean canRenameTo(final FileObject newfile) {
        // TODO implement this. Check the root URI, If same & if u have the
        // write permission return true
        return true;
    }

    /**
     * @see AbstractFileObject#doAttach()
     */
    @Override
    protected void doAttach() throws Exception {
        try {
            this.stat = this.hdfs.getFileStatus(this.path);
        } catch (final FileNotFoundException e) {
            this.stat = null;
            return;
        }
    }

    /**
     * @see AbstractFileObject#doGetAttributes()
     */
    @Override
    protected Map<String, Object> doGetAttributes() throws Exception {
        if (null == this.stat) {
            return super.doGetAttributes();
        } else {
            final Map<String, Object> attrs = new HashMap<>();
            attrs.put(HdfsFileAttributes.LAST_ACCESS_TIME.toString(), this.stat.getAccessTime());
            attrs.put(HdfsFileAttributes.BLOCK_SIZE.toString(), this.stat.getBlockSize());
            attrs.put(HdfsFileAttributes.GROUP.toString(), this.stat.getGroup());
            attrs.put(HdfsFileAttributes.OWNER.toString(), this.stat.getOwner());
            attrs.put(HdfsFileAttributes.PERMISSIONS.toString(), this.stat.getPermission().toString());
            attrs.put(HdfsFileAttributes.LENGTH.toString(), this.stat.getLen());
            attrs.put(HdfsFileAttributes.MODIFICATION_TIME.toString(), this.stat.getModificationTime());
            return attrs;
        }
    }

    /**
     * @see AbstractFileObject#doGetContentSize()
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return stat.getLen();
    }

    /**
     * @see AbstractFileObject#doGetInputStream()
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        return this.hdfs.open(this.path);
    }

    /**
     * @see AbstractFileObject#doGetLastModifiedTime()
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        if (null != this.stat) {
            return this.stat.getModificationTime();
        } else {
            return -1;
        }
    }

    /**
     * @see AbstractFileObject#doGetRandomAccessContent
     * (com.datos.vfs.util.RandomAccessMode)
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        if (mode.equals(RandomAccessMode.READWRITE)) {
            throw new UnsupportedOperationException();
        }
        return new HdfsRandomAccessContent(this.path, this.hdfs);
    }

    /**
     * @see AbstractFileObject#doGetType()
     */
    @Override
    protected FileType doGetType() throws Exception {
        try {
            doAttach();
            if (null == stat) {
                return FileType.IMAGINARY;
            }
            if (stat.isDir()) {
                return FileType.FOLDER;
            } else {
                return FileType.FILE;
            }
        } catch (final FileNotFoundException fnfe) {
            return FileType.IMAGINARY;
        }
    }

    /**
     * @see AbstractFileObject#doIsHidden()
     */
    @Override
    protected boolean doIsHidden() throws Exception {
        return false;
    }

    /**
     * @see AbstractFileObject#doIsReadable()
     */
    @Override
    protected boolean doIsReadable() throws Exception {
        return true;
    }

    /**
     * +
     * +     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doIsSameFile(org.apache.commons.vfs2.FileObject)
     * +
     */
    @Override
    protected boolean doIsSameFile(final FileObject destFile) throws FileSystemException {
        return false;
    }

    /**
     * @see AbstractFileObject#doIsWriteable()
     */
    @Override
    protected boolean doIsWriteable() throws Exception {
        return true;
    }

    /**
     * @see AbstractFileObject#doListChildren()
     */
    @Override
    protected String[] doListChildren() throws Exception {
        if (this.doGetType() != FileType.FOLDER) {
            throw new FileNotFolderException(this);
        }

        final FileStatus[] files = this.hdfs.listStatus(this.path);
        final String[] children = new String[files.length];
        int i = 0;
        for (final FileStatus status : files) {
            children[i++] = status.getPath().getName();
        }
        return children;
    }

    /**
     * @see AbstractFileObject#doListChildrenResolved()
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        if (this.doGetType() != FileType.FOLDER) {
            return null;
        }
        final String[] children = doListChildren();
        final FileObject[] fo = new FileObject[children.length];
        for (int i = 0; i < children.length; i++) {
            final Path p = new Path(this.path, children[i]);
            fo[i] = this.fs.resolveFile(p.toUri().toString());
        }
        return fo;
    }

    /**
     * @see AbstractFileObject#doRemoveAttribute(java.lang.String)
     */
    @Override
    protected void doRemoveAttribute(final String attrName) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * @see AbstractFileObject#doSetAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    protected void doSetAttribute(final String attrName, final Object value) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * @see AbstractFileObject#doSetLastModifiedTime(long)
     */
    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * @return boolean true if file exists, false if not
     * @see AbstractFileObject#exists()
     */
    @Override
    public boolean exists() throws FileSystemException {
        try {
            doAttach();
            return this.stat != null;
        } catch (final FileNotFoundException fne) {
            return false;
        } catch (final Exception e) {
            throw new FileSystemException("Unable to check existance ", e);
        }

    }


    @Override
    public void copyFrom(FileObject file, FileSelector selector) throws FileSystemException {
        /**
         * Fly implementation
         */
        super.copyFrom(file, selector);
    }

    @Override
    public void createFolder() throws FileSystemException {
        try {
            if (!this.hdfs.exists(path)) {
                this.hdfs.mkdirs(path);
            }
        } catch (IOException e) {
            throw new FileSystemException("Unable to create " + path.toString(), e);
        }
    }

    @Override
    public void createFile() throws FileSystemException {
        FSDataOutputStream create = null;
        try {

            if (!this.hdfs.exists(path)) {
                create = this.hdfs.create(path);
            }
        } catch (IOException e) {
            throw new FileSystemException("Unable to create " + path.toString(), e);
        } finally {
            if (null != create)
                try {
                    create.close();
                } catch (IOException e) {
                }
        }
    }

    @Override
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {

        StringBuffer relativePath = new StringBuffer();
        if (!(path.toString().startsWith("/"))) {
            relativePath.append("/");
        }
        relativePath.append(path.toString());
        if (bAppend) {
            createFile();
            return hdfs.append(new Path(relativePath.toString()));
        } else {
            return hdfs.create(new Path(relativePath.toString()));
        }
    }

    @Override
    public boolean isWriteable() throws FileSystemException {
        // TODO check permission too

        if (exists()) {
            // String permission = this.stat.getPermission().toString();
            // Properties properties = System.getProperties();
            // String userName = System.getProperty("user.name");
            //
            // System.out.println(properties);
            // String owner = this.stat.getOwner();
            // file.getContent().getAttributes()
            // try {
            return true;
            // } catch (IOException e1) {
            // e1.printStackTrace();
            // }
        }
        return false;
    }

    @Override
    public FileType getType() throws FileSystemException {
        return super.getType();
    }

    @Override
    protected void doRename(FileObject newFile) throws Exception {

        boolean rename;
        if (canRenameTo(newFile)) {
            String oldName = getName().getPath();
            String newName = newFile.getName().getPath();
            rename = hdfs.rename(new Path(oldName), new Path(newName));
        } else {
            throw new FileSystemException("vfs.provider/rename-not-supported.error");
        }

        if (!rename) {
            throw new FileSystemException("vfs.provider/rename-failed.error");
        }
    }

    public Path getPath() {
        return this.path;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.path.getName().hashCode();
    }

}
