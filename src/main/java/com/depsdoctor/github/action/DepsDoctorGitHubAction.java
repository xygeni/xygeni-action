package com.depsdoctor.github.action;

import com.depsdoctor.github.action.config.ApiConfig;
import com.depsdoctor.github.action.config.DepsDoctorConfig;
import com.depsdoctor.github.action.http.HttpClientUtils;
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
  private static final String COMMAND_PROPERTY = "command";
  private static final String PROJECT_PROPERTY = "project";
  private static final String DIRECTORY_PROPERTY = "directory";

  private static final String ZIP_FILE = "scanner.zip";
  private static final String DEPS_DOCTOR_DIR = "deps_doctor";

  public static void main(String[] args) throws Exception {

    log.info("Starting scanner action...");

    String username = System.getProperty(USERNAME_PROPERTY);
    String password = System.getProperty(PASSWORD_PROPERTY);
    String command = System.getProperty(COMMAND_PROPERTY);
    String project = System.getProperty(PROJECT_PROPERTY);
    String dir = System.getProperty(DIRECTORY_PROPERTY);

    if(isBlank(command) || isBlank(project) || isBlank(dir)) {
      log.error("The parameters commands, project and directory are mandatory");
      System.exit(1);
      return;
    }

    log.info(dir);

    downloadScanner(DEFAULT_URL, username, password);
    unzipScanner();
    downloadCustomerConfig();
    executeScanner(project, dir, command);

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

  private static void executeScanner(String project, String dir, String command) throws IOException, InterruptedException, TimeoutException {
    new ProcessExecutor()
        .directory(new File(DEPS_DOCTOR_DIR + "/deps-doctor/"))
        .command("./deps-doctor", command, "-n", project, "-d", dir, "-e", DEPS_DOCTOR_DIR + "/deps-doctor/**", "-u").timeout(60, TimeUnit.MINUTES)
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
