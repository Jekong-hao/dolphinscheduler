/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskRecallAckCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.cache.ResponseCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

@Component
public class TaskRecallAckProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskRecallAckProcessor.class);

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_RECALL_ACK == command.getType(),
            String.format("invalid command type : %s", command.getType()));

        TaskRecallAckCommand taskRecallAckCommand = JSONUtils.parseObject(command.getBody(), TaskRecallAckCommand.class);
        if (taskRecallAckCommand == null) {
            return;
        }

        if (taskRecallAckCommand.getStatus() ==  ExecutionStatus.SUCCESS.getCode()) {
            ResponseCache.get().removeRecallCache(taskRecallAckCommand.getTaskInstanceId());
            logger.debug("removeRecallCache: task instance id:{}", taskRecallAckCommand.getTaskInstanceId());
            TaskCallbackService.remove(taskRecallAckCommand.getTaskInstanceId());
            logger.debug("remove REMOTE_CHANNELS, task instance id:{}", taskRecallAckCommand.getTaskInstanceId());
        }
    }
}
