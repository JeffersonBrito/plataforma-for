package org.forrisco.core.plan;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;

import org.forpdi.core.company.Company;
import org.forrisco.core.bean.ItemSearchBean;
import org.forrisco.core.item.PlanRiskItem;
import org.forrisco.core.item.PlanRiskItemField;
import org.forrisco.core.item.PlanRiskSubItem;
import org.forrisco.core.item.PlanRiskSubItemField;
import org.forrisco.core.policy.Policy;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.portugueseStemmer;

import br.com.caelum.vraptor.boilerplate.HibernateBusiness;
import br.com.caelum.vraptor.boilerplate.bean.PaginatedList;

@RequestScoped
public class PlanRiskBS extends HibernateBusiness {
	
	/**
	 * Salva no banco de dados um novo item
	 * 
	 * @param PlanRisk,
	 *            instância do item a ser salvo
	 */
	public void save(PlanRisk planRisk) {
		planRisk.setId(null);
		planRisk.setDeleted(false);
		this.persist(planRisk);
	}
	
	/**
	 * Lista os planos de risco
	 * 
	 * @param Company
	 * @param Integer
	 * 
	 * @return
	 */
	public PaginatedList<PlanRisk> listPlanRisk(Company company, Integer page) {
		
		PaginatedList<PlanRisk> results = new PaginatedList<PlanRisk>();
		
		Criteria criteria = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("policy", "policy")
				.add(Restrictions.eq("policy.company", company))
				.addOrder(Order.asc("id"));						
		
		Criteria count = this.dao.newCriteria(PlanRisk.class)
				.add(Restrictions.eq("deleted", false))
				.createAlias("policy", "policy")
				.add(Restrictions.eq("policy.company", company))
				.setProjection(Projections.countDistinct("id"));

		results.setList(this.dao.findByCriteria(criteria, PlanRisk.class));
		results.setTotal((Long) count.uniqueResult());
		return results;
	}
	
	
	/**
	 * 
	 * @param plan
	 * @return
	 */
	public Policy listPolicybyPlanRisk (PlanRisk plan) {
			
		Criteria criteria = this.dao.newCriteria(Policy.class)
				.add(Restrictions.eq("deleted", false))								
				.add(Restrictions.eq("id",plan.getPolicy().getId()));		
		
		Policy result = (Policy) criteria.uniqueResult();
		return result;
	}

	public void delete(PlanRisk planRisk) {
		planRisk.setDeleted(true);
		this.persist(planRisk);
	}
	
	/**
	 * @param planRisk
	 * @param terms
	 * @param itensSelect
	 * @return boolean flag informando se algum campo foi encontrado nas informações gerais
	 */
	public boolean termsSearchedInGeneralInformation(PlanRisk planRisk, String terms, Long[] itensSelect) {
		SnowballStemmer snowballStemmer = new portugueseStemmer();
		snowballStemmer.setCurrent(terms.toLowerCase());
		snowballStemmer.stem();
		String stemTerms = snowballStemmer.getCurrent();
		if (planRisk.getName().toLowerCase().contains(stemTerms) || planRisk.getDescription().toLowerCase().contains(stemTerms)) {
			return true;
		}
		return false;
	}

