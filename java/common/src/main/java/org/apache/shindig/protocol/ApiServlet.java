/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.shindig.protocol;

import org.apache.shindig.auth.AuthInfo;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.servlet.InjectedServlet;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Common base class for API servlets.
 */
public abstract class ApiServlet extends InjectedServlet {

  private static final Logger logger = Logger.getLogger(ApiServlet.class.getName());

  protected static final String FORMAT_PARAM = "format";
  protected static final String JSON_FORMAT = "json";
  protected static final String ATOM_FORMAT = "atom";
  protected static final String XML_FORMAT = "xml";

  protected static final String DEFAULT_ENCODING = "UTF-8";

  /** ServletConfig parameter set to provide an explicit named binding for handlers */
  public static final String HANDLERS_PARAM = "handlers";

  /** The default key used to look up handlers if the servlet config parameter is not available */
  public static final Key<Set<Object>> DEFAULT_HANDLER_KEY =
       Key.get(new TypeLiteral<Set<Object>>(){}, Names.named("org.apache.shindig.protocol.handlers"));

  protected HandlerRegistry dispatcher;
  protected BeanJsonConverter jsonConverter;
  protected BeanConverter xmlConverter;
  protected BeanConverter atomConverter;

  @Deprecated
  protected boolean disallowUnknownContentTypes = true;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    // Lookup the set of handlers to bind to this api endpoint and
    // populate the registry with them
    String handlers = config.getInitParameter(HANDLERS_PARAM);
    Key<Set<Object>> handlerKey;
    if (handlers == null || "".equals(handlers)) {
      handlerKey = DEFAULT_HANDLER_KEY;
    } else {
      handlerKey = Key.get(new TypeLiteral<Set<Object>>(){}, Names.named(handlers));
    }
    this.dispatcher.addHandlers(injector.getInstance(handlerKey));
    this.dispatcher.addHandlers(Collections.<Object>singleton(new SystemHandler(dispatcher)));
  }

  @Inject
  public void setHandlerRegistry(HandlerRegistry dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Inject(optional = true)
  public void setDisallowUnknownContentTypes(
      @Named("shindig.api.disallow-unknown-content-types") boolean disallowUnknownContentTypes) {
    this.disallowUnknownContentTypes = disallowUnknownContentTypes;
  }

  @Inject
  public void setBeanConverters(
      @Named("shindig.bean.converter.json") BeanConverter jsonConverter,
      @Named("shindig.bean.converter.xml") BeanConverter xmlConverter,
      @Named("shindig.bean.converter.atom") BeanConverter atomConverter) {
    // fix this
    this.jsonConverter = (BeanJsonConverter) jsonConverter;
    this.xmlConverter = xmlConverter;
    this.atomConverter = atomConverter;
  }

  protected SecurityToken getSecurityToken(HttpServletRequest servletRequest) {
    return new AuthInfo(servletRequest).getSecurityToken();
  }

  protected abstract void sendError(HttpServletResponse servletResponse, ResponseItem responseItem)
      throws IOException;

  protected void sendSecurityError(HttpServletResponse servletResponse) throws IOException {
    sendError(servletResponse, new ResponseItem(HttpServletResponse.SC_UNAUTHORIZED,
        "The request did not have a proper security token nor oauth message and unauthenticated "
            + "requests are not allowed"));
  }

  protected ResponseItem getResponseItem(Future<?> future) {
    ResponseItem response;
    try {
      // TODO: use timeout methods?
      Object result = future != null ? future.get() : null;
      // TODO: null is now a supported return value for post/delete, but
      // is bad for get().
      response = new ResponseItem(result != null ? result : Collections.emptyMap());
    } catch (InterruptedException ie) {
      response = responseItemFromException(ie);
    } catch (ExecutionException ee) {
      response = responseItemFromException(ee.getCause());
    }

    return response;
  }

  protected ResponseItem responseItemFromException(Throwable t) {
    if (t instanceof ProtocolException) {
      ProtocolException pe = (ProtocolException) t;
      logger.log(Level.INFO, "Returning a response error as result of a protocol exception", pe);
      return new ResponseItem(pe.getCode(), pe.getMessage(), pe.getResponse());
    }
    logger.log(Level.WARNING, "Returning a response error as result of an exception", t);
    return new ResponseItem(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
  }

  protected void setCharacterEncodings(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws IOException {
    if (servletRequest.getCharacterEncoding() == null) {
      servletRequest.setCharacterEncoding(DEFAULT_ENCODING);
    }
    servletResponse.setCharacterEncoding(DEFAULT_ENCODING);
  }

  public void checkContentTypes(Set<String> allowedContentTypes,
      String contentType) throws ContentTypes.InvalidContentTypeException {
    ContentTypes.checkContentTypes(allowedContentTypes, contentType, disallowUnknownContentTypes);
  }
}
