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
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.openmrs.module.afyastat.api.db.InfoDao;
import org.openmrs.module.afyastat.model.Info;
import org.openmrs.module.afyastat.model.handler.InfoHandler;
import org.openmrs.util.HandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class HibernateInfoDao<T extends Info> extends HibernateSingleClassInfoDao<T> implements InfoDao<T> {
	
	@Autowired
	protected SessionFactory sessionFactory;
	
	private final Log log = LogFactory.getLog(HibernateInfoDao.class);
	
	/**
	 * Default constructor.
	 * 
	 * @param mappedClass
	 */
	protected HibernateInfoDao(final Class<T> mappedClass) {
		super(mappedClass);
	}
	
	/**
	 * @return the sessionFactory
	 */
	protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * Return the data with the given id.
	 * 
	 * @param id the data id.
	 * @return the data with the matching id.
	 * @should return data with matching id.
	 * @should return null when no data with matching id.
	 */
	@Override
	public T getData(final Integer id) {
		T data = getById(id);
		if (data != null) {
			List<InfoHandler> handlers = HandlerUtil.getHandlersForType(InfoHandler.class, data.getClass());
			for (InfoHandler handler : handlers) {
				if (handler.accept(data)) {
					handler.handleGet(data);
				}
			}
		}
		
		return data;
	}
	
	/**
	 * Return the data with the given uuid.
	 * 
	 * @param uuid the data uuid.
	 * @return the data with the matching uuid.
	 * @should return data with matching uuid.
	 * @should return null when no data with matching uuid.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getDataByUuid(final String uuid) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.eq("uuid", uuid));
		T data = (T) criteria.uniqueResult();
		if (data != null) {
			List<InfoHandler> handlers = HandlerUtil.getHandlersForType(InfoHandler.class, data.getClass());
			for (InfoHandler handler : handlers) {
				if (handler.accept(data)) {
					handler.handleGet(data);
				}
			}
		}
		return data;
	}
	
	/**
	 * Return the data with the given uuid and dateFormFilled.
	 * 
	 * @param formDataUuid the formDataUuid.
	 * @param dateFormFilled the dateFormFilled.
	 * @param patientUuid the patientUuid.
	 * @return the data with the matching uuid, dateFormFilled and patientUuid.
	 * @should return data with matching uuid,dateFormFilled and patientUuid.
	 * @should return null when no data with matching uuid, dateFormFilled and patientUuid.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T getDataByFormDataUuidDateFormFilledAndPatientUuid(final String formDataUuid, final Long dateFormFilled,
	        String patientUuid) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.eq("formDataUuid", formDataUuid));
		criteria.add(Restrictions.eq("dateFormFilled", dateFormFilled));
		criteria.add(Restrictions.eq("patientUuid", patientUuid));
		T data = (T) criteria.uniqueResult();
		if (data != null) {
			List<InfoHandler> handlers = HandlerUtil.getHandlersForType(InfoHandler.class, data.getClass());
			for (InfoHandler handler : handlers) {
				if (handler.accept(data)) {
					handler.handleGet(data);
				}
			}
		}
		return data;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see InfoDao#getAllDataByFormDataUuid(String)
	 */
	@Override
	public List<T> getAllDataByFormDataUuid(final String formDataUuid) {
		List<T> list = new ArrayList<T>();
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.eq("formDataUuid", formDataUuid));
		return criteria.list();
	}
	
	/**
	 * Return all saved data.
	 * 
	 * @return all saved data.
	 */
	@Override
	public List<T> getAllData() {
		List<T> list = new ArrayList<T>();
		for (T data : getAll()) {
			if (data != null) {
				List<InfoHandler> handlers = HandlerUtil.getHandlersForType(InfoHandler.class, data.getClass());
				for (InfoHandler handler : handlers) {
					if (handler.accept(data)) {
						handler.handleGet(data);
					}
				}
				list.add(data);
			}
		}
		return list;
	}
	
	/**
	 * Save data into the database.
	 * 
	 * @param data the data.
	 * @return saved data.
	 * @should save data into the database.
	 */
	@Override
	@Transactional
	public T saveData(final T data) {
		List<InfoHandler> handlers = HandlerUtil.getHandlersForType(InfoHandler.class, data.getClass());
		for (InfoHandler handler : handlers) {
			if (handler.accept(data)) {
				handler.handleSave(data);
			}
			saveOrUpdate(data);
		}
		return data;
	}
	
	/**
	 * Delete data from the database.
	 * 
	 * @param data the data
	 * @should remove data from the database
	 */
	@Override
	@Transactional
	public void purgeData(final T data) {
		List<InfoHandler> handlers = HandlerUtil.getHandlersForType(InfoHandler.class, data.getClass());
		for (InfoHandler handler : handlers) {
			if (handler.accept(data)) {
				handler.handleDelete(data);
			}
		}
		delete(data);
	}
	
	/**
	 * Get data with matching search term for particular page.
	 * 
	 * @param search the search term.
	 * @param pageNumber the page number.
	 * @param pageSize the size of the page.
	 * @return list of data for the page.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<T> getPagedData(final String search, final Integer pageNumber, final Integer pageSize) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		criteria.createAlias("location", "location", CriteriaSpecification.LEFT_JOIN);
		criteria.createAlias("provider", "provider", CriteriaSpecification.LEFT_JOIN);
		
		if (StringUtils.isNotEmpty(search)) {
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("payload", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("discriminator", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("location.name", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("patientUuid", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("formName", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("provider.identifier", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("provider.name", search, MatchMode.ANYWHERE));
			if (StringUtils.isNumeric(search)) {
				disjunction.add(Restrictions.eq("location.locationId", Integer.parseInt(search)));
			}
			criteria.add(disjunction);
			
		}
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
	 * Get data matching the discriminator
	 * 
	 * @param discriminator the discriminator.
	 * @return list of data.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<T> getDataByDiscriminator(final String discriminator) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.eq("discriminator", discriminator));
		return criteria.list();
	}
	
	/**
	 * Get data matching the list of discriminators
	 * 
	 * @param discriminators the list of discriminators
	 * @return list of data.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<T> getDataByDiscriminator(final List<String> discriminators) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		for (String discriminator : discriminators) {
			criteria.add(Restrictions.eq("discriminator", discriminator));
		}
		return criteria.list();
	}
	
	/**
	 * Get all data except matching the discriminator
	 * 
	 * @param discriminator the discriminator.
	 * @return list of data.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<T> getDataExcludeDiscriminator(final String discriminator) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.ne("discriminator", discriminator));
		return criteria.list();
	}
	
	/**
	 * Get all data except that matching the list of discriminators
	 * 
	 * @param discriminators the list of discriminators
	 * @return list of data.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<T> getDataExcludeDiscriminator(final List<String> discriminators) {
		Criteria criteria = getSessionFactory().getCurrentSession().createCriteria(mappedClass);
		for (String discriminator : discriminators) {
			criteria.add(Restrictions.ne("discriminator", discriminator));
		}
		return criteria.list();
	}
	
	/**
	 * Get the total number of data with matching search term.
	 * 
	 * @param search the search term.
	 * @return total number of data in the database.
	 */
	@Override
	public Number countData(final String search) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		criteria.createAlias("location", "location", CriteriaSpecification.LEFT_JOIN);
		criteria.createAlias("provider", "provider", CriteriaSpecification.LEFT_JOIN);
		
		if (StringUtils.isNotEmpty(search)) {
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("payload", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("discriminator", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("location.name", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("patientUuid", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("formName", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("provider.identifier", search, MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("provider.name", search, MatchMode.ANYWHERE));
			if (StringUtils.isNumeric(search)) {
				disjunction.add(Restrictions.eq("location.locationId", Integer.parseInt(search)));
			}
			criteria.add(disjunction);
		}
		criteria.setProjection(Projections.rowCount());
		return (Number) criteria.uniqueResult();
	}
	
	public void detachDataFromHibernateSession(final T data) {
		getSessionFactory().getCurrentSession().evict(data);
		getSessionFactory().getCurrentSession().flush();
	}
}
