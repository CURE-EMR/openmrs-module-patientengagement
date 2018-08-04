package org.openmrs.module.patientengagement.scheduler.tasks;

import org.openmrs.module.patientengagement.api.PatientEngagementService;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppointmentReminderTask extends AbstractTask {
	
	// Logger 
	private static final Logger log = LoggerFactory.getLogger(AppointmentReminderTask.class);
	
	private PatientEngagementService ps;
	
	@Override
	public void execute() {
		try {
			ps.sendAppointmentReminders();
			
		}
		catch (Exception e) {
			log.error("Failed to send alert notifications", e);
		}
	}
	
}
