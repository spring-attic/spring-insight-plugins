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
package com.springsource.insight.plugin.jcr;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

public class JCRCollectionUtils {
	public static String getWorkspaceName(Object targ) {
		String workspaceName=null;
		
		try {
			if (targ instanceof Item)
				workspaceName=((Item)targ).getSession().getWorkspace().getName();
			else
			if (targ instanceof Workspace)
		    	workspaceName=((Workspace)targ).getName();
			else
			if (targ instanceof Session)
				workspaceName=((Session)targ).getWorkspace().getName();
		}
		catch (RepositoryException e) {
			//ignore
		}
		
		return workspaceName;
	}
}
