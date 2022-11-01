FROM openjdk:11

# Copy the uberjar
COPY xygeni-github-action.jar /xygeni/lib/xygeni-github-action.jar
RUN chmod +x /xygeni/lib/xygeni-github-action.jar

# Copy the entrypoint script and properties used for the action
COPY entrypoint.sh /xygeni/entrypoint.sh
# Make it executable
RUN chmod +x /xygeni/entrypoint.sh
ENTRYPOINT ["/xygeni/entrypoint.sh"]
