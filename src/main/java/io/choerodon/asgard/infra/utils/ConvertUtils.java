package io.choerodon.asgard.infra.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.modelmapper.ModelMapper;

import io.choerodon.asgard.api.dto.JsonMergeDTO;
import io.choerodon.asgard.domain.*;
import io.choerodon.asgard.infra.mapper.JsonDataMapper;
import io.choerodon.asgard.property.PropertyJobTask;
import io.choerodon.asgard.property.PropertySaga;
import io.choerodon.asgard.property.PropertySagaTask;
import io.choerodon.asgard.property.PropertyTimedTask;
import io.choerodon.asgard.schedule.QuartzDefinition;
import io.choerodon.core.exception.CommonException;

public class ConvertUtils {

    private ConvertUtils() {
    }

    public static QuartzMethod convertQuartzMethod(final ObjectMapper mapper, final PropertyJobTask jobTask, final String service) {
        final QuartzMethod method = new QuartzMethod();
        method.setService(service);
        method.setCode(jobTask.getCode());
        method.setDescription(jobTask.getDescription());
        method.setMaxRetryCount(jobTask.getMaxRetryCount());
        method.setMethod(jobTask.getMethod());
        method.setLevel(jobTask.getLevel());
        try {
            String params = mapper.writeValueAsString(jobTask.getParams());
            method.setParams(params);
            return method;
        } catch (JsonProcessingException e) {
            throw new CommonException("error.ConvertUtils.convertQuartzMethod", e);
        }
    }

    public static QuartzTask convertQuartzTask(final ObjectMapper mapper, final PropertyTimedTask timedTask) {
        final QuartzTask task = new QuartzTask();
        task.setId(null);
        task.setName(timedTask.getName());
        task.setDescription(timedTask.getDescription());
        task.setExecuteMethod(timedTask.getMethodCode());
        task.setTriggerType("simple-trigger");
        task.setStartTime(new Date());
        task.setSimpleRepeatCount(timedTask.getRepeatCount());
        task.setSimpleRepeatInterval(timedTask.getRepeatInterval());
        task.setSimpleRepeatIntervalUnit(timedTask.getRepeatIntervalUnit());
        task.setStatus(QuartzDefinition.TaskStatus.ENABLE.name());
        //所有自定义定时任务皆为简单任务，此处借用此闲置属性放置是否是一次执行
        if (timedTask.getOneExecution()) {
            task.setCronExpression("1");
        } else {
            task.setCronExpression("0");
        }
        try {
            String params = mapper.writeValueAsString(timedTask.getParams());
            task.setExecuteParams(params);
            return task;
        } catch (JsonProcessingException e) {
            throw new CommonException("error.ConvertUtils.convertQuartzTask", e);
        }
    }

    public static Saga convertSaga(final ModelMapper mapper, final PropertySaga saga, final String service) {
        Saga sagaDO = mapper.map(saga, Saga.class);
        sagaDO.setService(service);
        return sagaDO;
    }

    public static SagaTask convertSagaTask(final ModelMapper mapper, final PropertySagaTask sagaTask, final String service) {
        SagaTask sagaTaskDO = mapper.map(sagaTask, SagaTask.class);
        sagaTaskDO.setService(service);
        return sagaTaskDO;
    }


    public static List<JsonMergeDTO> convertToJsonMerge(final List<SagaTaskInstance> seqTaskInstances, final JsonDataMapper jsonDataMapper) {
        List<JsonMergeDTO> list = new ArrayList<>(seqTaskInstances.size());
        for (SagaTaskInstance sagaTaskInstance : seqTaskInstances) {
            if (sagaTaskInstance.getOutputDataId() == null) {
                continue;
            }
            JsonData jsonData = jsonDataMapper.selectByPrimaryKey(sagaTaskInstance.getOutputDataId());
            if (jsonData != null && jsonData.getData() != null) {
                list.add(new JsonMergeDTO(sagaTaskInstance.getTaskCode(), jsonData.getData()));
            }
        }
        return list;
    }

    public static String jsonMerge(final List<JsonMergeDTO> mergeDTOS, final ObjectMapper objectMapper) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        if (mergeDTOS.isEmpty()) {
            return root.toString();
        }
        //元素都相同则直接返回任意一个
        if (isAllTheSame(mergeDTOS)) {
            return mergeDTOS.get(0).getTaskOutputJsonData();
        }
        for (JsonMergeDTO dto : mergeDTOS) {
            JsonNode jsonNode = objectMapper.readTree(dto.getTaskOutputJsonData());
            if (jsonNode instanceof ObjectNode) {
                root.setAll((ObjectNode) jsonNode);
            } else if (jsonNode instanceof ArrayNode) {
                root.putArray(dto.getTaskCode()).addAll((ArrayNode) jsonNode);
            } else if (jsonNode instanceof ValueNode) {
                root.set(dto.getTaskCode(), jsonNode);
            }
        }
        return root.toString();
    }

    /**
     * 判断所有元素是否相同
     */
    private static boolean isAllTheSame(final List<JsonMergeDTO> mergeDTOS) {
        if (mergeDTOS.size() == 1) {
            return true;
        }
        if (mergeDTOS.size() > 1) {
            for (int i = 0; i < mergeDTOS.size() - 1; i++) {
                if (!mergeDTOS.get(i).getTaskOutputJsonData().equals(mergeDTOS.get(i + 1).getTaskOutputJsonData())) {
                    return false;
                }
            }
        }
        return true;
    }

}
