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

package com.springsource.insight.plugin.jndi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.collection.PatternedResourceCollectionFilter;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public class JndiResourceCollectionFilter extends PatternedResourceCollectionFilter {
	public static final CollectionSettingName	INCLD_SETTING=
			new CollectionSettingName("included.resource.pattern", JndiPluginRuntimeDescriptor.PLUGIN_NAME, "Regexp used to select which names to intercept");
	public static final CollectionSettingName	EXCLD_SETTING=
			new CollectionSettingName("excluded.resource.pattern", JndiPluginRuntimeDescriptor.PLUGIN_NAME, "Regexp used to select which names to ignore");
	/**
	 * A {@link List} of <U>suffixes</U> automatically excluded. The main
	 * reason for this list is the fact that Apache Tomcat uses JNDI to
	 * look up classes as well as web rendering resources
	 */
	public static final List<String>	DEFAULT_EXCLUSION_PATTERNS=
			Collections.unmodifiableList(
					Arrays.asList("class", "jpg", "jpeg", "gif", "png", "ico", "bmp", "css", "js", "jsp", "htm", "html", "asp", "aspx"));

	private static final JndiResourceCollectionFilter	FILTER=new JndiResourceCollectionFilter();

	private JndiResourceCollectionFilter() {
		this(CollectionSettingsRegistry.getInstance());
	}

	public static final JndiResourceCollectionFilter getIntance () {
		return FILTER;
	}

	JndiResourceCollectionFilter(CollectionSettingsRegistry registry) {
		super(registry, INCLD_SETTING, EXCLD_SETTING);

		setExclusionPatterns(DEFAULT_EXCLUSION_PATTERNS);
	}
	
	void setInclusionPatterns(String ... patterns) {
		setInclusionPatterns((ArrayUtil.length(patterns) <= 0) ? Collections.<String>emptyList() : Arrays.asList(patterns));
	}

	void setInclusionPatterns(Collection<String> patterns) {
		setInclusionPattern(combinePatterns(patterns));
	}

	void setExclusionPatterns(String ... patterns) {
		setExclusionPatterns((ArrayUtil.length(patterns) <= 0) ? Collections.<String>emptyList() : Arrays.asList(patterns));
	}

	void setExclusionPatterns(Collection<String> patterns) {
		setExclusionPattern(combinePatterns(patterns));
	}

	static final String combinePatterns (Collection<String> patterns) {
		if (ListUtil.size(patterns) <= 0) {
			return "";
		}

		StringBuilder	sb=new StringBuilder(patterns.size() * 6);
		for (String p : patterns) {
			if (StringUtil.isEmpty(p)) {
				throw new IllegalArgumentException("Null/empty pattern in " + patterns);
			}

			if (sb.length() > 0) {
				sb.append('|');
			}

			sb.append(".*\\.").append(p).append('$');
		}

		return sb.toString();
	}
}
