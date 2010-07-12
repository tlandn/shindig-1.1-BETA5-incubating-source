/*
 ===========================================================================
 @    $Author$
 @  $Revision$
 @      $Date$
 @
 ===========================================================================
 */
package org.apache.shindig.social.sample.spi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.apache.shindig.common.testing.FakeGadgetToken;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Testclass for ShindigIntegratorAppDataService
 */
public class ShindigIntegratorAppDataServiceTest {
  private static final UserId JOHN_DOE = new UserId(UserId.Type.userId, "john.doe");
  private static final UserId JANE_DOE = new UserId(UserId.Type.userId, "jane.doe");
  private static final UserId MAIJA_M = new UserId(UserId.Type.userId, "maija.m");

  private static final GroupId SELF_GROUP = new GroupId(GroupId.Type.self, null);

  private static final String APP_ID = "";

  private AppDataService appDataService;

  /**
   * Test setup
   */
  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new ShindigIntegratorTestsGuiceModule());
    appDataService = injector.getInstance(ShindigIntegratorAppDataService.class);
    ((ShindigIntegratorAppDataService)appDataService).setPersistenceAdapter(new ShindigIntegratorPersistenceAdapter());
  }

  @Test
  public void testGetExpectedPersonData() throws Exception {
    DataCollection responseItem = appDataService.getPersonData(Sets.newHashSet(JOHN_DOE), SELF_GROUP, APP_ID,
        Collections.<String> emptySet(), new FakeGadgetToken()).get();

    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(JOHN_DOE.getUserId()).isEmpty());

    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).size() == 3);
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_1.HIGHSCORE"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_1.RANG"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_2.KEY1"));
  }

  @Test
  public void testGetExpectedAppDataForPlural() throws Exception {
    DataCollection responseItem = appDataService.getPersonData(Sets.newHashSet(JOHN_DOE, MAIJA_M), SELF_GROUP, APP_ID,
        Collections.<String> emptySet(), new FakeGadgetToken()).get();

    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(JOHN_DOE.getUserId()).isEmpty());

    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).size() == 3);
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_1.HIGHSCORE"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_1.RANG"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_2.KEY1"));

    assertFalse(responseItem.getEntry().get(MAIJA_M.getUserId()).isEmpty());
    assertTrue(responseItem.getEntry().get(MAIJA_M.getUserId()).size() == 2);
    assertTrue(responseItem.getEntry().get(MAIJA_M.getUserId()).containsKey("APP_1.HIGHSCORE"));
    assertTrue(responseItem.getEntry().get(MAIJA_M.getUserId()).containsKey("APP_1.RANG"));

    assertFalse(responseItem.getEntry().containsKey(JANE_DOE.getUserId()));
  }

  @Test
  public void testDeleteExpectedAppData() throws Exception {
    // Delete data from User
    appDataService.deletePersonData(JOHN_DOE, SELF_GROUP, APP_ID, Sets.newHashSet("APP_1.HIGHSCORE"),
        new FakeGadgetToken());

    // Fetch the remaining and test
    DataCollection responseItem = appDataService.getPersonData(Sets.newHashSet(JOHN_DOE), SELF_GROUP, APP_ID,
        Collections.<String> emptySet(), new FakeGadgetToken()).get();

    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(JOHN_DOE.getUserId()).isEmpty());

    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).size() == 2);
    assertFalse(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_1.HIGHSCORE"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_1.RANG"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_2.KEY1"));
  }

  @Test
  public void testUpdateExpectedAppData() throws Exception {
    // Update data from User
    appDataService.updatePersonData(JOHN_DOE, SELF_GROUP, APP_ID, null, ImmutableMap.of("APP_1.HIGHSCORE", "444",
        "newvalue", "20"), new FakeGadgetToken());
    
    // Fetch the remaining and test
    DataCollection responseItem = appDataService.getPersonData(Sets.newHashSet(JOHN_DOE), SELF_GROUP, APP_ID,
        Collections.<String> emptySet(), new FakeGadgetToken()).get();

    assertFalse(responseItem.getEntry().isEmpty());
    assertFalse(responseItem.getEntry().get(JOHN_DOE.getUserId()).isEmpty());

    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).size() == 4);
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("APP_1.HIGHSCORE"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).get("APP_1.HIGHSCORE").equals("444"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).containsKey("newvalue"));
    assertTrue(responseItem.getEntry().get(JOHN_DOE.getUserId()).get("newvalue").equals("20"));
  }
}
