/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.afyastat.api.db.hibernate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.openmrs.module.afyastat.api.db.ErrorInfoDao;
import org.openmrs.module.afyastat.model.AuditableInfo;
import org.openmrs.module.afyastat.model.ErrorInfo;

import java.util.List;

/**
 */
public class HibernateErrorInfoDao extends HibernateInfoDao<ErrorInfo> implements ErrorInfoDao {
	
	private final Log log = LogFactory.getLog(HibernateErrorInfoDao.class);
	
	/**
	 * Default constructor.
	 */
	protected HibernateErrorInfoDao() {
		super(ErrorInfo.class);
	}
	
	/**
	 * Get ErrorData with matching search term for particular page.
	 * 
	 * @param search the search term.
	 * @param pageNumber the page number.
	 * @param pageSize the size of the page.
	 * @return list of data for the page.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ErrorInfo> getPagedData(final String search, final Integer pageNumber, final Integer pageSize) {
		Criteria criteria = createCriteria(search);
		if (pageNumber != null) {
			criteria.setFirstResult((pageNumber - 1) * pageSize);
		}
		if (pageSize != null) {
			criteria.setMaxResults(pageSize);
		}
		criteria.addOrder(Order.desc("dateCreated"));
		return criteria.list();
	}
	
	/**
	 * Get the total number of ErrorData with matching search term.
	 * 
	 * @param search the search term.
	 * @return total number of data in the database.
	 */
	@Override
	public Number countData(final String search) {
		Criteria criteria = createCriteria(search);
		criteria.setProjection(Projections.rowCount());
		return (Number) criteria.uniqueResult();
	}
	
	private Criteria createCriteria(String search) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(ErrorInfo.class);
		criteria.createAlias("location", "location", CriteriaSpecification.LEFT_JOIN);
		criteria.createAlias("provider", "provider", CriteriaSpecification.LEFT_JOIN);
		
		if (StringUtils.isNotEmpty(search)) {
			criteria.createAlias("errorMessages", "errorMessages", CriteriaSpecification.LEFT_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("payload", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("discriminator", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("location.name", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("patientUuid", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("formName", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("provider.identifier", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("provider.name", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("errorMessages.message", search, MatchMode.ANYWHERE));
			if (StringUtils.isNumeric(search)) {
				disjunction.add(Restrictions.eq("location.locationId", Integer.parseInt(search)));
			}
			criteria.add(disjunction);
		}
		
		return criteria;
	}
	
	@Override
	public AuditableInfo recordSetPayload(String uuid, String payload) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.eq("uuid", uuid));
		AuditableInfo data = (AuditableInfo) criteria.uniqueResult();
		data.setPayload(payload);
		return (data);
	}
}
