package com.hi.easydq.proxy.users;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class LdapAuthenticationProviderTest {

	@Test
	public void testLdapConnectionIsOnlyHitOnce() throws Exception {
		LdapConnection ldapConnection = EasyMock
				.createMock(LdapConnection.class);
		EntryCursor cursor = EasyMock.createMock(EntryCursor.class);
		DefaultEntry entry = new DefaultEntry();
		entry.add("uid", "foo");
		entry.add("userpassword", "bar");

		ldapConnection.setTimeOut(30000);

		EasyMock.expect(
				ldapConnection.search("ou=users,ou=system", "(uid=foo)",
						SearchScope.SUBTREE)).andReturn(cursor);

		EasyMock.expect(cursor.next()).andReturn(true);

		EasyMock.expect(cursor.get()).andReturn(entry);

		cursor.close();

//		ldapConnection.bind("randomUser", "randomPassword");
		//ldapConnection.unBind();
		//EasyMock.expect(ldapConnection.close()).andReturn(true);

		EasyMock.replay(ldapConnection, cursor);

		@SuppressWarnings("resource")
		// Closed in the implementation
		// TODO Redesign the API
		LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(
				ldapConnection, "randomUser", "randomPassword");

		Assert.assertTrue(ldapAuthenticationProvider.authenticate("foo", "bar"));
		Assert.assertTrue(ldapAuthenticationProvider.authenticate("foo", "bar"));

		EasyMock.verify(ldapConnection, cursor);
	}

	@Test
	public void testAuthenticationFailure() throws Exception {
		LdapConnection ldapConnection = EasyMock
				.createMock(LdapConnection.class);

		ldapConnection.setTimeOut(30000);

		EasyMock.expect(
				ldapConnection.search("ou=users,ou=system", "(uid=foo)",
						SearchScope.SUBTREE)).andReturn(null);

//		ldapConnection.bind("randomUser", "randomPassword");
//		ldapConnection.unBind();
//		EasyMock.expect(ldapConnection.close()).andReturn(true);

		EasyMock.replay(ldapConnection);

		@SuppressWarnings("resource")
		// Closed in the implementation
		// TODO Redesign the API
		LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(
				ldapConnection, "randomUser", "randomPassword");

		Assert.assertFalse(ldapAuthenticationProvider
				.authenticate("foo", "bar"));

		EasyMock.verify(ldapConnection);

	}

	@Test
	public void testAuthenticationSuccess() throws Exception {
		LdapConnection ldapConnection = EasyMock
				.createMock(LdapConnection.class);
		EntryCursor cursor = EasyMock.createMock(EntryCursor.class);
		DefaultEntry entry = new DefaultEntry();
		entry.add("uid", "foo");
		entry.add("userpassword", "bar");

		ldapConnection.setTimeOut(30000);

		EasyMock.expect(
				ldapConnection.search("ou=users,ou=system", "(uid=foo)",
						SearchScope.SUBTREE)).andReturn(cursor);

		EasyMock.expect(cursor.next()).andReturn(true);
		EasyMock.expect(cursor.get()).andReturn(entry);
//		ldapConnection.bind("randomUser", "randomPassword");
//		ldapConnection.unBind();
//		EasyMock.expect(ldapConnection.close()).andReturn(true);

		cursor.close();

		EasyMock.replay(ldapConnection, cursor);

		@SuppressWarnings("resource")
		// Closed in the implementation
		// TODO Redesign the API
		LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(
				ldapConnection, "randomUser", "randomPassword");

		Assert.assertTrue(ldapAuthenticationProvider.authenticate("foo", "bar"));

		EasyMock.verify(ldapConnection, cursor);

	}
}
