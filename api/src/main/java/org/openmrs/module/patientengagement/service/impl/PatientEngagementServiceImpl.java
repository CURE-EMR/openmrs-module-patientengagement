/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientengagement.service.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.patientengagement.MessagingConfig;
import org.openmrs.module.patientengagement.dao.PatientEngagementDao;
import org.openmrs.module.patientengagement.service.PatientEngagementService;
import org.openmrs.module.patientengagement.util.MessagingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 * 
 * @author Bailly RURANGIRWA
 */
@Transactional
public class PatientEngagementServiceImpl extends BaseOpenmrsService implements PatientEngagementService {
	
	private static final Logger log = LoggerFactory.getLogger(PatientEngagementServiceImpl.class);
	
	PatientEngagementDao patientEngagementDao;
	
	public void setPatientEngagementDao(PatientEngagementDao patientEngagementDao) {
		this.patientEngagementDao = patientEngagementDao;
	}
	
	/**
	 * Gets a list of MessagingConfig objects from calling {@link #getMessagingConfig()}. Each
	 * configuration object from that list is used to read the service configured, the number of days
	 * before the appointment day and the actual message to send. We then call {@link #postMessage()} of
	 * MessagingUtil to send the actual message
	 */
	
	@Override
	public void sendAppointmentReminders() throws AuthenticationException, ClientProtocolException, IOException {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		
		try {
			List<MessagingConfig> configs = MessagingUtil.getMessagingConfig();
			for (MessagingConfig messagingConfig : configs) {
				AppointmentServiceDefinition service = Context.getService(AppointmentServiceDefinitionService.class).getAppointmentServiceByUuid(messagingConfig.getServiceUUID());
				List<Appointment> appointments = Context.getService(AppointmentsService.class).getAllFutureAppointmentsForService(service);
				String phone = null;
				for (Appointment appointment : appointments) {
					if (Days.daysBetween(new DateTime(new Date()), new DateTime(appointment.getStartDateTime())).getDays() == messagingConfig.getDaysBefore() - 1) {
						phone = appointment.getPatient().getAttribute(Context.getAdministrationService().getGlobalProperty("patientengagement.phoneAttribute")).getValue();
						if (phone != null && phone.length() > 0) {
							Patient p = appointment.getPatient();
							String patientName = getPersonName(p);
							Date appointmentDate = appointment.getStartDateTime();
							String messageAfterNameReplace = messagingConfig.getMessageText().replace("patientName", patientName);
							String messageAfterAppointmentDateReplace = messageAfterNameReplace.replace("appointmentDate", dateFormat.format(appointmentDate));
							MessagingUtil.postMessage(phone, messageAfterAppointmentDateReplace);
						}
					}
				}
				
			}
		}
		catch (Exception e) {
			log.error("There was an error sending appointment reminders" + e);
		}
		
	}
	
	public String getPersonName(Person person) {
		String patientName = "";
		if (person.getMiddleName() == null) {
			patientName = person.getFamilyName() + " " + person.getGivenName();
		} else {
			patientName = person.getFamilyName() + " " + person.getMiddleName() + " " + person.getGivenName();
		}
		return patientName;
	}
	
	@Override
	public void sendKenyaBirthdayWishes() throws AuthenticationException, ClientProtocolException, IOException {
		String phoneToSendTo = null;
		try {
			for (Encounter encounter : getRecentEncounterForActivePatientsWithBithDayToday(180)) {
				Person person = encounter.getPatient().getPerson();
				phoneToSendTo = getPreferredPhone(person);
				if (phoneToSendTo != null && phoneToSendTo.length() > 2) {
					String patientName = getPersonName(person);
					String messageAfterNameReplace = Context.getAdministrationService().getGlobalProperty("patientengagement.birthdayWishes").replace("patientName", patientName);
					MessagingUtil.postBirthdayWishes(phoneToSendTo, messageAfterNameReplace);
				}
			}
		}
		catch (Exception e) {
			log.error("There was an error sending birthday wishes" + e);
		}
	}
	
	public String getPreferredPhone(Person person) {
		String phoneToSendTo = null;
		try {
			if (person.getAttribute("patientPhoneNumber") != null) {
				phoneToSendTo = person.getAttribute("patientPhoneNumber").getValue();
			}
			if (phoneToSendTo == null && person.getAttribute("firstNextOfKinPhone") != null) {
				phoneToSendTo = person.getAttribute("firstNextOfKinPhone").getValue();
			}
			if (phoneToSendTo == null && person.getAttribute("secondNextOfKinPhone") != null) {
				phoneToSendTo = person.getAttribute("secondNextOfKinPhone").getValue();
			}
		}
		catch (Exception e) {
			log.error("There was an error geting the patient's phone number" + e);
		}
		return phoneToSendTo;
	}
	
	@Override
	public List<Encounter> getRecentEncounterForActivePatientsWithBithDayToday(int days) {
		return patientEngagementDao.getRecentEncounterForActivePatientsWithBithDayToday(days);
	}
}
