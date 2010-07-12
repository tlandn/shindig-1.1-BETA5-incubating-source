/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.social.sample.spi;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.conversion.BeanXStreamConverter;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Provides social api component injection for ShindigIntegrator tests
 */
public class ShindigIntegratorTestsGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PersonService.class).to(ShindigIntegratorPersonService.class);
    bind(AppDataService.class).to(ShindigIntegratorAppDataService.class);
    bind(ActivityService.class).to(ShindigIntegratorActivityService.class);

    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.xml")).to(BeanXStreamConverter.class);
    bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
  }
}
