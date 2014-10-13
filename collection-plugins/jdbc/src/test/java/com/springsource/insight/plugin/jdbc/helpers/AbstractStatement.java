/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jdbc.helpers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public abstract class AbstractStatement implements CallableStatement {
    private Connection conn;

    protected AbstractStatement() {
        super();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return null;
    }

    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    public void close() throws SQLException {
        // ignored
    }

    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    public void setMaxFieldSize(int max) throws SQLException {
        // ignored
    }

    public int getMaxRows() throws SQLException {
        return 0;
    }

    public void setMaxRows(int max) throws SQLException {
        // ignored
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        // ignored
    }

    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        // ignored
    }

    public void cancel() throws SQLException {
        // ignored
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void clearWarnings() throws SQLException {
        // ignored
    }

    public void setCursorName(String name) throws SQLException {
        // ignored
    }

    public boolean execute(String sql) throws SQLException {
        return true;
    }

    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    public int getUpdateCount() throws SQLException {
        return 0;
    }

    public boolean getMoreResults() throws SQLException {
        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {
        // ignored
    }

    public int getFetchDirection() throws SQLException {
        return 0;
    }

    public void setFetchSize(int rows) throws SQLException {
        // ignored
    }

    public int getFetchSize() throws SQLException {
        return 0;
    }

    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    public int getResultSetType() throws SQLException {
        return 0;
    }

    public void addBatch(String sql) throws SQLException {
        // ignored
    }

    public void clearBatch() throws SQLException {
        // ignored
    }

    public int[] executeBatch() throws SQLException {
        return null;
    }

    public Connection getConnection() throws SQLException {
        return conn;
    }

    public void setConnection(Connection c) {
        conn = c;
    }

    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException {
        return 0;
    }

    public int executeUpdate(String sql, int[] columnIndexes)
            throws SQLException {
        return 0;
    }

    public int executeUpdate(String sql, String[] columnNames)
            throws SQLException {
        return 0;
    }

    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException {
        return false;
    }

    public boolean execute(String sql, int[] columnIndexes)
            throws SQLException {
        return false;
    }

    public boolean execute(String sql, String[] columnNames)
            throws SQLException {
        return false;
    }

    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    public ResultSet executeQuery() throws SQLException {
        return null;
    }

    public int executeUpdate() throws SQLException {
        return 0;
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        // ignored
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        // ignored
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        // ignored
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        // ignored
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        // ignored
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        // ignored
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        // ignored
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        // ignored
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws SQLException {
        // ignored
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        // ignored
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        // ignored
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        // ignored
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        // ignored
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        // ignored
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        // ignored
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        // ignored
    }

    public void clearParameters() throws SQLException {
        // ignored
    }

    public boolean execute() throws SQLException {
        return true;
    }

    public void addBatch() throws SQLException {
        // ignored
    }

    public void setRef(int parameterIndex, Ref x) throws SQLException {
        // ignored
    }

    public void setArray(int parameterIndex, Array x) throws SQLException {
        // ignored
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
        // ignored
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        // ignored
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        // ignored
    }

    public void setNull(int parameterIndex, int sqlType, String typeName)
            throws SQLException {
        // ignored
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        // ignored
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return null;
    }

    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException {
        // ignored
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException {
        // ignored
    }

    public boolean wasNull() throws SQLException {
        return false;
    }

    public String getString(int parameterIndex) throws SQLException {
        return null;
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return false;
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return 0;
    }

    public short getShort(int parameterIndex) throws SQLException {
        return 0;
    }

    public int getInt(int parameterIndex) throws SQLException {
        return 0;
    }

    public long getLong(int parameterIndex) throws SQLException {
        return 0;
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return 0;
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return 0;
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException {
        return null;
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return null;
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return null;
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return null;
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return null;
    }

    public Object getObject(int parameterIndex, Map<String, Class<?>> map)
            throws SQLException {
        return null;
    }

    public Ref getRef(int parameterIndex) throws SQLException {
        return null;
    }

    public Array getArray(int parameterIndex) throws SQLException {
        return null;
    }

    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
            throws SQLException {
        return null;
    }

    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // ignored
    }

    public void registerOutParameter(String parameterName, int sqlType)
            throws SQLException {
        // ignored
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        // ignored
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        // ignored
    }

    public URL getURL(int parameterIndex) throws SQLException {
        return null;
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        // ignored
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        // ignored
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        // ignored
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        // ignored
    }

    public void setShort(String parameterName, short x) throws SQLException {
        // ignored
    }

    public void setInt(String parameterName, int x) throws SQLException {
        // ignored
    }

    public void setLong(String parameterName, long x) throws SQLException {
        // ignored
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        // ignored
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        // ignored
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        // ignored
    }

    public void setString(String parameterName, String x) throws SQLException {
        // ignored
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        // ignored
    }

    public void setDate(String parameterName, Date x) throws SQLException {
        // ignored
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        // ignored
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        // ignored
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        // ignored
    }

    public void setObject(String parameterName, Object x, int targetSqlType)
            throws SQLException {
        // ignored
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        // ignored
    }

    public void setDate(String parameterName, Date x, Calendar cal)
            throws SQLException {
        // ignored
    }

    public void setTime(String parameterName, Time x, Calendar cal)
            throws SQLException {
        // ignored
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
            throws SQLException {
        // ignored
    }

    public void setNull(String parameterName, int sqlType, String typeName)
            throws SQLException {
        // ignored
    }

    public String getString(String parameterName) throws SQLException {
        return null;
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return false;
    }

    public byte getByte(String parameterName) throws SQLException {
        return 0;
    }

    public short getShort(String parameterName) throws SQLException {
        return 0;
    }

    public int getInt(String parameterName) throws SQLException {
        return 0;
    }

    public long getLong(String parameterName) throws SQLException {
        return 0;
    }

    public float getFloat(String parameterName) throws SQLException {
        return 0;
    }

    public double getDouble(String parameterName) throws SQLException {
        return 0;
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return null;
    }

    public Date getDate(String parameterName) throws SQLException {
        return null;
    }

    public Time getTime(String parameterName) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return null;
    }

    public Object getObject(String parameterName) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return null;
    }

    public Object getObject(String parameterName, Map<String, Class<?>> map)
            throws SQLException {
        return null;
    }

    public Ref getRef(String parameterName) throws SQLException {
        return null;
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return null;
    }

    public Array getArray(String parameterName) throws SQLException {
        return null;
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return null;
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal)
            throws SQLException {
        return null;
    }

    public URL getURL(String parameterName) throws SQLException {
        return null;
    }
}