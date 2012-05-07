/**
 * Copyright 2009-2011 the original author or authors.
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

package com.springsource.insight.plugin.jdbc.parser;


public class SimpleJdbcUrlMetaData implements JdbcUrlMetaData {

    private final String host;
    private final int port;
    private final String databaseName;
    private final String connectionUrl;
    private final String vendorName;

    private final int hashValue;

    @SuppressWarnings("hiding")
    public SimpleJdbcUrlMetaData(final String host, final int port, final String databaseName, final String connectionUrl, final String vendorName) {

        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.connectionUrl = connectionUrl;
        this.vendorName = vendorName;

        // we can calculate the hash code since all members are final
        this.hashValue = calcHash();
    }
    
    @SuppressWarnings("hiding")
    public SimpleJdbcUrlMetaData(final String host, final int port, final String databaseName, final String connectionUrl, final DatabaseType type) {
        this(host, port, databaseName, connectionUrl, type.getVendorName());
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getVendorName() {
        return vendorName;
    }

    private int calcHash() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((connectionUrl == null) ? 0 : connectionUrl.hashCode());
        result = prime * result + ((databaseName == null) ? 0 : databaseName.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
        return result;
    }
    
    @Override
    public int hashCode() {
        return hashValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleJdbcUrlMetaData other = (SimpleJdbcUrlMetaData) obj;
        if (connectionUrl == null) {
            if (other.connectionUrl != null)
                return false;
        } else if (!connectionUrl.equals(other.connectionUrl))
            return false;
        if (databaseName == null) {
            if (other.databaseName != null)
                return false;
        } else if (!databaseName.equals(other.databaseName))
            return false;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (port != other.port)
            return false;
        if (vendorName == null) {
            if (other.vendorName != null)
                return false;
        } else if (!vendorName.equals(other.vendorName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getConnectionUrl() + "," + getVendorName() + "," + getDatabaseName() + "," + getHost() + "," + getPort();
    }

}
