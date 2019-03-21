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

package com.springsource.insight.plugin.spring.security;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

/**
 *
 */
public class TestingUserDetailsManager implements UserDetailsManager {
    private final Map<String, UserDetails> usersMap = new TreeMap<String, UserDetails>();
    private final Logger logger = Logger.getLogger(getClass().getName());
    private UserDetails currentUser;

    public TestingUserDetailsManager() {
        super();
    }

    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        return setCurrentUser(username);
    }

    public void createUser(UserDetails user) {
        addUser(user);
    }

    public void updateUser(UserDetails user) {
        String username = user.getUsername();
        UserDetails prev = usersMap.put(username, user);
        if (prev == null)
            throw new IllegalStateException("No previous details for " + username);
        logger.info("updated " + user);
    }

    public void deleteUser(String username) {
        UserDetails user = usersMap.remove(username);
        if (user == null)
            logger.warning("deleteUser(" + username + ") N/A");
        else
            logger.info("deleteUser(" + username + "): " + user);

    }

    public void changePassword(String oldPassword, String newPassword) {
        if (currentUser == null)
            throw new IllegalStateException("No currently loaded user");

        logger.info("changePassword(" + oldPassword + " => " + newPassword + ") " + currentUser);
    }

    public boolean userExists(String username) {
        return usersMap.containsKey(username);
    }

    Map<String, UserDetails> getUsersMap() {
        return Collections.unmodifiableMap(usersMap);
    }

    // need this call to avoid calling "loadUserByUsername" which is intercepted by the aspect
    UserDetails setCurrentUser(String username)
            throws UsernameNotFoundException {
        UserDetails user = usersMap.get(username);
        if (user == null)
            throw new UsernameNotFoundException("No such user");

        currentUser = user;
        return user;
    }

    // need this call to avoid calling "createUser" which is intercepted by the aspect
    void addUser(UserDetails user) {
        String username = user.getUsername();
        UserDetails prev = usersMap.put(username, user);
        if (prev != null)
            throw new IllegalStateException("Multiple instances for " + username);
        logger.info("added " + user);
    }

    void clearUsers() {
        if (usersMap.size() > 0)
            usersMap.clear();
        if (currentUser != null)
            currentUser = null;
    }
}
