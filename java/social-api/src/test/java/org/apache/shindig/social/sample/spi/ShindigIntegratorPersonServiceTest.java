/*
 ===========================================================================
 @    $Author$
 @  $Revision$
 @      $Date$
 @
 ===========================================================================
 */
package org.apache.shindig.social.sample.spi;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.testing.FakeGadgetToken;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.FilterOperation;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;

import org.apache.shindig.social.opensocial.spi.UserId;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Testclass for ShindigIntegratorPersonService
 */
public class ShindigIntegratorPersonServiceTest {

	private static final UserId JOHN_DOE = new UserId(UserId.Type.userId,
			"john.doe");
	private static final UserId JANE_DOE = new UserId(UserId.Type.userId,
			"jane.doe");
	private static final UserId MAIJA_M = new UserId(UserId.Type.userId,
			"maija.m");
	private static final UserId GEORGE_DOE = new UserId(UserId.Type.userId,
			"george.doe");

	private static final UserId VIEWER = new UserId(UserId.Type.viewer, null);

	private PersonService personService;

	private boolean containsPerson(List<Person> personList, String id) {
		Iterator<Person> iter = personList.iterator();
		while (iter.hasNext()) {
			Person currentPerson = iter.next();
			if (currentPerson.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Test setup
	 */
	@Before
	public void setUp() throws Exception {
		Injector injector = Guice
				.createInjector(new ShindigIntegratorTestsGuiceModule());
		personService = injector
				.getInstance(ShindigIntegratorPersonService.class);
		((ShindigIntegratorPersonService) personService)
				.setPersistenceAdapter(new ShindigIntegratorPersistenceAdapter());
	}

	/**
	 * Tests getPerson with a UserId typed as Userid. The Id string is set in
	 * the UserId.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetExpectedPersonByUserId() throws Exception {
		Future<Person> selectedObject = personService.getPerson(JOHN_DOE,
				Collections.<String> emptySet(), new FakeGadgetToken());
		Person person = selectedObject.get();
		assertEquals(JOHN_DOE.getUserId(), person.getId());
	}

	/**
	 * Tests getPerson with a UserId typed as Viewerid. The Id string is set in
	 * the token in the field viewerId.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetExpectedPersonByViewerId() throws Exception {
		SecurityToken token = FakeGadgetToken.createToken("viewerId=john.doe");
		Future<Person> selectedObject = personService.getPerson(VIEWER,
				Collections.<String> emptySet(), token);
		Person person = selectedObject.get();
		assertEquals(JOHN_DOE.getUserId(), person.getId());
	}

	@Test
	public void testGetExpectedFriends() throws Exception {
		CollectionOptions options = new CollectionOptions();
		options.setSortBy(PersonService.TOP_FRIENDS_SORT);
		options.setSortOrder(SortOrder.ascending);
		options.setFilter(null);
		options.setFilterOperation(FilterOperation.contains);
		options.setFilterValue("");
		options.setFirst(0);
		options.setMax(20);

		RestfulCollection<Person> friendsCollection = personService.getPeople(
				Sets.newHashSet(JOHN_DOE),
				new GroupId(GroupId.Type.friends, null), options,
				Collections.<String> emptySet(), new FakeGadgetToken()).get();
		assertEquals(4, friendsCollection.getEntry().size());
	}

	/**
	 * Tests getPeople with a couple of UserIds
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetExpectedPeople() throws Exception {
		CollectionOptions options = new CollectionOptions();
		options.setSortBy(PersonService.ALL_FILTER);
		options.setSortOrder(SortOrder.ascending);
		options.setFilter(null);
		options.setFilterOperation(FilterOperation.contains);
		options.setFilterValue("");
		options.setFirst(0);
		options.setMax(5);

		Set<UserId> userIds = new java.util.HashSet<UserId>();
		userIds.add(JOHN_DOE);
		userIds.add(JANE_DOE);
		userIds.add(MAIJA_M);

		RestfulCollection<Person> peopleCollection = personService.getPeople(
				userIds, null, options, Collections.<String> emptySet(),
				new FakeGadgetToken()).get();
		List<Person> personList = peopleCollection.getEntry();
		assertEquals(true, containsPerson(personList, JOHN_DOE.getUserId()));
		assertEquals(true, containsPerson(personList, MAIJA_M.getUserId()));
		assertEquals(false, containsPerson(personList, GEORGE_DOE.getUserId()));
	}
}
