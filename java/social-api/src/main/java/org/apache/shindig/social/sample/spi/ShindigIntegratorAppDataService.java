package org.apache.shindig.social.sample.spi;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.google.common.collect.Maps;

/**
 * AppDataService-Implementation to integrate a community - DB into Shindig.
 */
public class ShindigIntegratorAppDataService implements AppDataService {

  private ShindigIntegratorPersistenceAdapter db;

  /**
   * Lazy Init for PersistenceAdapter. The Adapter is a singleton.
   */
  private void initDB() {
    // TODO Better way would be to create the adapter in the guice-Module and set it via injection.
    if (db == null) {
      db = ShindigIntegratorPersistenceAdapter.getInstance();
    }
  }

  /**
   * Provide Injection for PersistenceAdapter
   * 
   * @param adapter
   */
  public void setPersistenceAdapter(ShindigIntegratorPersistenceAdapter adapter) {
    db = adapter;
  }

  /**
   * Deletes data for the specified user and group.
   * 
   * @param userId
   *          The user
   * @param groupId
   *          The group
   * @param appId
   *          The app
   * @param fields
   *          The fields to delete. Empty set implies all
   * @param token
   *          The security token
   * @return an error if one occurs
   */
  public Future<Void> deletePersonData(UserId userId, GroupId groupId, String appId, Set<String> fields,
      SecurityToken token) throws ProtocolException {

    initDB();

    String user = userId.getUserId(token);

    Map<String, String> personData = db.getAppData(user);

    Iterator<String> iteratorFields = fields.iterator();
    while (iteratorFields.hasNext()) {
      personData.remove(iteratorFields.next());
    }

    return ImmediateFuture.newInstance(null);
  }

  /**
   * Retrives app data for the specified user list and group.
   * 
   * @param userIds
   *          A set of UserIds.
   * @param groupId
   *          The group
   * @param appId
   *          The app
   * @param fields
   *          The fields to filter the data by. Empty set implies all
   * @param token
   *          The security token
   * @return The data fetched
   */
  public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId, Set<String> fields,
      SecurityToken token) throws ProtocolException {

    Map<String, Map<String, String>> idToData = Maps.newHashMap();

    initDB();

    List<Person> personList = db.getRelatedPersons(userIds, groupId, token);
    Iterator<Person> iter = personList.iterator();
    while (iter.hasNext()) {
      Person currentPerson = iter.next();

      Map<String, String> personData = db.getAppData(currentPerson.getId());
      idToData.put(currentPerson.getId(), personData);
    }

    return ImmediateFuture.newInstance(new DataCollection(idToData));
  }

  /**
   * Updates app data for the specified user and group with the new values.
   * 
   * @param userId
   *          The user
   * @param groupId
   *          The group
   * @param appId
   *          The app
   * @param fields
   *          The fields to filter the data by. Empty set implies all
   * @param values
   *          The values to set
   * @param token
   *          The security token
   * @return an error if one occurs
   */
  public Future<Void> updatePersonData(UserId userId, GroupId groupId, String appId, Set<String> fields,
      Map<String, String> values, SecurityToken token) throws ProtocolException {

    initDB();

    String user = userId.getUserId(token);

    Map<String, String> personData = db.getAppData(user);
    for (Map.Entry<String, String> entry : values.entrySet()) {
      personData.put(entry.getKey(), entry.getValue());
    }

    return ImmediateFuture.newInstance(null);
  }
}
