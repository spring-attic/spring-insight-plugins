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
package com.springsource.insight.plugin.jws;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * 
 */
@WebService(name=JwsServiceDefinitions.SERVICE_NAME,
            endpointInterface=JwsServiceDefinitions.ENDPOINT,
            portName=JwsServiceDefinitions.PORT_NAME,
            serviceName=JwsServiceDefinitions.SERVICE_NAME,
            targetNamespace=JwsServiceDefinitions.TARGET_NAMESPACE,
            wsdlLocation=JwsServiceDefinitions.WSDL_LOCATION)
public class JwsServiceInstance {
    public JwsServiceInstance() {
        super();
    }

    public Date getCurrentDate () {
        return new Date(System.currentTimeMillis());
    }

    @WebMethod(action=JwsServiceDefinitions.YESTERDAY_ACTION,
               operationName=JwsServiceDefinitions.YESTERDAY_OPER,
               exclude=JwsServiceDefinitions.EXCLUDE_METHOD)
    public Date getYesterdayDate (
            @WebParam(name=JwsServiceDefinitions.YESTERDAY_PARAM,
                      partName=JwsServiceDefinitions.YESTERDAY_PARAM,
                      targetNamespace=JwsServiceDefinitions.TARGET_NAMESPACE,
                      header=JwsServiceDefinitions.HEADER_PARAM)    long now,
            boolean ignoredValue) {
        return new Date(now - 86400000L);
    }

    @WebMethod(action=JwsServiceDefinitions.TOMORROW_ACTION,
               operationName=JwsServiceDefinitions.TOMORROW_OPER,
               exclude=JwsServiceDefinitions.EXCLUDE_METHOD)
    public Date getTomorrowDate (
            @WebParam(name=JwsServiceDefinitions.TOMORROW_PARAM,
                      partName=JwsServiceDefinitions.TOMORROW_PARAM,
                      targetNamespace=JwsServiceDefinitions.TARGET_NAMESPACE,
                      header=JwsServiceDefinitions.HEADER_PARAM)    long now,
            boolean ignoredValue) {
        return new Date(now + 86400000L);
    }
}
