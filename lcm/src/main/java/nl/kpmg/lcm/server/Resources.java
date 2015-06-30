/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.server;

import nl.kpmg.lcm.server.metadata.storage.MetaDataDao;
import nl.kpmg.lcm.server.metadata.storage.TaskDescriptionDao;
import nl.kpmg.lcm.server.metadata.storage.TaskScheduleDao;

/**
 * Static resources class for centralizing dependencies.
 *
 * There is a need to have centralized dependencies for some DAO components.
 * Normally the choice here would be Spring Dependency injection. However this
 * is heavily bean dependent which is not really easily configurable from the
 * outside AFAIK. However if this is suitable do replace this!
 */
public final class Resources {

    /**
     * The DAO implementation.
     */
    private static MetaDataDao metaDataDao;

    /**
     * The DAO implementation.
     */
    private static TaskDescriptionDao taskDao;

    /**
     * The DAO implementation.
     */
    private static TaskScheduleDao taskScheduleDao;

    private static String baseUri;

    /**
     * Private constructor to make this class pure static.
     */
    private Resources() { }

    /**
     * @return the metaData DAO implementation
     */
    public static MetaDataDao getMetaDataDao() {
        return metaDataDao;
    }

    /**
     * @param metaDataDao the metaData DAO implementation to use
     */
    public static void setMetaDataDao(final MetaDataDao metaDataDao) {
        Resources.metaDataDao = metaDataDao;
    }

    public static TaskDescriptionDao getTaskDao() {
        return taskDao;
    }

    public static void setTaskDao(TaskDescriptionDao taskDao) {
        Resources.taskDao = taskDao;
    }

    public static TaskScheduleDao getTaskScheduleDao() {
        return taskScheduleDao;
    }

    public static void setTaskScheduleDao(TaskScheduleDao taskScheduleDao) {
        Resources.taskScheduleDao = taskScheduleDao;
    }

    public static String getBaseUri() {
        return baseUri;
    }

    public static void setBaseUri(final String baseUri) {
        Resources.baseUri = baseUri;
    }
}
