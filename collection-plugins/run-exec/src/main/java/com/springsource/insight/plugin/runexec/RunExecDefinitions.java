/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.runexec;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

/**
 * 
 */
public final class RunExecDefinitions {
    public RunExecDefinitions() {
       super();
    }

    public static final OperationType   RUN_OP=OperationType.valueOf("run-runnable"),
                                        TIMER_OP=OperationType.valueOf("run-timer"),
                                        EXEC_OP=OperationType.valueOf("exec-runnable");
    
    // a few common attributes
    public static final String  THREADNAME_ATTR="threadName",
                                RUNNERID_ATTR="runnerId",
                                SPAWNLOC_ATTR="spawnLocation",
                                LINE_NUMBER_ATTR="lineNumber";

    public static String createRunnerId (Runnable runner) {
        return createRunnerId(runner, resolveRunnerClass(runner));
    }

    public static OperationMap setSpawnLocation (Operation op, Runnable runner) {
        Class<?>            runnerClass=resolveRunnerClass(runner);
        SourceCodeLocation  scl=new SourceCodeLocation(runnerClass.getName(), "run", (-1));
        return setSpawnLocation(op, scl);
    }

    public static Class<?> resolveRunnerClass (Runnable runner) {
        Class<?>  runClass=runner.getClass();
        String    simpleName=runClass.getSimpleName();
        // null/empty simple name usually means a proxy or an anonymous inner class
        if ((simpleName == null) || (simpleName.length() <= 0) || simpleName.contains("$Proxy$")) {
            return Runnable.class;
        }
 
        return runClass;
    }
    
    public static String createRunnerId (Runnable runner, Class<?> runnerClass) {
        return runnerClass.getName() + "@" + System.identityHashCode(runner);
    }

    public static OperationMap setSpawnLocation (Operation op, SourceCodeLocation spawnLocation) {
        return setSpawnLocation(op.createMap(SPAWNLOC_ATTR), spawnLocation);
    }

    public static OperationMap setSpawnLocation (OperationMap op, SourceCodeLocation spawnLocation) {
        return op.put(OperationFields.CLASS_NAME, spawnLocation.getClassName())
                 .put(OperationFields.METHOD_NAME, spawnLocation.getMethodName())
                 .put(LINE_NUMBER_ATTR, spawnLocation.getLineNumber())
                 ;
    }

    public static SourceCodeLocation getSpawnLocation (Operation op) {
        return getSpawnLocation(op.get(SPAWNLOC_ATTR, OperationMap.class));
    }

    public static SourceCodeLocation getSpawnLocation (OperationMap op) {
        if (op == null) {   // means information N/A
            return null;
        }

        String  className=op.get(OperationFields.CLASS_NAME, String.class),
                methodName=op.get(OperationFields.METHOD_NAME, String.class);
        Integer lineNumber=op.get(LINE_NUMBER_ATTR, Integer.class);
        return new SourceCodeLocation(className, methodName, lineNumber.intValue());
    }
}
