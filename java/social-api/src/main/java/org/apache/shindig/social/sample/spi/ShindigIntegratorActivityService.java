package org.apache.shindig.social.sample.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;

import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * ActivityService-Implementation to integrate a community - DB into Shindig.
 */
public class ShindigIntegratorActivityService implements ActivityService {

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
	 * The JSON<->Bean converter
	 */
	private BeanConverter converter;

	@Inject
	public ShindigIntegratorActivityService(
			@Named("shindig.bean.converter.json") BeanConverter converter)
			throws Exception {
		this.converter = converter;
	}

	private boolean containsActivity(List<Activity> activityList, String id) {
		Iterator<Activity> iter = activityList.iterator();
		while (iter.hasNext()) {
			Activity currentActivity = iter.next();
			if (currentActivity.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	private JSONObject convertFromActivity(Activity activity, Set<String> fields)
			throws JSONException {
		return new JSONObject(converter.convertToString(activity));
	}

	/**
	 * Creates the passed in activity for the passed in user and group. Once
	 * createActivity is called, getActivities will be able to return the
	 * Activity.
	 * 
	 * @param userId
	 *            The id of the person to create the activity for.
	 * @param groupId
	 *            The group.
	 * @param appId
	 *            The app id.
	 * @param fields
	 *            The fields to return.
	 * @param activity
	 *            The activity to create.
	 * @param token
	 *            A valid SecurityToken
	 * @return a response item containing any errors
	 */
	public Future<Void> createActivity(UserId userId, GroupId groupId,
			String appId, Set<String> fields, Activity activity,
			SecurityToken token) throws ProtocolException {

		initDB();

		if (activity.getUserId() == null || activity.getUserId().equals("")) {
			activity.setUserId(userId.getUserId(token));
		}
		if (activity.getAppId() == null || activity.getAppId().equals("")) {
			activity.setAppId(appId);
		}

		db.addNewActivity(activity);

		return ImmediateFuture.newInstance(null);
	}

	public Future<Void> deleteActivities(UserId userId, GroupId groupId,
			String appId, Set<String> activityIds, SecurityToken token)
			throws ProtocolException {

		return ImmediateFuture.newInstance(null);
	}

	/**
	 * Returns a list of activities that correspond to the passed in users and
	 * group.
	 * 
	 * @param userIds
	 *            The set of ids of the people to fetch activities for.
	 * @param groupId
	 *            Indicates whether to fetch activities for a group.
	 * @param appId
	 *            The app id.
	 * @param fields
	 *            The fields to return. Empty set implies all
	 * @param token
	 *            A valid SecurityToken
	 * @return a response item with the list of activities.
	 */
	public Future<RestfulCollection<Activity>> getActivities(
			Set<UserId> userIds, GroupId groupId, String appId,
			Set<String> fields, CollectionOptions options, SecurityToken token)
			throws ProtocolException {
		List<Activity> result = new ArrayList<Activity>();

		initDB();

		List<Person> people = db.getRelatedPersons(userIds, groupId, token);
		Iterator<Person> iterPeople = people.iterator();
		while (iterPeople.hasNext()) {
			Person currentPerson = iterPeople.next();

			List<Activity> activities = db.getActivities(currentPerson.getId());
			Iterator<Activity> iter = activities.iterator();
			while (iter.hasNext()) {
				Activity currentActivity = iter.next();
				if (appId == null || currentActivity.getAppId().equals("")) {
					if (!containsActivity(result, currentActivity.getId())) {
						result.add(currentActivity);
					}
				} else if (currentActivity.getAppId().equals(appId)) {
					if (!containsActivity(result, currentActivity.getId())) {
						result.add(currentActivity);
					}
				}
			}
		}

		return ImmediateFuture.newInstance(new RestfulCollection<Activity>(
				result));
	}

	/**
	 * Returns a set of activities for the passed in user and group that
	 * corresponds to a list of activityIds.
	 * 
	 * @param userId
	 *            The set of ids of the people to fetch activities for.
	 * @param groupId
	 *            Indicates whether to fetch activities for a group.
	 * @param appId
	 *            The app id.
	 * @param fields
	 *            The fields to return. Empty set implies all
	 * @param activityIds
	 *            The set of activity ids to fetch.
	 * @param token
	 *            A valid SecurityToken
	 * @return a response item with the list of activities.
	 */
	public Future<RestfulCollection<Activity>> getActivities(UserId userId,
			GroupId groupId, String appId, Set<String> fields,
			CollectionOptions options, Set<String> activityIds,
			SecurityToken token) throws ProtocolException {
		List<Activity> result = new ArrayList<Activity>();

		initDB();

		String user = userId.getUserId(token);

		List<Activity> activities = db.getActivities(user);
		Iterator<Activity> iter = activities.iterator();
		while (iter.hasNext()) {
			Activity currentActivity = iter.next();
			if (activityIds.contains(currentActivity.getId())) {
				result.add(currentActivity);
			}
		}

		return ImmediateFuture.newInstance(new RestfulCollection<Activity>(
				result));
	}

	/**
	 * Returns a activity for the passed in user and group that corresponds to a
	 * single of activityId
	 * 
	 * @param userId
	 *            The set of ids of the people to fetch activities for.
	 * @param groupId
	 *            Indicates whether to fetch activities for a group.
	 * @param appId
	 *            The app id.
	 * @param fields
	 *            The fields to return. Empty set implies all
	 * @param activityId
	 *            The activity id to fetch.
	 * @param token
	 *            A valid SecurityToken
	 * @return a response item with the list of activities.
	 */
	public Future<Activity> getActivity(UserId userId, GroupId groupId,
			String appId, Set<String> fields, String activityId,
			SecurityToken token) throws ProtocolException {

		initDB();

		String user = userId.getUserId(token);

		Activity currentActivity = db.findActivity(activityId);
		if (currentActivity != null) {
			if (currentActivity.getUserId().equals(user)) {
				return ImmediateFuture.newInstance(currentActivity);
			}
		}

		throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,
				"Activity not found");
	}
}
