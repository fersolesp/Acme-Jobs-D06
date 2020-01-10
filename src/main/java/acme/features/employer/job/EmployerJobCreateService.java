
package acme.features.employer.job;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.descriptors.Descriptor;
import acme.entities.jobs.Job;
import acme.entities.jobs.Status;
import acme.entities.roles.Employer;
import acme.framework.components.Errors;
import acme.framework.components.Model;
import acme.framework.components.Request;
import acme.framework.services.AbstractCreateService;

@Service
public class EmployerJobCreateService implements AbstractCreateService<Employer, Job> {

	// Internal state --------------------------------------

	@Autowired
	EmployerJobRepository repository;


	// AbstractCreateService<Employer, Job> interface --------

	@Override
	public boolean authorise(final Request<Job> request) {
		assert request != null;
		return true;
	}

	@Override
	public void bind(final Request<Job> request, final Job entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors);
	}

	@Override
	public void unbind(final Request<Job> request, final Job entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "reference", "status", "title");
		request.unbind(entity, model, "deadline", "salary", "moreInfo", "descriptor", "descriptor.description");
	}

	@Override
	public Job instantiate(final Request<Job> request) {
		assert request != null;

		Job result = new Job();
		int employerId = request.getPrincipal().getActiveRoleId();
		result.setEmployer(this.repository.findEmployerById(employerId));
		result.setStatus(Status.DRAFT);

		return result;
	}

	@Override
	public void validate(final Request<Job> request, final Job entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		boolean isUnique, isEuro;
		Date minimumDeadLine;
		Calendar calendar;

		//--------REFERENCE--------------

		if (!errors.hasErrors("reference")) {
			isUnique = this.repository.findOneJobByReference(entity.getReference()) != null;
			errors.state(request, !isUnique, "reference", "employer.job.error.label.unique");
		}

		//---------DEADLINE---------------

		if (!errors.hasErrors("deadline")) {
			calendar = new GregorianCalendar();
			minimumDeadLine = calendar.getTime();
			errors.state(request, entity.getDeadline().after(minimumDeadLine), "deadline", "employer.job.error.label.deadline");
		}

		//---------SALARY---------------

		if (!errors.hasErrors("salary")) {
			isEuro = entity.getSalary().getCurrency().equals("EUR") || entity.getSalary().getCurrency().equals("€");
			errors.state(request, isEuro, "salary", "employer.job.error.label.salary-currency");

		}
	}

	@Override
	public void create(final Request<Job> request, final Job entity) {
		assert request != null;
		assert entity != null;

		Descriptor descriptor = new Descriptor();
		descriptor.setDescription(request.getModel().getString("descriptor.description"));
		entity.setDescriptor(descriptor);

		this.repository.save(entity);
	}
}
