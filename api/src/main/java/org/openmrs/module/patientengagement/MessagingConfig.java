package org.openmrs.module.patientengagement;

public class MessagingConfig {
	
	private String serviceUUID;
	
	private int daysBefore;
	
	private String messageText;
	
	public MessagingConfig() {
		super();
	}
	
	public MessagingConfig(String serviceUUID, int daysBefore, String messageText) {
		super();
		this.serviceUUID = serviceUUID;
		this.daysBefore = daysBefore;
		this.messageText = messageText;
	}
	
	public String getServiceUUID() {
		return serviceUUID;
	}
	
	public void setServiceUUID(String serviceUUID) {
		this.serviceUUID = serviceUUID;
	}
	
	public int getDaysBefore() {
		return daysBefore;
	}
	
	public void setDaysBefore(int daysBefore) {
		this.daysBefore = daysBefore;
	}
	
	public String getMessageText() {
		return messageText;
	}
	
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	
}
