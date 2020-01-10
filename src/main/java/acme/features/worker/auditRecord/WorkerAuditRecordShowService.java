
package acme.features.worker.auditRecord;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.auditRecords.AuditRecord;
import acme.entities.jobs.Status;
import acme.entities.roles.Worker;
import acme.framework.components.Model;
import acme.framework.components.Request;
import acme.framework.services.AbstractShowService;

@Service
public class WorkerAuditRecordShowService implements AbstractShowService<Worker, AuditRecord> {

	@Autowired
	private WorkerAuditRecordRepository repository;


	@Override
	public boolean authorise(final Request<AuditRecord> request) {
		assert request != null;

		int idWorker = request.getPrincipal().getActiveRoleId();
		int auditRecordId = request.getModel().getInteger("id");

		int numberAr = this.repository.findApplicationsOfAJobAppliedByAuditRecordId(idWorker, auditRecordId);

		AuditRecord ar = this.repository.findOneAuditRecordById(auditRecordId);

		Calendar calendar = new GregorianCalendar();
		Date minimumDeadLine = calendar.getTime();

		return numberAr > 0 || ar.getStatus() == Status.PUBLISHED && ar.getJob().getDeadline().after(minimumDeadLine);

	}

	@Override
	public void unbind(final Request<AuditRecord> request, final AuditRecord entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "title", "status", "moment", "body", "auditor.userAccount.username");
	}

	@Override
	public AuditRecord findOne(final Request<AuditRecord> request) {
		assert request != null;
		AuditRecord result;
		int id = request.getModel().getInteger("id");

		result = this.repository.findOneAuditRecordById(id);

		return result;
	}

}
