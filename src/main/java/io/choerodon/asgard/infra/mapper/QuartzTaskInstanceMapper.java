package io.choerodon.asgard.infra.mapper;

import io.choerodon.asgard.api.dto.PollScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceDTO;
import io.choerodon.asgard.api.dto.ScheduleTaskInstanceLogDTO;
import io.choerodon.asgard.domain.QuartzTaskInstance;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface QuartzTaskInstanceMapper extends BaseMapper<QuartzTaskInstance> {

    QuartzTaskInstance selectLastInstance(@Param("taskId") long taskId);

    List<ScheduleTaskInstanceDTO> fulltextSearch(@Param("status") String status,
                                                 @Param("taskName") String taskName,
                                                 @Param("exceptionMessage") String exceptionMessage,
                                                 @Param("params") String params,
                                                 @Param("level") String level,
                                                 @Param("sourceId") Long sourceId);

    List<PollScheduleTaskInstanceDTO> pollBathByMethod(@Param("method") String method);

    int unlockByInstance(@Param("instance") String instance);

    int lockByInstanceAndUpdateStartTime(@Param("id") long id,
                                         @Param("instance") String instance,
                                         @Param("number") Long objectVersionNumber,
                                         @Param("time") Date date);


    List<ScheduleTaskInstanceLogDTO> selectByTaskId(@Param("taskId") Long taskId,
                                                    @Param("status") String status,
                                                    @Param("serviceInstanceId") String serviceInstanceId,
                                                    @Param("params") String params,
                                                    @Param("level") String level,
                                                    @Param("sourceId") Long sourceId);

}
