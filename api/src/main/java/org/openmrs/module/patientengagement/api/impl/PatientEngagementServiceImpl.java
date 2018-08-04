/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientengagement.api.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.patientengagement.Item;
import org.openmrs.module.patientengagement.MessagingConfig;
import org.openmrs.module.patientengagement.api.PatientEngagementService;
import org.openmrs.module.patientengagement.api.dao.PatientEngagementDao;
import org.openmrs.module.patientengagement.util.MessagingUtil;

public class PatientEngagementServiceImpl extends BaseOpenmrsService implements PatientEngagementService {
	
	PatientEngagementDao dao;
	
	UserService userService;
	
	AppointmentsService as;
	
	AppointmentServiceService ass;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(PatientEngagementDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	public void setAs(AppointmentsService as) {
		this.as = as;
	}
	
	public void setAss(AppointmentServiceService ass) {
		this.ass = ass;
	}
	
	@Override
	public Item getItemByUuid(String uuid) throws APIException {
		return dao.getItemByUuid(uuid);
	}
	
	@Override
	public Item saveItem(Item item) throws APIException {
		if (item.getOwner() == null) {
			item.setOwner(userService.getUser(1));
		}
		
		return dao.saveItem(item);
	}
	
	@Override
	public void sendAppointmentReminders() throws AuthenticationException, ClientProtocolException, IOException {
		
		List<MessagingConfig> configs = MessagingUtil.getMessagingConfig();
		for (MessagingConfig messagingConfig : configs) {
			AppointmentService service = ass.getAppointmentServiceByUuid(messagingConfig.getServiceUUID());
			List<Appointment> appointments = as.getAllFutureAppointmentsForService(service);
			
			for (Appointment appointment : appointments) {
				if (Days.daysBetween(new DateTime(appointment.getStartDateTime()), new DateTime(new Date())).getDays() == messagingConfig.getDaysBefore()) {
					String phone = appointment.getPatient().getAttribute("mobilePhone").getValue();
					if (phone != null && phone.length() > 0) {
						MessagingUtil.postMessage(phone, messagingConfig.getMessageText());
					}
				}
			}
			
		}
		
	}
	
}
