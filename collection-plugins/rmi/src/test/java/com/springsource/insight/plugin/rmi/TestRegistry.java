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

package com.springsource.insight.plugin.rmi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.HashMap;

import com.springsource.insight.util.ListUtil;

/**
 */
public class TestRegistry extends HashMap<String,Remote> implements Registry {
	private static final long serialVersionUID = 7667773694972670028L;

	public TestRegistry () {
		super();
	}

	public Remote lookup(String name) throws RemoteException,
			NotBoundException, AccessException {
		// TODO Auto-generated method stub
		return get(name);
	}

	public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
		if (containsKey(name)) {
			throw new AlreadyBoundException(name);
		}
		
		put(name, obj);
	}

	public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
		Remote	rem=remove(name);
		if (rem == null) {
			throw new NotBoundException(name);
		}
	}

	public void rebind(String name, Remote obj) throws RemoteException, AccessException {
		put(name, obj);
	}

	public String[] list() throws RemoteException, AccessException {
		Collection<String>	keys=keySet();
		if (ListUtil.size(keys) <= 0) {
			return new String[0];
		} else {
			return keys.toArray(new String[keys.size()]);
		}
	}
}
