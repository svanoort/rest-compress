FROM jboss/wildfly
ADD target/demo-app-0.7.war /opt/jboss/wildfly/standalone/deployments/
ENV JAVA_OPTS='-Xms96m -Xmx96m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=96M -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true'
