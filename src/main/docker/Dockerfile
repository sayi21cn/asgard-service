FROM registry.cn-hangzhou.aliyuncs.com/choerodon-tools/javabase:0.8.0
COPY app.jar /asgard-service.jar
ENTRYPOINT exec java $JAVA_OPTS $SKYWALKING_OPTS  -jar /asgard-service.jar
