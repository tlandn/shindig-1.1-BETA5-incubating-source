/*
 ===========================================================================
 @    $Author$
 @  $Revision$
 @      $Date$
 @
 ===========================================================================
 */
package org.apache.shindig.social.sample.spi;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.shindig.common.testing.FakeGadgetToken;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Testclass for ShindigIntegratorActivityService
 */
public class ShindigIntegratorActivityServiceTest {

	private static final UserId JOHN_DOE = new UserId(UserId.Type.userId,
			"john.doe");
	private static final UserId GEORGE_DOE = new UserId(UserId.Type.userId,
			"george.doe");

	private static final GroupId SELF_GROUP = new GroupId(GroupId.Type.self,
			null);

	private static final String APP_ID = "App1";

	private ActivityService activityService;

	private boolean containsActivity(List<Activity> activityList, String title) {
		Iterator<Activity> iter = activityList.iterator();
		while (iter.hasNext()) {
			Activity currentActivity = iter.next();
			if (currentActivity.getTitle().equals(title)) {
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
		activityService = injector
				.getInstance(ShindigIntegratorActivityService.class);
		((ShindigIntegratorActivityService) activityService)
				.setPersistenceAdapter(new ShindigIntegratorPersistenceAdapter());
	}

	@Test
	public void testGetExpectedActivities() throws Exception {
		RestfulCollection<Activity> responseItem = activityService
				.getActivities(Sets.newHashSet(JOHN_DOE), SELF_GROUP, APP_ID,
						Collections.<String> emptySet(), null,
						new FakeGadgetToken()).get();
		assertTrue(responseItem.getTotalResults() == 2);
	}

	@Test
	public void testGetExpectedActivitiesForPlural() throws Exception {
		RestfulCollection<Activity> responseItem = activityService
				.getActivities(Sets.newHashSet(GEORGE_DOE, JOHN_DOE),
						SELF_GROUP, APP_ID, Collections.<String> emptySet(),
						null, new FakeGadgetToken()).get();
		assertTrue(responseItem.getTotalResults() == 3);
	}

	@Test
	public void testCreateActivity() throws Exception {
		Activity activity = new ActivityImpl();
		activity.setTitle("New Test Activity");
		activity.setBody("Test Activitiy Body message");

		activityService.createActivity(JOHN_DOE, new GroupId(GroupId.Type.self,
				null), APP_ID, Sets.<String> newHashSet(), activity,
				new FakeGadgetToken());

		// Test if the new activity can be found via getActivites
		RestfulCollection<Activity> responseItem = activityService
				.getActivities(Sets.newHashSet(JOHN_DOE), SELF_GROUP, APP_ID,
						Collections.<String> emptySet(), null,
						new FakeGadgetToken()).get();
		List<Activity> activities = responseItem.getEntry();
		assertTrue(containsActivity(activities, "New Test Activity"));
	}
}
