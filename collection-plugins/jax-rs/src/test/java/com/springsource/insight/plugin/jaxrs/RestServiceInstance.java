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
package com.springsource.insight.plugin.jaxrs;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 */
@Path(RestServiceDefinitions.ROOT_PATH)
public class RestServiceInstance {
    public RestServiceInstance() {
        super();
    }

    public void initialize () {
        // do nothing
    }

    @GET
    public Date getCurrentDate () {
        return new Date(System.currentTimeMillis());
    }

    @GET @Path(RestServiceDefinitions.YESTERDAY_PATH)
    public Date getYesterdayDate (@PathParam("{now}") long now, boolean ignoredValue) {
        return new Date(now - 86400000L);
    }

    @GET @Path(RestServiceDefinitions.TOMORROW_PATH)
    public Date getTomorrowDate (@PathParam("{now}") long now, boolean ignoredValue) {
        return new Date(now + 86400000L);
    }

    public void destroy () {
        // do nothing
    }
}
