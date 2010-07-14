package org.apache.shindig.social.sample.spi;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;

import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;

/**
 * PersonService-Implementation to integrate a community - DB into Shindig.
 */
public class ShindigIntegratorPersonService implements PersonService {

	private ShindigIntegratorPersistenceAdapter db;

	/**
	 * Lazy Init for PersistenceAdapter. The Adapter is a singleton.
	 */
	private void initDB() {
		// TODO Better way would be to create the adapter in the guice-Module
		// and set it via injection.
		if (db == null) {
			db = ShindigIntegratorPersistenceAdapter.getInstance();
		}
	}

	/**
	 * Provide Injection for PersistenceAdapter
	 * 
	 * @param adapter
	 */
	public void setPersistenceAdapter(
			ShindigIntegratorPersistenceAdapter adapter) {
		db = adapter;
	}

	/**
	 * Returns a list of people that correspond to the passed in person ids.
	 * 
	 * @param userIds
	 *            A set of users
	 * @param groupId
	 *            The group
	 * @param collectionOptions
	 *            How to filter, sort and paginate the collection being fetched
	 * @param fields
	 *            The profile details to fetch. Empty set implies all
	 * @param token
	 *            The gadget token
	 * @return a list of people.
	 */
	public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds,
			GroupId groupId, CollectionOptions options, Set<String> fields,
			SecurityToken token) throws ProtocolException {
		initDB();

		List<Person> result = db.getRelatedPersons(userIds, groupId, token);

		int totalSize = result.size();
		
		System.out.println("Trong getPeople() totalSize : " + totalSize);
		
		
		int last = options.getFirst() + options.getMax();
		result = result.subList(options.getFirst(), Math.min(last, totalSize));

		return ImmediateFuture.newInstance(new RestfulCollection<Person>(
				result, options.getFirst(), totalSize, options.getMax()));
	}

	/**
	 * Returns a person that corresponds to the passed in person id.
	 * 
	 * @param id
	 *            The id of the person to fetch.
	 * @param fields
	 *            The fields to fetch.
	 * @param token
	 *            The gadget token
	 * @return a list of people.
	 */
	public Future<Person> getPerson(UserId id, Set<String> fields,
			SecurityToken token) throws ProtocolException {
		initDB();

		if (id != null) {
			Person currentPerson = db.findPerson(id.getUserId(token));
			if (currentPerson != null) {
				return ImmediateFuture.newInstance(currentPerson);
			}
		}
		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Person not found");
	}
}
