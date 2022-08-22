package com.depsdoctor.github.action;

import com.depsdoctor.github.action.config.ApiConfig;
import com.depsdoctor.github.action.config.DepsDoctorConfig;
import com.depsdoctor.github.action.http.HttpClientUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.depsdoctor.github.action.Command.*;
import static com.depsdoctor.github.action.utils.Files.unzipFile;
import static com.depsdoctor.github.action.utils.Strings.isBlank;

/**
 * Main class that will execute the process:
 * <ul>
 *   <li>Get parameters</li>
 *   <li>Validate parameters</li>
 *   <li>Download scanner</li>
 *   <li>Unzip scanner</li>
 *   <li>Download customer configuration</li>
 *   <li>Execute scanner</li>
 * <ul/>
 */
public class DepsDoctorGitHubAction {

  private static final Logger log = LoggerFactory.getLogger(DepsDoctorGitHubAction.class);

  private static final String DEFAULT_URL = "https://api.nauik.com/deps-doctor-service";
  private static final String USERNAME_PROPERTY = "username";
  private static final String PASSWORD_PROPERTY = "password";

  private static final String WILOSECURL_PROPERTY = "wilosecurl";

  private static final String ZIP_FILE = "scanner.zip";
  private static final String DEPS_DOCTOR_DIR = "deps_doctor";

  private static final String[] DEPS_DOCTOR_CMD = {"./deps-doctor", "scan", "-u"};

  public static void main(String[] args) throws Exception {

    log.info("Starting scanner action...");

    String url = System.getProperty(WILOSECURL_PROPERTY, DEFAULT_URL);
    String username = System.getProperty(USERNAME_PROPERTY);
    String password = System.getProperty(PASSWORD_PROPERTY);
    Command command = Command.builder()
        .project(System.getProperty(PROJECT_PROPERTY))
        .directory(System.getProperty(DIRECTORY_PROPERTY))
        .run(System.getProperty(RUN_PROPERTY))
        .include(System.getProperty(INCLUDE_PROPERTY))
        .exclude(System.getProperty(EXCLUDE_PROPERTY))
        .output(System.getProperty(OUTPUT_PROPERTY))
        .format(System.getProperty(FORMAT_PROPERTY))
        .columns(System.getProperty(COLUMNS_PROPERTY))
        .code(System.getProperty(CODE_PROPERTY))
        .conf(System.getProperty(CONF_PROPERTY))
        .baseline(System.getProperty(BASELINE_PROPERTY))
        .customDetectorsDir(System.getProperty(CUSTOM_DETECTORS_DIR_PROPERTY))
        .detectors(System.getProperty(DETECTORS_PROPERTY))
        .skipDetectors(System.getProperty(SKIP_DETECTORS_PROPERTY))
        .sbom(System.getProperty(SBOM_PROPERTY))
        .sbomFormat(System.getProperty(SBOM_FORMAT_PROPERTY))
        .secretsMode(System.getProperty(SECRETS_MODE_PROPERTY))
        .standard(System.getProperty(STANDARD_PROPERTY))
        .failOn(System.getProperty(FAIL_ON_PROPERTY))
        .build();

    if(isBlank(command.getProject()) || isBlank(command.getDirectory())) {
      log.error("The project and directory are mandatory");
      if(!isBlank(command.getFailOn()) && command.getFailOn().equalsIgnoreCase("never")) {
        System.exit(0);
        return;
      }
      System.exit(1);
      return;
    }

    downloadScanner(url, username, password);
    unzipScanner();
    downloadCustomerConfig();
    executeScanner(command);

    log.info("Scanner action  completed successfully.");
  }

  private static void downloadCustomerConfig() {
  }

  private static void unzipScanner() throws IOException {
    Path targetDir = Path.of(DEPS_DOCTOR_DIR);
    Path zipScanner = Path.of(ZIP_FILE);
    unzipFile(zipScanner, targetDir);
    // Path zipRelease = Path.of(DEPS_DOCTOR_DIR + "/deps-doctor-release.zip");
    // unzipFile(zipRelease, targetDir);
    File file = new File(DEPS_DOCTOR_DIR + "/deps-doctor/deps-doctor");
    //noinspection ResultOfMethodCallIgnored
    file.setExecutable(true);
  }

  private static void executeScanner(Command command) throws IOException, InterruptedException, TimeoutException {
    //Exclude the scanner downloaded
    if(isBlank(command.getExclude())) {
      command.setExclude(DEPS_DOCTOR_DIR + "/deps-doctor/**");
    }else{
      command.setExclude(command.getExclude() + "," + DEPS_DOCTOR_DIR + "/deps-doctor/**");
    }

    String[] args = ArrayUtils.addAll(DEPS_DOCTOR_CMD, command.getCommandParams());
    new ProcessExecutor()
        .directory(new File(DEPS_DOCTOR_DIR + "/deps-doctor/"))
        .command(args).timeout(60, TimeUnit.MINUTES)
        .redirectError(Slf4jStream.of(log).asWarn())
        .redirectOutput(Slf4jStream.of(log).asInfo()).execute();
  }

  private static void downloadScanner(String url, String username, String password) throws Exception {
    HttpClientUtils httpClientUtils = new HttpClientUtils();
    DepsDoctorConfig config = new DepsDoctorConfig();
    ApiConfig apiConfig = new ApiConfig();
    apiConfig.setUrl(url);
    apiConfig.setUsername(username);
    apiConfig.setPassword(password);
    config.setApi(apiConfig);
    InputStream is = httpClientUtils.getRelease(config);
    Files.copy(is, Path.of(ZIP_FILE), StandardCopyOption.REPLACE_EXISTING);
  }


}
