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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.patientengagement.MessagingConfig;
import org.openmrs.module.patientengagement.api.PatientEngagementService;
import org.openmrs.module.patientengagement.api.dao.PatientEngagementDao;
import org.openmrs.module.patientengagement.util.MessagingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 * 
 * @author Bailly RURANGIRWA
 */
@Service("patientEngagementService")
public class PatientEngagementServiceImpl extends BaseOpenmrsService implements PatientEngagementService {
	
	private static final Logger log = LoggerFactory.getLogger(PatientEngagementServiceImpl.class);
	
	@Autowired
	AppointmentServiceService ass;
	
	@Autowired
	AppointmentsService as;
	
	PatientEngagementDao dao;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(PatientEngagementDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Gets a list of MessagingConfig objects from calling {@link #getMessagingConfig()}. Each
	 * configuration object from that list is used to read the service configured, the number of
	 * days before the appointment day and the actual message to send. We then call
	 * {@link #postMessage()} of MessagingUtil to send the actual message
	 */
	
	@Override
	public void sendAppointmentReminders() throws AuthenticationException, ClientProtocolException, IOException {
		
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		try {
			List<MessagingConfig> configs = MessagingUtil.getMessagingConfig();
			for (MessagingConfig messagingConfig : configs) {
				AppointmentService service = ass.getAppointmentServiceByUuid(messagingConfig.getServiceUUID());
				List<Appointment> appointments = as.getAllFutureAppointmentsForService(service);
				String phone = null;
				for (Appointment appointment : appointments) {
					if (Days.daysBetween(new DateTime(new Date()), new DateTime(appointment.getStartDateTime())).getDays() == messagingConfig.getDaysBefore() - 1) {
						phone = appointment.getPatient().getAttribute(Context.getAdministrationService().getGlobalProperty("patientengagement.phoneAttribute")).getValue();
						if (phone != null && phone.length() > 0 && phone.equalsIgnoreCase("09289309869")) {
							Patient p = appointment.getPatient();
							String patientName = p.getFamilyName() + " " + p.getMiddleName() + " " + p.getGivenName();
							Date appointmentDate = appointment.getStartDateTime();
							String messageOne = messagingConfig.getMessageText().replace("patientName", patientName);
							String message = messageOne.replace("appointmentDate", dateFormat.format(appointmentDate));
							MessagingUtil.postMessage(phone, message);
						}
					}
				}
				
			}
		}
		catch (Exception e) {
			log.error("There was an error sending appointment reminders" + e);
		}
		
	}
	
}
