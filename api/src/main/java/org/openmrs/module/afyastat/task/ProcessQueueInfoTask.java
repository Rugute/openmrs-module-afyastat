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
package org.openmrs.module.afyastat.task;

import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 */
public class ProcessQueueInfoTask extends AbstractTask {
	
	private QueueInfoProcessor processor;
	
	public ProcessQueueInfoTask() {
		this.processor = new QueueInfoProcessor();
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#execute()
	 */
	@Override
	public void execute() {
		Context.openSession();
		processor.processQueueData();
		Context.closeSession();
	}
}
