package de.ufo.cinemasystem.models;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.Id;
import org.springframework.security.access.prepost.PreAuthorize;



@Getter
@Setter
@Entity
@Table(name = "employees")
public class EmployeeEntry {
	private             Money                    salary;
	private             String                   jobMail;
	private             short                    hoursPerWeek;
	private             boolean                  stillAdjusted;
	private             String                   shift;
	private @EmbeddedId UserEntry.UserIdentifier id;



	public EmployeeEntry(UserEntry userEntry, Money salary, String jobMail, short hoursPerWeek)
	{
		this.id            = userEntry.getId();
		this.salary        = salary;
		this.jobMail       = jobMail;
		this.hoursPerWeek  = hoursPerWeek;
		this.stillAdjusted = true;
		this.shift         = "0800 - 1700";
	}

	public EmployeeEntry() {

	}


	@PreAuthorize("BOSS")
	public void setSalary(Money salary) {
		this.salary = salary;
	}



	@PreAuthorize("BOSS")
	public Money getSalary() {
		return salary;
	}
}
