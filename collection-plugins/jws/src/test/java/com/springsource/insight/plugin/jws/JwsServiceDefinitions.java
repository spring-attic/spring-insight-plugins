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

import javax.jws.WebParam;

/**
 * 
 */
public final class JwsServiceDefinitions {
    private JwsServiceDefinitions() {
        super();
    }

    // constants used by the WebService annotation
    public static final String  SERVICE_NAME="JwsServiceTest",
                                ENDPOINT=SERVICE_NAME + "Endpoint",
                                PORT_NAME=SERVICE_NAME + "PortName",
                                TARGET_NAMESPACE="http://lu.ci.an.demo/" + SERVICE_NAME,
                                WSDL_LOCATION="WEB-INF/wsdl/" + SERVICE_NAME + ".wsdl";

    // constants used by the WebMethod/WebParam annotation(s)
    public static final String  ACTION_SUFFIX="-ACTION",
                                OPERATION_SUFFIX="-OP",
                                PARAM_SUFFIX="-PARAM",
                                NOW_CALL="NOW",
                                YESTERDAY_CALL="YESTERDAY",
                                TOMORROW_CALL="TOMORROW";

    public static final String  NOW_ACTION=NOW_CALL + ACTION_SUFFIX,
                                NOW_OPER=NOW_CALL + OPERATION_SUFFIX,
                                YESTERDAY_ACTION=YESTERDAY_CALL + ACTION_SUFFIX,
                                YESTERDAY_OPER=YESTERDAY_CALL + OPERATION_SUFFIX,
                                YESTERDAY_PARAM=YESTERDAY_CALL + PARAM_SUFFIX,
                                TOMORROW_ACTION=TOMORROW_CALL + ACTION_SUFFIX,
                                TOMORROW_OPER=TOMORROW_CALL + OPERATION_SUFFIX,
                                TOMORROW_PARAM=TOMORROW_CALL + PARAM_SUFFIX;

    public static final boolean EXCLUDE_METHOD=true, HEADER_PARAM=true;
    public static final WebParam.Mode   PARAM_MODE=WebParam.Mode.IN;
}
