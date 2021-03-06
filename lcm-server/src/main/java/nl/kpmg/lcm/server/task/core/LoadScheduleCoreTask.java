/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package nl.kpmg.lcm.server.task.core;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import nl.kpmg.lcm.server.data.TaskSchedule;
import nl.kpmg.lcm.server.data.TaskSchedule.TaskScheduleItem;
import nl.kpmg.lcm.server.data.dao.TaskScheduleDao;
import nl.kpmg.lcm.server.task.CoreTask;
import nl.kpmg.lcm.server.task.EnrichmentTask;
import nl.kpmg.lcm.server.task.TaskException;
import nl.kpmg.lcm.server.task.TaskResult;
import nl.kpmg.lcm.server.task.TaskScheduleException;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
/**
 * TaskSchedule update task. This task will try and install all task items in the most current
 * TaskSchedule. The current implementation doesn't do this with much intelligence. A backoff should
 * be implemented so that a new schedule will only sparsely overwrite the previous schedule.
 *
 * @author mhoekstra
 */
public class LoadScheduleCoreTask extends CoreTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadScheduleCoreTask.class.getName());
  /**
   * The group key which is used to register the scheduled tasks.
   */
  private static final String GROUP_KEY = "scheduled";

  /**
   * The TaskScheduleDao.
   */
  @Autowired
  private TaskScheduleDao taskScheduleDao;

  /**
   * The currently active TaskSchedule.
   */
  private TaskSchedule current;

  /**
   * Installs the current TaskSchedule.
   *
   * @return the result of the task
   * @throws TaskException if the task fails
   */
  @Override
  public final TaskResult execute() throws TaskException {
    TaskSchedule latest = taskScheduleDao.findFirstByOrderByIdDesc();
    if (current == null || !current.equals(latest)) {
      try {
        removeTasks();
      } catch (SchedulerException ex) {
        LOGGER.error("couldn't remove the previous schedule.", ex);
        return TaskResult.FAILURE;
      }

      if (latest != null && latest.getItems() != null) {
        latest.getItems().stream().forEach((TaskScheduleItem taskScheduleItem) -> {
          try {
            scheduleEnrichmentTask(taskScheduleItem.getName(), taskScheduleItem.getJob(),
                taskScheduleItem.getTarget(), taskScheduleItem.getCron());
          } catch (TaskScheduleException ex) {
            LOGGER.warn( "Failed to schedule ", ex);
          }
        });
      }

      current = latest;
    }
    return TaskResult.SUCCESS;
  }

  /**
   * Schedules an EnrichmentTask based on its cron definition.
   *
   * @param name The name of the job
   * @param job the class of the job to execute
   * @param target the target MetaData expression
   * @param cron the execution schedule
   * @throws TaskScheduleException when the task couldn't be scheduled
   */
  private void scheduleEnrichmentTask(final String name, final String job, final String target,
      final String cron) throws TaskScheduleException {

    // Assertions until we have input validation on the database objects
    if (job == null) {
      throw new TaskScheduleException(
          String.format("The job for task with name '%s', target '%s', and cron '%s' is empty",
              name, target, cron));
    }

    try {
      Class<?> cls = Class.forName(job);
      if (EnrichmentTask.class.isAssignableFrom(cls)) {
        scheduleEnrichmentTask(name, (Class<EnrichmentTask>) cls, target, cron);
      } else {
        throw new TaskScheduleException("Task definition doesn't contain a schedulable job");
      }
    } catch (ClassNotFoundException ex) {
      LOGGER.error( null, ex);
      throw new TaskScheduleException(ex);
    }
  }

  /**
   * Schedules an EnrichmentTask based on its cron definition.
   *
   * @param name The name of the job
   * @param job the class of the job to execute
   * @param target the target MetaData expression
   * @param cron the execution schedule
   * @throws TaskScheduleException when the task couldn't be scheduled
   */
  private void scheduleEnrichmentTask(final String name, final Class<? extends EnrichmentTask> job,
      final String target, final String cron) throws TaskScheduleException {

    // Assertions until we have input validation on the database objects
    if (name == null || job == null || target == null || cron == null) {
      throw new TaskScheduleException(String.format(
          "The job %s for task with name '%s', target '%s', and cron '%s' can't be scheduled "
              + "due to missing values",
          job, name, target, cron));
    }

    try {
      JobDetail jobDetail = newJob(job).withIdentity(name, GROUP_KEY).build();

      jobDetail.getJobDataMap().put(EnrichmentTask.TARGET_KEY, target);
      jobDetail.getJobDataMap().put(EnrichmentTask.TASK_ID_KEY, name);

      CronTrigger trigger =
          newTrigger().withIdentity(name, GROUP_KEY).withSchedule(cronSchedule(cron)).build();

      Scheduler scheduler = getScheduler();
      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException ex) {
      LOGGER.error( null, ex);
      throw new TaskScheduleException(ex);
    }
  }

  /**
   * Remove all the scheduled task.
   *
   * @throws SchedulerException if a job can't be removed
   */
  private void removeTasks() throws SchedulerException {
    Scheduler scheduler = getScheduler();
    Set<JobKey> jobKeys =
        scheduler.getJobKeys((GroupMatcher<JobKey>) GroupMatcher.jobGroupEquals(GROUP_KEY));

    jobKeys.stream().forEach((jobKey) -> {
      try {
        scheduler.deleteJob(jobKey);
      } catch (SchedulerException ex) {
        LOGGER.error("failed removing task", ex);
      }
    });
  }
}
