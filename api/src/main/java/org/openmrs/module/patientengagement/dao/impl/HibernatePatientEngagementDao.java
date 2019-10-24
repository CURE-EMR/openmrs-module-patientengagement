/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientengagement.dao.impl;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.module.operationtheater.api.model.SurgicalAppointment;
import org.openmrs.module.patientengagement.dao.PatientEngagementDao;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 * 
 * @author Bailly RURANGIRWA
 */
public class HibernatePatientEngagementDao implements PatientEngagementDao {
	
	/* Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@SuppressWarnings("unchecked")
	public List<Encounter> getRecentEncounterForActivePatientsWithBithDayToday(int days) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String today = dateFormat.format(new Date());
		String birthdayMonthAndDay = today.substring(today.indexOf("-") + 1);
		
		String sql = "select * from encounter e, person p where e.patient_id = p.person_id and p.birthdate like '%" + birthdayMonthAndDay + "' and DATEDIFF(NOW(), e.date_created) < " + days + " group by patient_id";
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		query.addEntity(Encounter.class);
		List<Encounter> results = query.list();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public List<SurgicalAppointment> getSurgicalAppointmentWithNoAttributes() {
		String sql = "select * from surgical_appointment where surgical_appointment_id not in (select surgical_appointment_id from surgical_appointment_attribute where surgical_appointment_attribute_type_id in (13,14))";
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		query.addEntity(SurgicalAppointment.class);
		List<SurgicalAppointment> results = query.list();
		return results;
	}
	
	public void createAttribute(SurgicalAppointment surgicalAppointment, int surgicalAppointmentAttributeType) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String value = "";
		if (surgicalAppointmentAttributeType == 13) {
			value = surgicalAppointment.getPatient().getPerson().getGender();
		}
		
		if (surgicalAppointmentAttributeType == 14) {
			value = dateFormat.format(surgicalAppointment.getPatient().getPerson().getBirthdate());
		}
		
		String sql = "insert into surgical_appointment_attribute (surgical_appointment_id, surgical_appointment_attribute_type_id, value, creator, date_created, uuid) values (" + surgicalAppointment.getId() + ", " + surgicalAppointmentAttributeType + ",'" + value + "', 4, NOW(), UUID())";
		SQLQuery query = getCurrentSession().createSQLQuery(sql);
		query.executeUpdate();
	}
	
	private org.hibernate.Session getCurrentSession() {
		try {
			return sessionFactory.getCurrentSession();
		}
		catch (NoSuchMethodError ex) {
			try {
				Method method = sessionFactory.getClass().getMethod("getCurrentSession", null);
				return (org.hibernate.Session) method.invoke(sessionFactory, null);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to get the current hibernate session", e);
			}
		}
	}
}
