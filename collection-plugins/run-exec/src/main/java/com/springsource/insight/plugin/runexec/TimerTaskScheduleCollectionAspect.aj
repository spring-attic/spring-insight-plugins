/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.runexec;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.aspectj.lang.annotation.SuppressAjWarnings;

/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public aspect TimerTaskScheduleCollectionAspect extends ExecuteMethodCollectionAspect {
    public TimerTaskScheduleCollectionAspect() {
        super();
    }

    /*
     * NOTE: we need to use 'call' since Timer is a core class
     */

    public pointcut delayedSchedule(): call(* Timer.schedule(TimerTask,long));
    public pointcut periodicDelayedSchedule(): call(* Timer.schedule(TimerTask,long,long));

    public pointcut datedSchedule(): call(* Timer.schedule(TimerTask,Date));
    public pointcut periodicDatedSchedule(): call(* Timer.schedule(TimerTask,Date,long));

    public pointcut delayedFixedRateSchedule(): call(* Timer.scheduleAtFixedRate(TimerTask,long,long));
    public pointcut datedFixedRateSchedule(): call(* Timer.scheduleAtFixedRate(TimerTask,Date,long));

    public pointcut collectionPoint()
            : delayedSchedule() || periodicDelayedSchedule()
            || datedSchedule() || periodicDatedSchedule()
            || delayedFixedRateSchedule() || datedFixedRateSchedule()
            ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around(TimerTask task, long delayTime)
            : delayedSchedule() && args(task, delayTime) {
        TimerTask effectiveTask = resolveTimerTask(task, thisJoinPointStaticPart);
        return proceed(effectiveTask, delayTime);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around(TimerTask task, long delayTime, long period)
            : periodicDelayedSchedule() && args(task, delayTime, period) {
        TimerTask effectiveTask = resolveTimerTask(task, thisJoinPointStaticPart);
        return proceed(effectiveTask, delayTime, period);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around(TimerTask task, Date startTime)
            : datedSchedule() && args(task, startTime) {
        TimerTask effectiveTask = resolveTimerTask(task, thisJoinPointStaticPart);
        return proceed(effectiveTask, startTime);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around(TimerTask task, Date startTime, long period)
            : periodicDatedSchedule() && args(task, startTime, period) {
        TimerTask effectiveTask = resolveTimerTask(task, thisJoinPointStaticPart);
        return proceed(effectiveTask, startTime, period);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around(TimerTask task, long delayTime, long period)
            : delayedFixedRateSchedule() && args(task, delayTime, period) {
        TimerTask effectiveTask = resolveTimerTask(task, thisJoinPointStaticPart);
        return proceed(effectiveTask, delayTime, period);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around(TimerTask task, Date startTime, long period)
            : datedFixedRateSchedule() && args(task, startTime, period) {
        TimerTask effectiveTask = resolveTimerTask(task, thisJoinPointStaticPart);
        return proceed(effectiveTask, startTime, period);
    }
}
