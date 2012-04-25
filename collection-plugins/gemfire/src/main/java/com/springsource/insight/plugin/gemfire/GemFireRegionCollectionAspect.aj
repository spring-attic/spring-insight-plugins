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

package com.springsource.insight.plugin.gemfire;

import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.JoinPoint;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.server.CacheServer;
import com.gemstone.gemfire.cache.util.GatewayHub;
import com.springsource.insight.intercept.operation.Operation;

public aspect GemFireRegionCollectionAspect extends AbstractGemFireCollectionAspect {
		
    public pointcut readWriteFlow () : execution(* Region.get(..)) || execution(* Region.getAll(..)) || execution(* Region.getEntry(..)) || execution(* Region.selectValue(..)) || execution(* Region.values())
    	|| execution(* Region.put(..)) || execution(* Region.putAll(..)) || execution(* Region.putIfAbsent(..)) || execution(* Region.query(..))
    	|| execution(* Region.remove(..)) || execution(* Region.replace(..));    
    	
    public pointcut otherFlow () : execution(* Region.clear()) || execution(* Region.containsKey*(..)) || execution(* Region.containsValue*(..))
    							|| execution(* Region.create*(..)) || execution(* Region.destroy*(..))
    							|| execution(* Region.entr*(..)) || execution(* Region.existsValue(..)) || execution(* Region.isEmpty())
    							|| execution(* Region.invalidate*(..)) || execution(* Region.key*(..))
    							|| execution(* Region.loadSnapshot*(..)) || execution(* Region.local*(..)) || execution(* Region.saveSnapshot*(..));    
    
    public pointcut collectionPoint () : (readWriteFlow() || otherFlow()) && !cflowbelow(readWriteFlow());

    public GemFireRegionCollectionAspect() {
		super(GemFireDefenitions.TYPE_REGION);
	}

    @Override
    protected Operation createOperation(final JoinPoint jp) {
    	Operation op = createBasicOperation(jp);
   
    	Region region = (Region) jp.getThis();
    	
    	addServers(op, region.getCache());    	
        op.put(GemFireDefenitions.FIELD_PATH, region.getFullPath());
        
                
        return op;
    }
    
    private void addServers(Operation op, Cache cache) {
    	Set<Object> servers = new HashSet<Object>();
    	for (CacheServer server : cache.getCacheServers()) {
    		servers.add(server.getBindAddress());
    	}
    	for (CacheServer server : cache.getBridgeServers()) { //support for GemFire < 5.1
    		servers.add(server.getBindAddress());
    	}    
    	for (GatewayHub gwh : cache.getGatewayHubs()) {
    		servers.add(gwh.getId() + ":" + gwh.getPort());
    	}    	 
    	op.createList(GemFireDefenitions.FIELD_SERVERS).addAll(servers);
	}
}
