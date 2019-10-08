/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientengagement.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientengagement.MessagingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for patientengagement module. It includes methods for connecting to RapidPro for
 * posting JSON requests and methods for parsing patientengagement module global properties
 * configurations.
 * 
 * @author Bailly RURANGIRWA
 */
public class MessagingUtil {
	
	private static final Logger log = LoggerFactory.getLogger(MessagingUtil.class);
	
	/**
	 * Reads configuration for sending appointment reminders form "patientengagement.messagingConfig"
	 * global property and creates a list of MessagingConfig instances
	 * 
	 * @return a list of MessagingConfig objects
	 */
	public static List<MessagingConfig> getMessagingConfig() {
		
		List<MessagingConfig> list = null;
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			System.setProperty("file.encoding", "UTF-8");
			Field charset = Charset.class.getDeclaredField("defaultCharset");
			charset.setAccessible(true);
			charset.set(null, null);
			
			String json = Context.getAdministrationService().getGlobalProperty("patientengagement.messagingConfig");
			
			list = mapper.readValue(json, new TypeReference<List<MessagingConfig>>() {});
			
		}
		catch (JsonGenerationException e) {
			log.error("There was an error parsing the JSON configuration string from patientengagement.messagingConfig global property: " + e);
		}
		catch (JsonMappingException e) {
			log.error("There was an error parsing the JSON configuration string from patientengagement.messagingConfig global property. Please, check the mappings of the JSON keys to MessagingConfig properties " + e);
		}
		catch (IOException e) {
			log.error("There was an error parsing the JSON configuration string from patientengagement.messagingConfig global property: " + e);
		}
		catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Creates a JSON post request to a configured URL from "patientengagement.postURL" global property.
	 * 
	 * @param phone The phone number to send the message to
	 * @param messageText The actual message to send
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public static void postMessage(String phone, String messageText) throws ClientProtocolException, IOException, AuthenticationException {
		String countryCode = Context.getAdministrationService().getGlobalProperty("patientengagement.countryCode");
		
		String fixedPhoneNumber = phone.replaceFirst("0", countryCode);
		String json = "{ \"urns\": [ \"tel:" + fixedPhoneNumber + "\"], \"text\": \"" + new String(messageText.getBytes("UTF-8"), "ISO-8859-1") + "\" }";
		HttpPost httpPost = new HttpPost(Context.getAdministrationService().getGlobalProperty("patientengagement.postURL"));
		httpPost.setEntity(new StringEntity(json));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("Authorization", Context.getAdministrationService().getGlobalProperty("patientengagement.Authorization"));
		
		CloseableHttpClient client = HttpClients.createDefault();
		client.execute(httpPost);
		client.close();
	}
	
	public static void postBirthdayWishes(String phone, String messageText) throws ClientProtocolException, IOException, AuthenticationException {
		String countryCode = Context.getAdministrationService().getGlobalProperty("patientengagement.countryCode");
		String fixedPhoneNumber = "";
		if (Character.compare(phone.charAt(0), '0') == 0) {
			fixedPhoneNumber = phone.replaceFirst("0", countryCode);
		}
		if (Character.compare(phone.charAt(0), '7') == 0) {
			fixedPhoneNumber = phone.replaceFirst("7", countryCode + "7");
		}
		String json = "{ \"urns\": [ \"tel:" + fixedPhoneNumber + "\"], \"text\": \"" + new String(messageText.getBytes("UTF-8"), "ISO-8859-1") + "\" }";
		HttpPost httpPost = new HttpPost(Context.getAdministrationService().getGlobalProperty("patientengagement.postURL"));
		httpPost.setEntity(new StringEntity(json));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("Authorization", Context.getAdministrationService().getGlobalProperty("patientengagement.Authorization"));
		
		CloseableHttpClient client = HttpClients.createDefault();
		client.execute(httpPost);
		client.close();
	}
}
