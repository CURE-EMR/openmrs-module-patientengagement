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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientengagement.MessagingConfig;

/**
 * Utility class for patientengagement module. It includes methods for connecting to RapidPro for
 * posting JSON requests and methods for parsing patientengagement module global properties
 * configurations.
 * 
 * @author Bailly RURANGIRWA
 */
public class MessagingUtil {
	
	//Get the admin service for reading global properties
	private static AdministrationService adminService = Context.getAdministrationService();
	
	/**
	 * Reads configuration for sending appointment reminders form
	 * "patientengagement.messagingConfig" global property and creates a list of MessagingConfig
	 * instances
	 * 
	 * @return a list of MessagingConfig objects
	 */
	public static List<MessagingConfig> getMessagingConfig() {
		
		List<MessagingConfig> list = null;
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			String json = adminService.getGlobalProperty("patientengagement.messagingConfig");
			list = mapper.readValue(json, new TypeReference<List<MessagingConfig>>() {});
			
		}
		catch (JsonGenerationException e) {
			e.printStackTrace();
		}
		catch (JsonMappingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Creates a JSON post request to a configured URL from "patientengagement.postURL" global
	 * property.
	 * 
	 * @param phone The phone number to send the message to
	 * @param messageText The actual message to send
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public static void postMessage(String phone, String messageText) throws ClientProtocolException, IOException, AuthenticationException {
		
		String json = "{\r\n    \"urns\": [\"tel:\"" + phone + "\"], \r\n    \"text\": \"" + messageText + "\"\r\n}";
		
		HttpPost httpPost = new HttpPost(adminService.getGlobalProperty("patientengagement.postURL"));
		httpPost.setEntity(new StringEntity(json));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("Authorization", adminService.getGlobalProperty("patientengagement.Authorization"));
		
		CloseableHttpClient client = HttpClients.createDefault();
		client.execute(httpPost);
		client.close();
	}
}
