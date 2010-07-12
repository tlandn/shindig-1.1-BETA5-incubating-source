package org.apache.shindig.social.core.config;

import java.util.Set;

import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.sample.oauth.SampleOAuthDataStore;
import org.apache.shindig.social.sample.oauth.SampleRealm;
import org.apache.shindig.social.sample.service.SampleContainerHandler;
import org.apache.shindig.social.sample.spi.ShindigIntegratorActivityService;
import org.apache.shindig.social.sample.spi.ShindigIntegratorAppDataService;
import org.apache.shindig.social.sample.spi.ShindigIntegratorOAuthDataStore;
import org.apache.shindig.social.sample.spi.ShindigIntegratorPersonService;

import com.google.common.collect.ImmutableSet;
import com.google.inject.name.Names;

/**
 * Configuration Module to integrate integrate a community - DB into Shindig.
 */
public class ShindigIntegratorGuiceModule extends SocialApiGuiceModule {

	@Override
	protected Set<Object> getHandlers() {
		ImmutableSet.Builder<Object> handlers = ImmutableSet.builder();
		handlers.addAll(super.getHandlers());
		handlers.add(SampleContainerHandler.class);
		return handlers.build();
	}

	@Override
	protected void configure() {
		super.configure();

		bind(PersonService.class).to(ShindigIntegratorPersonService.class);
		bind(AppDataService.class).to(ShindigIntegratorAppDataService.class);
		bind(ActivityService.class).to(ShindigIntegratorActivityService.class);

		bind(String.class).annotatedWith(
				Names.named("shindig.canonical.json.db")).toInstance(
				"sampledata/canonicaldb.json");
		bind(OAuthDataStore.class).to(SampleOAuthDataStore.class);

		// We do this so that jsecurity realms can get access to the
		// jsondbservice singleton
		requestStaticInjection(SampleRealm.class);

		// bind(ParameterFetcher.class).annotatedWith(
		// Names.named("DataServiceServlet")).to(
		// DataServiceServletFetcher.class);

		// bind(Boolean.class)
		// .annotatedWith(
		// Names.named(AnonymousAuthenticationHandler.ALLOW_UNAUTHENTICATED))
		// .toInstance(Boolean.TRUE);

		// bind(BeanConverter.class).annotatedWith(
		// Names.named("shindig.bean.converter.xml")).to(
		// BeanXStreamConverter.class);
		// bind(BeanConverter.class).annotatedWith(
		// Names.named("shindig.bean.converter.json")).to(
		// BeanJsonConverter.class);
		// bind(BeanConverter.class).annotatedWith(
		// Names.named("shindig.bean.converter.atom")).to(
		// BeanXStreamAtomConverter.class);
		//
		// bind(new TypeLiteral<List<AuthenticationHandler>>() {
		// }).toProvider(AuthenticationHandlerProvider.class);
	}

}