	/**
	 * Listar os itens/subitens pelo termo da busca.
	 * 
	 * @param policy
	 *           	Política.
	 * @param terms
	 *            Termo da busca.
	 * @param itensSelect
	 *            Lista de itens.
	 * @param ordResult
	 *            Ordem.
	 * @return results Lista de instâncias dos itens/subitens.
	 */
	public List<PlanRiskItem> listItemTerms(PlanRisk planRisk, String terms, Long[] itensSelect, int ordResult) {
		if (terms == null || terms.isEmpty()) {
			return new ArrayList<PlanRiskItem>();
		}
		if(itensSelect != null && itensSelect.length == 0) {
			return new ArrayList<PlanRiskItem>();
		}
		
		SnowballStemmer snowballStemmer = new portugueseStemmer();
		snowballStemmer.setCurrent(terms);
		snowballStemmer.stem();
		String stemTerms = snowballStemmer.getCurrent();
		
		Criterion name = Restrictions.like("name", "%" + stemTerms + "%").ignoreCase();
		Criterion description = Restrictions.like("description", "%" + stemTerms + "%").ignoreCase();
		LogicalExpression orExp = Restrictions.or(name, description);
		
		Criteria criteria = this.dao.newCriteria(PlanRiskItem.class)
				.add(Restrictions.eq("planRisk", planRisk))
				.add(Restrictions.eq("deleted", false));	
		
		if (itensSelect != null) {
			Disjunction or = Restrictions.disjunction();
			for (int i = 0; i < itensSelect.length; i++) {
				or.add(Restrictions.eq("id", itensSelect[i]));
			}
			criteria.add(or);
		}

		Map<Long, PlanRiskItem> itens = new HashMap<Long,PlanRiskItem>();
		List<PlanRiskItem> list = this.dao.findByCriteria(criteria, PlanRiskItem.class);

		for(int i=0; i < list.size(); i++) {
			Criteria crit = this.dao.newCriteria(PlanRiskItemField.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRiskItem", list.get(i)))
		 		.add(orExp);
			List<PlanRiskItemField> fields = this.dao.findByCriteria(crit, PlanRiskItemField.class);

			for(int j=0; j < fields.size(); j++) {
				fields.get(j).getPlanRiskItem().setDescription(fields.get(j).getName());
				itens.put(fields.get(j).getPlanRiskItem().getId(),fields.get(j).getPlanRiskItem());
			}
		}

		criteria.add(orExp);
		list = this.dao.findByCriteria(criteria, PlanRiskItem.class);
		
		for(int i=0; i< list.size(); i++) {
			itens.put(list.get(i).getId(),list.get(i));
		}
		
		return new ArrayList<PlanRiskItem>(itens.values());
	}
	
	public List<PlanRiskSubItem> listSubitemTerms(PlanRisk planRisk, String terms, Long[] subitensSelect, int ordResult) {
		if (terms == null || terms.isEmpty()) {
			return new ArrayList<PlanRiskSubItem>();
		}
		if(subitensSelect != null && subitensSelect.length == 0) {
			return new ArrayList<PlanRiskSubItem>();
		}
		
		SnowballStemmer snowballStemmer = new portugueseStemmer();
		snowballStemmer.setCurrent(terms);
		snowballStemmer.stem();
		String stemTerms = snowballStemmer.getCurrent();

		Criterion name = Restrictions.like("name", "%" + stemTerms + "%").ignoreCase();
		Criterion description = Restrictions.like("description", "%" + stemTerms + "%").ignoreCase();
		LogicalExpression orExp = Restrictions.or(name,description);
		
		Criteria criteria = this.dao.newCriteria(PlanRiskSubItem.class)
				.createAlias("planRiskItem", "planRiskItem", JoinType.INNER_JOIN)
				.add(Restrictions.eq("planRiskItem.planRisk", planRisk))
				.add(Restrictions.eq("deleted", false));
		
		if (subitensSelect != null) {
			Disjunction or = Restrictions.disjunction();
			for (int i = 0; i < subitensSelect.length; i++) {
				or.add(Restrictions.eq("id", subitensSelect[i]));
			}
			criteria.add(or);
		}

		Map<Long, PlanRiskSubItem> subitens = new HashMap<Long, PlanRiskSubItem>();
		List<PlanRiskSubItem> list = this.dao.findByCriteria(criteria, PlanRiskSubItem.class);
		
		for(int i=0; i < list.size(); i++) {
			Criteria crit = this.dao.newCriteria(PlanRiskSubItemField.class)
				.add(Restrictions.eq("deleted", false))
				.add(Restrictions.eq("planRiskSubItem", list.get(i)))
		 		.add(orExp);
			List<PlanRiskSubItemField> fields = this.dao.findByCriteria(crit, PlanRiskSubItemField.class);
		
			for(int j=0; j < fields.size(); j++) {
				list.get(j).setItemId(list.get(j).getPlanRiskItem().getId());
				subitens.put(fields.get(j).getPlanRiskSubItem().getId(),fields.get(j).getPlanRiskSubItem());
			}
		}
		
		criteria.add(orExp);
		list = this.dao.findByCriteria(criteria, PlanRiskSubItem.class);
		
		for(int i=0; i< list.size(); i++) {
			list.get(i).setItemId(list.get(i).getPlanRiskItem().getId());
			subitens.put(list.get(i).getId(),list.get(i));
		}
			
		return new ArrayList<PlanRiskSubItem>(subitens.values());
	}

	public PaginatedList<ItemSearchBean> termResult(boolean includeGeneralInformation, PlanRisk planRisk, List<PlanRiskItem> itens,
			List<PlanRiskSubItem> subitens,  Integer page,  Long limit){
		int firstResult = 0;
		int maxResult;
		int count = 0;
		if (limit != null) {
			firstResult = (int) ((page - 1) * limit);
			maxResult = limit.intValue();
		} else {
			maxResult = Integer.MAX_VALUE;
		}
		
		List<ItemSearchBean> list = new ArrayList<>();
		// adiciona informacoes gerais
		if (includeGeneralInformation) {
			ItemSearchBean bean = new ItemSearchBean();
			bean.setName(planRisk.getName());
			bean.setDescription(planRisk.getDescription());
			bean.setLevel("Informações gerais");
			list.add(bean);
			count++;
		}
		// adiciona itens 
		for(PlanRiskItem item : itens) {
			ItemSearchBean bean = new ItemSearchBean();
			bean.setId(item.getId());
			bean.setName(item.getName());
			bean.setDescription(item.getDescription());
			bean.setLevel("Item");
			if (limit != null) {
				if (count >= firstResult && list.size() < maxResult) {
					list.add(bean);
					count++;
				} else if (list.size() >= maxResult) {
					break;
				} else {
					count++;
				}
			} else {
				list.add(bean);
			}
		}
		// adiciona subitens
		for(PlanRiskSubItem subitem : subitens) {
			ItemSearchBean bean = new ItemSearchBean();
			bean.setId(subitem.getId());
			bean.setParentId(subitem.getPlanRiskItem().getId());
			bean.setName(subitem.getName());
			bean.setDescription(subitem.getDescription());
			bean.setLevel("Subitem");
			if (limit != null) {
				if (count >= firstResult && list.size() < maxResult) {
					list.add(bean);
					count++;
				} else if (list.size() >= maxResult) {
					break;
				} else {
					count++;
				}
			} else {
				list.add(bean);
			}
		}

		PaginatedList<ItemSearchBean> result = new PaginatedList<ItemSearchBean>();
		
		result.setList(list);
		result.setTotal((long)list.size());
		return result;
	}
}
