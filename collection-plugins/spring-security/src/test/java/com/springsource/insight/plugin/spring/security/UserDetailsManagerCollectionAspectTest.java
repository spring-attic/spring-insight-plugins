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

package com.springsource.insight.plugin.spring.security;

import java.util.Collection;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * 
 */
public class UserDetailsManagerCollectionAspectTest
        extends SpringSecurityCollectionTestSupport {
    private final TestingUserDetailsManager manager=new TestingUserDetailsManager();
    private static final DummyObscuredValueMarker    marker=new DummyObscuredValueMarker();

    public UserDetailsManagerCollectionAspectTest() {
        super();
    }

    @BeforeClass
    public static void setupObscuringMarker () {
        UserDetailsManagerCollectionAspect.aspectOf().setSensitiveValueMarker(marker);
    }

    @Override
    @Before
    public void setUp () {
        super.setUp();
        // making sure again
        marker.cleanValues();
        manager.clearUsers();
    }

    @Override
    @After
    public void restore () {
        super.restore();
        marker.cleanValues();
        manager.clearUsers();
    }

    /*
     * NOTE: we are using a mock on purpose in order to ensure that the aspect
     * intercepts the {@link UserDetailsService} call(s) as well as the
     * {@link UserDetailsManager} ones since UserDetailsManager extends UserDetailsService
     */
    @Test
    public void testLoadUserByUsername () {
        final String    USERNAME="testLoadUserByUsername";
        manager.addUser(createUser(USERNAME));

        UserDetails result=manager.loadUserByUsername(USERNAME);
        Assert.assertNotNull("Mismatched loaded user instances", result);

        // the result is created by the UserDetailsOperationCollector
        Operation   op=assertOperationResult("loadUserByUsername", result, UserDetailsOperationCollector.RESULT_MAP_NAME);
        assertExtractedUsername(op, USERNAME);
    }

    @Test
    public void testCreateUser () {
        UserDetails user=createUser("testCreateUser");
        manager.createUser(user);
        assertOperationResult("createUser", user, "userDetails");
    }

    @Test
    public void testUpdateUser () {
        manager.addUser(createUser("testUpdateUser"));
        UserDetails user=createUser("testUpdateUser");
        manager.updateUser(user);
        assertOperationResult("updateUser", user, "userDetails");
    }

    @Test
    public void testDeleteUser () {
        final String    USERNAME="testDeleteUser";
        manager.addUser(createUser(USERNAME));
        manager.deleteUser(USERNAME);
        assertExtractedUsername("deleteUser", USERNAME);
    }
    
    @Test
    public void testUserExists () {
        final String    USERNAME="testUserExists";
        manager.userExists(USERNAME);
        assertExtractedUsername("userExists", USERNAME);
    }

    @Test
    public void testChangePassword () {
        UserDetails user=createUser("testChangePassword");
        manager.addUser(user);
        manager.setCurrentUser(user.getUsername());

        String  oldPassword=user.getPassword(), newPassword=UUID.randomUUID().toString();
        manager.changePassword(oldPassword, newPassword);

        Operation   op=assertOperationAction("changePassword");
        Assert.assertEquals("Mismatched old password", oldPassword, op.get("oldPassword", String.class));
        assertObscuredString("oldPassword", oldPassword);

        Assert.assertEquals("Mismatched new password", newPassword, op.get("newPassword", String.class));
        assertObscuredString("newPassword", newPassword);
    }

    protected Operation assertExtractedUsername (String actionName, String username) {
        return assertExtractedUsername(assertOperationAction(actionName), username);
    }

    protected Operation assertExtractedUsername (Operation op, String username) {
        Assert.assertNotNull("No operation extracted", op);
        Assert.assertEquals("Mismatched operation type", SpringSecurityDefinitions.USER_OP, op.getType());
        Assert.assertEquals("Mismatched username value", username, op.get("username", String.class));
        assertObscuredString("username", username);
        return op;
    }

    protected void assertObscuredString (String type, String value) {
        Collection<?> obscuredValues=marker.getValues();
        Assert.assertTrue("Not obscured - " + type, obscuredValues.contains(value));
    }

    protected Operation assertOperationResult (String actionName, UserDetails details, String mapName) {
        Operation   op=assertOperationAction(actionName);
        assertUserDetails(op.get(mapName, OperationMap.class), details);
        assertObscuredDetails(details, marker.getValues());
        return op;
    }

    protected void assertObscuredDetails (UserDetails details, Collection<?> obscuredValues) {
        Assert.assertTrue("Username not obscured", obscuredValues.contains(details.getUsername()));
        Assert.assertTrue("Password not obscured", obscuredValues.contains(details.getPassword()));
    }

    protected Operation assertOperationAction (String actionName) {
        Operation op=getLastEntered();
        Assert.assertNotNull("No operation extracted", op);
        Assert.assertEquals("Mismatched operation type", SpringSecurityDefinitions.USER_OP, op.getType());
        Assert.assertEquals("Mismatched action name", actionName, op.get("action", String.class));
        return op;
    }

    protected OperationMap assertUserDetails (OperationMap mapValue, UserDetails details) {
        if (mapValue == null) { // OK if null - just means no extra information collected
            return null;
        }

        Assert.assertEquals("Mismatched username",
                details.getUsername(), mapValue.get("username", String.class));
        Assert.assertEquals("Mismatched password",
                details.getPassword(), mapValue.get("password", String.class));
        Assert.assertEquals("Mismatched accountNonExpired",
                Boolean.valueOf(details.isAccountNonExpired()), mapValue.get("accountNonExpired", Boolean.class));
        Assert.assertEquals("Mismatched accountNonLocked",
                Boolean.valueOf(details.isAccountNonLocked()), mapValue.get("accountNonLocked", Boolean.class));
        Assert.assertEquals("Mismatched credentialsNonExpired",
                Boolean.valueOf(details.isCredentialsNonExpired()), mapValue.get("credentialsNonExpired", Boolean.class));
        Assert.assertEquals("Mismatched enabled",
                Boolean.valueOf(details.isEnabled()), mapValue.get("enabled", Boolean.class));
        assertGrantedAuthoritiesInstances(mapValue.get(ObscuringOperationCollector.GRANTED_AUTHS_LIST_NAME, OperationList.class),
                                          details.getAuthorities());
        return mapValue;
    }

    @Override
    public UserDetailsManagerCollectionAspect getAspect() {
        return UserDetailsManagerCollectionAspect.aspectOf();
    }

    private User createUser (String username) {
        return new User(username, UUID.randomUUID().toString(),
                        true, true, true, true,
                        AuthorityUtils.createAuthorityList(String.valueOf(System.nanoTime())));
    }
}
