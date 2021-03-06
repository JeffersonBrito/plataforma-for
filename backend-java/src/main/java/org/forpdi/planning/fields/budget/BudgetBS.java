package org.forpdi.planning.fields.budget;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import org.forpdi.core.company.Company;
import org.forpdi.planning.structure.StructureLevelInstance;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import br.com.caelum.vraptor.boilerplate.HibernateBusiness;
import br.com.caelum.vraptor.boilerplate.bean.PaginatedList;
import br.com.caelum.vraptor.boilerplate.util.GeneralUtils;

@RequestScoped
public class BudgetBS extends HibernateBusiness {

	/**
	 * Salva um elemento orçamentário.
	 * 
	 * @param budget
	 *            Elemento orçamentario a ser salvo.
	 * @return void.
	 */
	public void saveBudgetElement(BudgetElement budgetElement) {
		budgetElement.setDeleted(false);
		this.persist(budgetElement);
	}

	/**
	 * Retornar lista de elementos orçamentários.
	 * 
	 * @return BudgetSimulationDB Lista de ação orçamentária.
	 */
	public PaginatedList<BudgetElement> listBudgetElement(Company company) {
		PaginatedList<BudgetElement> list = new PaginatedList<BudgetElement>();
		Criteria criteria = this.dao.newCriteria(BudgetElement.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("company", company));

		list.setList(this.dao.findByCriteria(criteria, BudgetElement.class));

		return list;
	}

	public List<BudgetElement> listAllBudgetElementsByCompany(Company company) {
		Criteria criteria = this.dao.newCriteria(BudgetElement.class)
			.add(Restrictions.eq("company", company));
		return this.dao.findByCriteria(criteria, BudgetElement.class);
	}

	public List<Budget> listAllBudgetsByElementsAndLevelInstances(
			List<BudgetElement> budgetElements, List<StructureLevelInstance> structureLevelInstances) {
		if (GeneralUtils.isEmpty(budgetElements) && GeneralUtils.isEmpty(structureLevelInstances)) {
			return Collections.emptyList();
		}
		Criteria criteria = this.dao.newCriteria(Budget.class);
		if (!GeneralUtils.isEmpty(budgetElements))
			criteria.add(Restrictions.in("budgetElement", budgetElements));
		if (!GeneralUtils.isEmpty(structureLevelInstances))
			criteria.add(Restrictions.in("levelInstance", structureLevelInstances));
		return this.dao.findByCriteria(criteria, Budget.class);
	}


	/**
	 * Buscar Elemento Orçamentário.
	 * 
	 * @param id
	 *            Id do elemento orçamentário.
	 * @return Elemento orçamentário.
	 */
	public BudgetElement budgetElementExistsById(Long id) {
		Criteria criteria = this.dao.newCriteria(BudgetElement.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("id", id));

		return (BudgetElement) criteria.uniqueResult();
	}

	public BudgetElement budgetElementExistsBySubActionAndCompany(String subAction, Company company) {
		Criteria criteria = this.dao.newCriteria(BudgetElement.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("subAction", subAction).ignoreCase()).add(Restrictions.eq("company", company))
				.setMaxResults(1);

		return (BudgetElement) criteria.uniqueResult();
	}

	/**
	 * Update no elemento orçamentário.
	 * 
	 * @param budgetElement
	 *            Elemento orçamentário para realizar o update.
	 * @return void.
	 */
	public void update(BudgetElement budgetElement) {
		budgetElement.setDeleted(false);
		this.persist(budgetElement);
	}

	/**
	 * Deletar elemento orçamentário.
	 * 
	 * @param budget
	 *            Orçamento para deletar.
	 * @return void.
	 */
	public void deleteBudget(BudgetElement budgetElement) {
		budgetElement.setDeleted(true);
		this.persist(budgetElement);
	}

	/**
	 * Buscar lista de Bugtes.
	 * 
	 * @param BudgetElement
	 *            Elemento orçamentario.
	 * @return PaginatedList<Budget> Lista de orçamentos.
	 */
	public PaginatedList<Budget> listBudgetsByBudgetElement(BudgetElement budgetElement) {
		PaginatedList<Budget> list = new PaginatedList<Budget>();
		Criteria criteria = this.dao.newCriteria(Budget.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("budgetElement", budgetElement));
		list.setList(this.dao.findByCriteria(criteria, Budget.class));

		return list;
	}
	
	/**
	 * Buscar lista de Bugtes.
	 * 
	 * @param StructureLevelInstance
	 *            structure instance level
	 * @return List<Budget> Lista de orçamentos.
	 */
	public List<Budget> listBudgetByLevelInstance(StructureLevelInstance level){		
		Criteria criteria = this.dao.newCriteria(Budget.class).add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("levelInstance", level));
		List<Budget> list = this.dao.findByCriteria(criteria, Budget.class);
		return list;
	}
	
	/**
	 * Buscar lista de Bugtes. Inclusive deletados.
	 * 
	 * @param StructureLevelInstance
	 *            structure instance level
	 * @return List<Budget> Lista de orçamentos.
	 */
	public List<Budget> listAllBudgetByLevelInstance(StructureLevelInstance level){		
		Criteria criteria = this.dao.newCriteria(Budget.class).add(Restrictions.eq("levelInstance", level));
		List<Budget> list = this.dao.findByCriteria(criteria, Budget.class);
		return list;
	}

}