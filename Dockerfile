FROM openjdk:11

COPY deps-doctor-github-action.jar /deps_doctor/lib/deps-doctor-github-action.jar
RUN chmod +x /deps_doctor/lib/deps-doctor-github-action.jar

#Copy the entrypoint script and properties used for the action
COPY entrypoint.sh /deps_doctor/entrypoint.sh
#Make it executable
RUN chmod +x /deps_doctor/entrypoint.sh
ENTRYPOINT ["/deps_doctor/entrypoint.sh"]
