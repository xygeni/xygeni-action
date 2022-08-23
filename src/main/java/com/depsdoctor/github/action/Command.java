package com.depsdoctor.github.action;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.depsdoctor.github.action.utils.Strings.isBlank;

/**
 * Class that contains all user parameters
 */
@Data
@Builder
public class Command {

  public static final String PROJECT_PROPERTY = "project";
  public static final String DIRECTORY_PROPERTY = "directory";
  public static final String RUN_PROPERTY = "run";
  public static final String INCLUDE_PROPERTY = "include";
  public static final String EXCLUDE_PROPERTY = "exclude";
  public static final String OUTPUT_PROPERTY = "output";
  public static final String FORMAT_PROPERTY = "format";
  public static final String COLUMNS_PROPERTY = "columns";
  public static final String CODE_PROPERTY = "code";
  public static final String CONF_PROPERTY = "conf";
  public static final String BASELINE_PROPERTY = "baseline";
  public static final String CUSTOM_DETECTORS_DIR_PROPERTY = "custom_detectors_dir";
  public static final String DETECTORS_PROPERTY = "detectors";
  public static final String SKIP_DETECTORS_PROPERTY = "skip_detectors";
  public static final String SBOM_PROPERTY = "sbom";
  public static final String SBOM_FORMAT_PROPERTY = "sbom_format";
  public static final String SECRETS_MODE_PROPERTY = "secrets_mode";
  public static final String STANDARD_PROPERTY = "standard";
  public static final String FAIL_ON_PROPERTY = "fail_on";
  public static final String TRY_ALL_SCANS_PROPERTY = "try_all_scans";

  private String project;
  private String directory;
  private String run;
  private String include;
  private String exclude;
  private String output;
  private String format;
  private String columns;
  private String code;
  private String conf;
  private String baseline;
  private String customDetectorsDir;
  private String detectors;
  private String skipDetectors;
  private String sbom;
  private String sbomFormat;
  private String secretsMode;
  private String standard;
  private String failOn;
  private String tryAllScans;

  public String[] getCommandParams() {
    List<String> params = new ArrayList<>();
    if(!isBlank(project)) addParam("-n", project, params);
    if(!isBlank(directory)) addParam("-d", directory, params);
    if(!isBlank(run)) addParam("--run", run, params);
    if(!isBlank(include)) addParam("-i", include, params);
    if(!isBlank(exclude)) addParam("-e", exclude, params);
    if(!isBlank(output)) addParam("-o", output, params);
    if(!isBlank(format)) addParam("-f", format, params);
    if(!isBlank(columns)) addParam("--cols", columns, params);
    if(!isBlank(code) && code.equalsIgnoreCase("false")) addParam("--no-code", null, params);
    if(!isBlank(conf)) addParam("-c", conf, params);
    if(!isBlank(baseline)) addParam("-b", baseline, params);
    if(!isBlank(customDetectorsDir)) addParam("--custom-detectors-dir", customDetectorsDir, params);
    if(!isBlank(detectors)) addParam("--detectors", detectors, params);
    if(!isBlank(skipDetectors)) addParam("--skip-detectors", skipDetectors, params);
    if(!isBlank(sbom)) addParam("--sbom", sbom, params);
    if(!isBlank(sbomFormat)) addParam("--sbom-format", sbomFormat, params);
    if(!isBlank(secretsMode)) addParam("--secrets-mode", secretsMode, params);
    if(!isBlank(standard)) addParam("-s", standard, params);
    if(!isBlank(failOn)) {
      if(failOn.equalsIgnoreCase("never")) {
        addParam("--never-fail", null, params);
      } else {
        addParam("--fail-on", failOn, params);
      }
    }
    if(!isBlank(tryAllScans) && "true".equalsIgnoreCase(tryAllScans)) addParam("--try-all-scans", null, params);
    return params.toArray(new String[0]);
  }

  private void addParam(String option, String value, List<String> params) {
    params.add(option);
    if(value != null) params.add(value);
  }

}
