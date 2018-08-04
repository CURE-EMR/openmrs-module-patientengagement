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
import org.openmrs.api.context.Context;
import org.openmrs.module.patientengagement.MessagingConfig;

public class MessagingUtil {
	
	public static List<MessagingConfig> getMessagingConfig() {
		
		List<MessagingConfig> list = null;
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			String json = Context.getAdministrationService().getGlobalProperty("patientengagement.messagingConfig");
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
	
	public static void postMessage(String phone, String messageText) throws ClientProtocolException, IOException, AuthenticationException {
		
		String json = "{\r\n    \"urns\": [\"tel:" + phone + "\"], \r\n    \"text\": " + messageText + "\r\n}";
		
		HttpPost httpPost = new HttpPost(Context.getAdministrationService().getGlobalProperty("patientengagement.postUR"));
		httpPost.setEntity(new StringEntity(json));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setHeader("Authorization", Context.getAdministrationService().getGlobalProperty("patientengagement.Authorization"));
		
		CloseableHttpClient client = HttpClients.createDefault();
		client.execute(httpPost);
		client.close();
	}
}
