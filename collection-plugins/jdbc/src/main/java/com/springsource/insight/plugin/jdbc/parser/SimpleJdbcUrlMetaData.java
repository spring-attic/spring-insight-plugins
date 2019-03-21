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
package com.springsource.insight.plugin.jdbc.parser;

import com.springsource.insight.util.ObjectUtil;


public class SimpleJdbcUrlMetaData implements JdbcUrlMetaData {
    private final String host;
    private final int port;
    private final String databaseName;
    private final String connectionUrl;
    private final String vendorName;
    private final int hashValue;

    public SimpleJdbcUrlMetaData(String dbHost, int dbPort, String dbName, String url, DatabaseType type) {
        this(dbHost, dbPort, dbName, url, type.getVendorName());
    }

    public SimpleJdbcUrlMetaData(String dbHost, int dbPort, String dbName, String url, final String vendor) {
        this.host = dbHost;
        this.port = dbPort;
        this.databaseName = dbName;
        this.connectionUrl = url;
        this.vendorName = vendor;

        // we can calculate the hash code since all members are final
        this.hashValue = calcHash();
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
        return ObjectUtil.typedEquals(getConnectionUrl(), other.getConnectionUrl())
        	&& ObjectUtil.typedEquals(getDatabaseName(), other.getDatabaseName())
        	&& ObjectUtil.typedEquals(getHost(), other.getHost())
        	&& ObjectUtil.typedEquals(getVendorName(), other.getVendorName())
        	&& (getPort() == other.getPort())
        	 ;
    }

    @Override
    public String toString() {
        return getConnectionUrl() + "," + getVendorName() + "," + getDatabaseName() + "," + getHost() + "," + getPort();
    }

}
