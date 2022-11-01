#!/bin/sh
java \
  "-Dxygeni_url=${INPUT_XYGENI_URL}" \
  "-Dscanner_dir=${RUNNER_TEMP}" \
  "-Dtoken="${INPUT_TOKEN}" \
  "-Dusername=${INPUT_USERNAME}" "-Dpassword=${INPUT_PASSWORD}" \
  "-Drun=${INPUT_RUN}" "-Dproject=${INPUT_PROJECT}" \
  "-Ddirectory=${GITHUB_WORKSPACE}" \
  "-Dinclude=${INPUT_INCLUDE}" "-Dexclude=${INPUT_EXCLUDE}" \
  "-Doutput=${INPUT_OUTPUT}" "-Dformat=${INPUT_FORMAT}" "-Dcolumns=${INPUT_COLUMNS}" \
  "-Dcode=${INPUT_CODE}" "-Dconf=${INPUT_CONF}" "-Dbaseline=${INPUT_BASELINE}" \
  "-Dcustom_detectors_dir=${INPUT_CUSTOM_DETECTORS_DIR}" \
  "-Ddetectors=${INPUT_DETECTORS}" "-Dskip_detectors=${INPUT_SKIP_DETECTORS}" \
  "-Dsbom=${INPUT_SBOM}" "-Dsbom_format=${INPUT_SBOM_FORMAT}" \
  "-Dsecrets_mode=${INPUT_SECRETS_MODE}" \
  "-Dstandard=${INPUT_STANDARD}" \
  "-Dfail_on=${INPUT_FAIL_ON}" "-Dtry_all_scans=${INPUT_TRY_ALL_SCANS}" \
  -cp "/xygeni/lib/xygeni-github-action.jar" \
  io.xygeni.github.action.XygeniGitHubAction
