FROM jboss/wildfly
ADD target/demo-app-0.7.war /opt/jboss/wildfly/standalone/deployments/
ENV JAVA_OPTS='-Xms256m -Xmx256m -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=128M -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true'
