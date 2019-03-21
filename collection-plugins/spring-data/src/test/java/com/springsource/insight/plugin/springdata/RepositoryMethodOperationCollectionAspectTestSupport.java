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

package com.springsource.insight.plugin.springdata;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.mockito.Mockito;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.plugin.springdata.dao.TestEntityRepository;
import com.springsource.insight.plugin.springdata.model.TestEntity;
import com.springsource.insight.util.StringFormatterUtils;

/**
 * 
 */
public class RepositoryMethodOperationCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {
    private static final int   NUM_ENTITIES=Byte.SIZE;

    public RepositoryMethodOperationCollectionAspectTestSupport() {
        super();
    }

    @Override
    public RepositoryMethodOperationCollectionAspect getAspect() {
        return RepositoryMethodOperationCollectionAspect.aspectOf();
    }

    public void testEntityRepository (TestEntityRepository repository) {
        Date        startTime=new Date(System.currentTimeMillis()), endTime=new Date(startTime.getTime());
        for (int index=0; index < NUM_ENTITIES; index++) {
            TestEntity  entity=repository.save(new TestEntity(endTime));
            assertNotNull("No entity created for " + endTime, entity);
            assertInvokedMethodOperation("save", entity, entity);

            Long    id=entity.getId();
            assertNotNull("No ID assigned to " + entity, id);

            boolean  exists=repository.exists(id);
            assertTrue("Entity does not exist " + entity, exists);
            assertInvokedMethodOperation("exists", Boolean.valueOf(exists), id);

            endTime = new Date(endTime.getTime() + TimeUnit.SECONDS.toMillis(15L));
        }

        assertEquals("Mismatched number of created entities", NUM_ENTITIES, repository.count());
        assertInvokedMethodOperation("count", Long.valueOf(NUM_ENTITIES));

        Iterable<? extends TestEntity>    list=runListQuery(repository, null, null);
        list = runListQuery(repository, startTime, endTime);

        repository.delete(list);
        assertInvokedMethodOperation("delete", null, list);
    }

    private Iterable<? extends TestEntity> runListQuery (
            TestEntityRepository repository, Date startTime, Date endTime) {
        Iterable<? extends TestEntity>    list=((startTime == null) || (endTime == null))
                ? repository.findAll()
                : repository.findEntitiesInRange(startTime, endTime)
                ;
       String   name=((startTime == null) || (endTime == null)) ? "findAll" : "findEntitiesInRange";
       assertNotNull("[" + name + "]: No entities retrieved", list);

       if ((startTime == null) || (endTime == null)) {
           assertInvokedMethodOperation(name, list);
       } else {
           assertInvokedMethodOperation(name, list, startTime, endTime);
       }

       int  count=0;
       for (TestEntity expEntity : list) {
           Long         id=expEntity.getId();
           TestEntity   actEntity=repository.findOne(id);
           assertNotNull("[" + name + "]: Cannot locate ID=" + id, actEntity);
           assertEquals("[" + name + "]: Mismatced values for ID=" + id, expEntity, actEntity);
           assertInvokedMethodOperation("findOne", actEntity, id);

           count++;
       }
       assertEquals("Mismatched number of retrieved entities", NUM_ENTITIES, count);

       return list;
    }

    public Operation assertInvokedMethodOperation (String name, Object returnValue, Object ... args) {
        Operation   op=getLastEntered();
        assertNotNull("[" + name + "]: No operation extracted", op);
        resetSpiedCollector();

        assertEquals("[" + name + "]: Mismatched operation type", SpringDataDefinitions.REPO_TYPE, op.getType());
        assertEquals("[" + name + "]: Mismatched method name", name, op.get(OperationFields.METHOD_NAME, String.class));

        // see JoinPointFinalizer code 
        OperationList argsList = op.get(OperationFields.ARGUMENTS, OperationList.class);
        assertNotNull("[" + name + "]: No arguments list", argsList);
        assertEquals("[" + name + "]: Mismatched arguments list size", args.length, argsList.size());
        for (int index=0; index < args.length; index++) {
            Object  argVal=args[index];
            if (!StringFormatterUtils.isToStringable(argVal)) {
                continue;
            }

            String  expValue=StringFormatterUtils.formatObject(argVal),
                    actValue=argsList.get(index, String.class);
            assertEquals("[" + name + "]: Mismatched argument #" + index + " value", expValue, actValue);
        }

        // see DefaultOperationCollector#exitNormal(Object) code
        if (returnValue != null) {
            String  expValue=StringFormatterUtils.formatObject(returnValue),
                    actValue=op.get(OperationFields.RETURN_VALUE, String.class);
            assertEquals("[" + name + "]: Mismatched return value", expValue, actValue);
        }

        return op;
    }
    
    public void resetSpiedCollector () {
        Mockito.reset(spiedOperationCollector);
    }
}
