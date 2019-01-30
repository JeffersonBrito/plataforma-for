package org.forrisco.core.plan;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.forrisco.core.policy.Policy;

import br.com.caelum.vraptor.boilerplate.SimpleLogicalDeletableEntity;
import br.com.caelum.vraptor.serialization.SkipSerialization;

/**
 * @author Matheus Nascimento
 * 
 */
@Entity(name = PlanRisk.TABLE)
@Table(name = PlanRisk.TABLE)

public class PlanRisk extends SimpleLogicalDeletableEntity {
	public static final String TABLE = "frisco_plan_risk";
	private static final long serialVersionUID = 1L;

	@Column(nullable = false, length=255)
	private String name;

	@Column(nullable = true, length=10000)
	private String description;
	
	//@SkipSerialization
	@ManyToOne(targetEntity=Policy.class, optional=false,  fetch=FetchType.EAGER)
	private Policy policy;

	private boolean archived = false;
	
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

}