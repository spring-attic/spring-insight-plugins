/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jdbc.helpers.jdk16;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

import com.springsource.insight.plugin.jdbc.helpers.AbstractStatement;

/**
 *
 */
public class Jdk16Statement extends AbstractStatement {
    public Jdk16Statement() {
        super();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public boolean isClosed() throws SQLException {
        return false;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        // ignored
    }

    public boolean isPoolable() throws SQLException {
        return false;
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        return null;
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return null;
    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        // ignored
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        // ignored
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        // ignored
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        // ignored
    }

    public void setNString(String parameterName, String value)
            throws SQLException {
        // ignored
    }

    public void setNClob(String parameterName, Reader reader, long length)
            throws SQLException {
        // ignored
    }

    public void setNClob(String parameterName, Reader reader)
            throws SQLException {
        // ignored
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        return null;
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return null;
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject)
            throws SQLException {
        // ignored
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return null;
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return null;
    }

    public String getNString(int parameterIndex) throws SQLException {
        return null;
    }

    public String getNString(String parameterName) throws SQLException {
        return null;
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return null;
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return null;
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        // ignored
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        // ignored
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        // ignored
    }


    public void setAsciiStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        // ignored
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        // ignored
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        // ignored
    }

    public void setAsciiStream(int parameterIndex, InputStream x)
            throws SQLException {
        // ignored
    }

    public void setBinaryStream(int parameterIndex, InputStream x)
            throws SQLException {
        // ignored
    }

    public void setCharacterStream(int parameterIndex, Reader reader)
            throws SQLException {
        // ignored
    }

    public void setNCharacterStream(int parameterIndex, Reader value)
            throws SQLException {
        // ignored
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        // ignored
    }

    public void setBlob(int parameterIndex, InputStream inputStream)
            throws SQLException {
        // ignored
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        // ignored
    }

    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        // ignored
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        // ignored
    }

    public Blob getBlob(int parameterIndex) throws SQLException {
        return null;
    }

    public Clob getClob(int parameterIndex) throws SQLException {
        return null;
    }

    public Clob getClob(String parameterName) throws SQLException {
        return null;
    }

    public void setClob(String parameterName, Reader reader, long length)
            throws SQLException {
        // ignored
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        // ignored
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return null;
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return null;
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        // ignored
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        // ignored
    }

    public void setAsciiStream(String parameterName, InputStream x, long length)
            throws SQLException {
        // ignored
    }

    public void setBinaryStream(String parameterName, InputStream x, long length)
            throws SQLException {
        // ignored
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        // ignored
    }

    public void setAsciiStream(String parameterName, InputStream x)
            throws SQLException {
        // ignored
    }

    public void setBinaryStream(String parameterName, InputStream x)
            throws SQLException {
        // ignored
    }

    public void setCharacterStream(String parameterName, Reader reader)
            throws SQLException {
        // ignored
    }

    public void setNCharacterStream(String parameterName, Reader value)
            throws SQLException {
        // ignored
    }

    public void setClob(String parameterName, Reader reader)
            throws SQLException {
        // ignored
    }

    public void setBlob(String parameterName, InputStream inputStream)
            throws SQLException {
        // ignored
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        // ignored
    }

    public void setNString(int parameterIndex, String value)
            throws SQLException {
        // ignored
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        // ignored
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        // ignored
    }

    public void setClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        // ignored
    }

    public void setNClob(int parameterIndex, Reader reader, long length)
            throws SQLException {
        // ignored
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject)
            throws SQLException {
        // ignored
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
        // ignored
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        // ignored
    }

    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        // ignored
    }

    public void setBinaryStream(String parameterName, InputStream x, int length)
            throws SQLException {
        // ignored
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws SQLException {
        // ignored
    }

    public <T> T getObject(int parameterIndex, Class<T> type)
            throws SQLException {
        return null;
    }

    public <T> T getObject(String parameterName, Class<T> type)
            throws SQLException {
        return null;
    }

    public void closeOnCompletion() throws SQLException {
        // ignored
    }

    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }
}
