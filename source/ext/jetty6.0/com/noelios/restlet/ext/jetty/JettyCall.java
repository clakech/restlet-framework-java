/*
 * Copyright 2005 J�r�me LOUVEL
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.jetty;

import java.util.ArrayList;
import java.util.List;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpHeaders;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.CharacterSets;
import org.restlet.data.Cookies;
import org.restlet.data.Languages;
import org.restlet.data.MediaTypes;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Representation;
import org.restlet.data.Security;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.ChallengeResponseImpl;
import com.noelios.restlet.data.ChallengeSchemeImpl;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.MediaTypeImpl;
import com.noelios.restlet.data.MethodImpl;
import com.noelios.restlet.data.PreferenceImpl;
import com.noelios.restlet.data.PreferenceReaderImpl;
import com.noelios.restlet.data.ReferenceImpl;
import com.noelios.restlet.data.SecurityImpl;

/**
 * Call that is used by the Jetty HTTP server connector.
 */
public class JettyCall extends UniformCallImpl
{
   /**
    * Constructor.
    * @param request The Jetty HTTP request.
    * @param response The Jetty HTTP response.
    */
   public JettyCall(HttpConnection connection)
   {
      super(getReferrer(connection), getUserAgentName(connection), getMediaPrefs(connection),
            getCharacterSetPrefs(connection), getLanguagePrefs(connection), getMethod(connection),
            getResource(connection), getCookies(connection), getInput(connection));
      setClientAddress(connection.getRequest().getRemoteAddr());
   }

   /**
    * Extracts the call's referrer from the HTTP request.
    * @param connection The Jetty HTTP connection.
    * @return The call's referrer.
    */
   private static Reference getReferrer(HttpConnection connection)
   {
      String referrer = connection.getRequest().getHeader("Referer");

      if(referrer != null)
      {
         return new ReferenceImpl(referrer);
      }
      else
      {
         return null;
      }
   }

   /**
    * Extracts the call's referrer from the HTTP request.
    * @param connection The Jetty HTTP connection.
    * @return The call's referrer.
    */
   private static String getUserAgentName(HttpConnection connection)
   {
      return connection.getRequest().getHeader(HttpHeaders.USER_AGENT);
   }

   /**
    * Extracts the call's resource from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's resource.
    */
   private static Reference getResource(HttpConnection connection)
   {
      String resource = connection.getRequest().getRequestURL().toString();

      if(resource != null)
      {
         return new ReferenceImpl(resource);
      }
      else
      {
         return null;
      }
   }

   /**
    * Extracts the call's method from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's method.
    */
   private static Method getMethod(HttpConnection connection)
   {
      String method = connection.getRequest().getMethod();

      if(method != null)
      {
      return new MethodImpl(method);
      }
      else
      {
      return null;
      }
   }

   /**
    * Extracts the call's input representation from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's input representation.
    */
   private static Representation getInput(HttpConnection connection)
   {
      return new InputRepresentation(connection.getInputStream(),
           new MediaTypeImpl(connection.getRequest().getContentType()));
   }

   /**
    * Extracts the call's media preferences from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's media preferences.
    */
   private static List<Preference> getMediaPrefs(HttpConnection connection)
   {
      List<Preference> result = null;
      String accept = connection.getRequest().getHeader(HttpHeaders.ACCEPT);

      if(accept != null)
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_MEDIA_TYPE, accept);
         result = pr.readPreferences();
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(MediaTypes.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's character set preferences from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's character set preferences.
    */
   private static List<Preference> getCharacterSetPrefs(HttpConnection connection)
   {
      // Implementation according to
      // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
      List<Preference> result = null;
      String acceptCharset = connection.getRequest().getHeader(HttpHeaders.ACCEPT_CHARSET);

      if(acceptCharset != null)
      {
         if(acceptCharset.length() == 0)
         {
            result = new ArrayList<Preference>();
            result.add(new PreferenceImpl(CharacterSets.ISO_8859_1));
         }
         else
         {
            PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_CHARACTER_SET,
                  acceptCharset);
            result = pr.readPreferences();
         }
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(CharacterSets.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's language preferences from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's language preferences.
    */
   private static List<Preference> getLanguagePrefs(HttpConnection connection)
   {
      List<Preference> result = null;
      String acceptLanguage = connection.getRequest().getHeader(HttpHeaders.ACCEPT_LANGUAGE);

      if(acceptLanguage != null)
      {
         PreferenceReaderImpl pr = new PreferenceReaderImpl(PreferenceReaderImpl.TYPE_LANGUAGE,
               acceptLanguage);
         result = pr.readPreferences();
      }
      else
      {
         result = new ArrayList<Preference>();
         result.add(new PreferenceImpl(Languages.ALL));
      }

      return result;
   }

   /**
    * Extracts the call's cookies from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's cookies.
    */
   private static Cookies getCookies(HttpConnection connection)
   {
      Cookies result = null;
      String cookieHeader = connection.getRequest().getHeader(HttpHeaders.COOKIE);

      if(cookieHeader != null)
      {
         result = new com.noelios.restlet.data.CookiesImpl(cookieHeader);
      }

      return result;
   }

   /**
    * Ectracts the call's security data from the HTTP request.
    * @param connection The Jetty HTTP request.
    * @return The call's security data.
    */
   private static Security getSecurity(HttpConnection connection)
   {
      Security result = new SecurityImpl();
      result.setConfidential(connection.getRequest().isSecure());

      String authorization = connection.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
      if(authorization != null)
      {
         int space = authorization.indexOf(' ');

         if(space != -1)
         {
            String scheme = authorization.substring(0, space);
            String credentials = authorization.substring(space + 1);
            ChallengeResponse challengeResponse = new ChallengeResponseImpl(new ChallengeSchemeImpl("HTTP_" + scheme, scheme), credentials);
            result.setChallengeResponse(challengeResponse);
         }
      }

      return result;
   }

}
