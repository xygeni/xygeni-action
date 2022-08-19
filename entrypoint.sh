#!/bin/sh
java -Dusername=${INPUT_USERNAME} -Dpassword=${INPUT_PASSWORD} -Dcommands=${INPUT_COMMANDS} -Dproject=${INPUT_PROJECT} -Ddirectory=${GITHUB_WORKSPACE} -cp "/deps_doctor/lib/deps-doctor-github-action.jar" com.depsdoctor.github.action.DepsDoctorGitHubAction
