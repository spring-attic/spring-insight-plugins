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
package com.springsource.insight.plugin.quartz.scheduler;

import java.util.logging.Logger;

import org.quartz.utils.Key;

import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.props.BeanPropertiesSource;

/**
 * 
 */
public final class QuartzKeyValueAccessor {
	private BeanPropertiesSource	keySource;
	private final Logger	logger=Logger.getLogger(getClass().getName());
	private static class LazyFieldHolder {
		static final QuartzKeyValueAccessor	accessor=new QuartzKeyValueAccessor();
	}

	public static QuartzKeyValueAccessor getInstance () {
		return LazyFieldHolder.accessor;
	}

	QuartzKeyValueAccessor () {
    	try {
    		keySource = new BeanPropertiesSource(Key.class);
    	} catch(Throwable e) {
    		logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
   					     + " to get key bean properties: " + e.getMessage());
    	}
	}

	public String getFullName (Object key) {
		String	group=getGroup(key), name=getName(key);
		if (StringUtil.isEmpty(group)) {
			if (StringUtil.isEmpty(name)) {
				return "";
			} else {
				return name;
			}
		} else if (StringUtil.isEmpty(name)) {
			return group;
		} else {	// both non-empty
			return group + "." + name;
		}
	}

	public String getGroup (Object key) {
		return getKeyValue(key, "group");
	}

	public String getName (Object key) {
		return getKeyValue(key, "name");
	}

    private String getKeyValue (Object key, String name) {
    	if ((key == null) || (keySource == null)) {
    		return null;
    	}

    	try {
    		return keySource.getProperty(key, name, String.class);
    	} catch(Exception e) {
    		logger.warning("getKeyValue(" + name + ") failed (" + e.getClass().getSimpleName() + ")"
    			      	 + " to retrieve value: " + e.getMessage());
    		return null;
    	}
    }
}
