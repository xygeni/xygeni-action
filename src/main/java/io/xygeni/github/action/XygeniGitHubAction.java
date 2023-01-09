package io.xygeni.github.action;

import io.xygeni.github.action.config.ApiConfig;
import io.xygeni.github.action.config.DepsDoctorConfig;
import io.xygeni.github.action.http.HttpClientUtils;
import io.xygeni.github.action.utils.OS;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.xygeni.github.action.Command.*;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
 *
 * @author felix.carnicero
 *
 * @version 01-Nov-2022 lrodriguez -
 *   Changed deps-doctor by xygeni.
 *   The scanner directory moved to RUNNER_TEMP, a temporary directory instead of the working dir ()
 */
public class XygeniGitHubAction {

  private static final Logger log = LoggerFactory.getLogger(XygeniGitHubAction.class);

  private static final String DEFAULT_URL = "https://api.xygeni.com";

  private static final String TOKEN_PROPERTY = "token";
  private static final String USERNAME_PROPERTY = "username";
  private static final String PASSWORD_PROPERTY = "password";
  private static final String SCANNER_DIR_PROPERTY = "scanner_dir";

  private static final String XYGENI_URL_PROPERTY = "xygeni_url";

  // The relative directory to working directory where the scanner will be unzipped, when RUNNER_TEMP not available
  private static final String XYGENI_SCANNER_DEPLOYDIR = ".";

  // The scanner directory in zip directory
  private static final String XYGENI_SCANNER_DIR = "xygeni_scanner";
  // The script (bash version) to run
  private static final String XYGENI_SCRIPT = "./xygeni";
  // The location of the main configuration (for writing the scanner authentication)
  private static final String XYGENI_CONF_PATH = XYGENI_SCANNER_DIR + "/conf/xygeni.yml";

  private static final String[] XYGENI_CMD = { XYGENI_SCRIPT, "scan"};

  private static final String PS_FILE = "install.ps1";
  private static final String SH_FILE = "install.sh";

  public static void main(String[] args) {
    log.info("Starting scanner action...");

    String url = getProperty(XYGENI_URL_PROPERTY, DEFAULT_URL);
    String token = getProperty(TOKEN_PROPERTY);
    String username = getProperty(USERNAME_PROPERTY);
    String password = getProperty(PASSWORD_PROPERTY);

    // Use RUNNER_TEMP temporary directory for the runner
    String scannerDir = getProperty(SCANNER_DIR_PROPERTY);
    if(isBlank(scannerDir)) scannerDir = System.getenv("RUNNER_TEMP");
    if(isBlank(scannerDir)) scannerDir = (new File(".")).getAbsolutePath();

    Command command = builder()
        .project(getProperty(PROJECT_PROPERTY))
        .directory(getProperty(DIRECTORY_PROPERTY))
        .run(getProperty(RUN_PROPERTY))
        .include(getProperty(INCLUDE_PROPERTY))
        .exclude(getProperty(EXCLUDE_PROPERTY))
        .output(getProperty(OUTPUT_PROPERTY))
        .format(getProperty(FORMAT_PROPERTY))
        .columns(getProperty(COLUMNS_PROPERTY))
        .code(getProperty(CODE_PROPERTY))
        .conf(getProperty(CONF_PROPERTY))
        .baseline(getProperty(BASELINE_PROPERTY))
        .customDetectorsDir(getProperty(CUSTOM_DETECTORS_DIR_PROPERTY))
        .detectors(getProperty(DETECTORS_PROPERTY))
        .skipDetectors(getProperty(SKIP_DETECTORS_PROPERTY))
        .sbom(getProperty(SBOM_PROPERTY))
        .sbomFormat(getProperty(SBOM_FORMAT_PROPERTY))
        .secretsMode(getProperty(SECRETS_MODE_PROPERTY))
        .standard(getProperty(STANDARD_PROPERTY))
        .failOn(getProperty(FAIL_ON_PROPERTY))
        .tryAllScans(getProperty(TRY_ALL_SCANS_PROPERTY))
        .build();

    // Validate that either token or username/password are available
    if(isBlank(token) && isBlank(username)) {
      log.error("Either API token or username/password must be specified");
      if(!isBlank(command.getFailOn()) && command.getFailOn().equalsIgnoreCase("never")) {
        System.exit(0);
        return;
      }
      System.exit(1);
      return;
    }

    // Check that the mandatory project / directory arguments are provided
    if(isBlank(command.getProject()) || isBlank(command.getDirectory())) {
      log.error("The project and directory are mandatory");
      if(!isBlank(command.getFailOn()) && command.getFailOn().equalsIgnoreCase("never")) {
        System.exit(0);
        return;
      }
      System.exit(1);
      return;
    }

    try {
      File script = downloadScriptInstaller(scannerDir, url, token, username, password);
      executeInstaller(script, token, username, password);
      updateCredentials(scannerDir, url, token, username, password);
      executeScanner(command, scannerDir);

    } catch(Throwable e) {
      log.error("Error executing xygeni-action: " + e.getMessage(), e);
      if(!isBlank(command.getFailOn()) && command.getFailOn().equalsIgnoreCase("never")) {
        System.exit(0);
        return;
      }
    }

    log.info("Scanner action  completed successfully.");
  }

  private static void updateCredentials(String scannerDir, String url, String token, String username, String password) throws IOException {
    File configFile = new File(scannerDir, XYGENI_CONF_PATH);
    File tmpFile = new File(scannerDir, XYGENI_CONF_PATH + ".tmp");
    Files.move(configFile.toPath(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    List<String> lines = Files.readAllLines(tmpFile.toPath());
    List<String> outLines = new ArrayList<>();

    boolean isInApi = false;
    for (String line : lines) {
      if(!line.startsWith(" ")) isInApi = false;
      if(line.startsWith("api:")) isInApi = true;
      if(isInApi) {
        if(line.trim().startsWith("url:") && isNotBlank(url)) {
          line = "  url: " + url;
        }
        if(line.trim().startsWith("username:") && isNotBlank(username)) {
          line = "  username: " + username;
        }
        if(line.trim().startsWith("password:") && isNotBlank(password)) {
          line = "  password: " + password;
        }
        if(line.trim().startsWith("apikey:") && isNotBlank(token)) {
          line = "  apikey: " + token;
        }
      }
      outLines.add(line);
    }
    Files.write(configFile.toPath(), outLines);
  }

  private static void executeInstaller(File script, String token, String username, String password) throws IOException, InterruptedException, TimeoutException {
    String[] command;
    if(isNotBlank(token) && token.startsWith("xya_")){
      command = new String[]{script.getAbsolutePath(), "-o", "-t", token, "-d", script.getParentFile().getAbsolutePath() + "/" + XYGENI_SCANNER_DIR};
    }else{
      command = new String[]{script.getAbsolutePath(), "-o", "-u", username, "-p", password, "-d", script.getParentFile().getAbsolutePath() + "/" + XYGENI_SCANNER_DIR};
    }
    new ProcessExecutor()
        .directory(script.getParentFile())
        .command(command).timeout(60, TimeUnit.MINUTES)
        .redirectError(Slf4jStream.of(log).asWarn())
        .redirectOutput(Slf4jStream.of(log).asInfo()).execute();
  }

  private static void executeScanner(Command command, String scannerDir) throws IOException, InterruptedException, TimeoutException {
    // Exclude the scanner downloaded, if not the temporary RUNNER_TEMP
    if(XYGENI_SCANNER_DEPLOYDIR.equals(scannerDir)) {
      if (isBlank(command.getExclude())) {
        command.setExclude(XYGENI_SCANNER_DIR+"/**");
      } else {
        command.setExclude(command.getExclude() + "," + XYGENI_SCANNER_DIR + "/**");
      }
    }

    String[] args = ArrayUtils.addAll(XYGENI_CMD, command.getCommandParams());
    ProcessResult result = new ProcessExecutor()
        .directory( new File(scannerDir, XYGENI_SCANNER_DIR) )
        .command(args).timeout(60, TimeUnit.MINUTES)
        .redirectError(Slf4jStream.of(log).asWarn())
        .redirectOutput(Slf4jStream.of(log).asInfo()).execute();

    System.exit(result.getExitValue());
  }

  private static File downloadScriptInstaller(String scannerDir, String url, String token, String username, String password) throws Exception {
    DepsDoctorConfig config = new DepsDoctorConfig();
    ApiConfig api = new ApiConfig();
    api.setUrl(url);
    api.setToken(token);
    api.setUsername(username);
    api.setPassword(password);
    config.setApi(api);

    File dir = new File(scannerDir);
    if(!dir.exists()) {
      log.info("Directory {} does not exist", scannerDir);
      boolean created = dir.mkdirs();
      if(created) log.info("Directory {} created", scannerDir);
      else log.error("can not create directory {} ", scannerDir);
    }

    InputStream is = new HttpClientUtils().downloadScript(config);
    if(OS.isWindows()) {
      File psFile = new File(scannerDir, PS_FILE);
      Files.copy(is, psFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return psFile;
    }else{
      File shFile = new File(scannerDir, SH_FILE);
      Files.copy(is, shFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return shFile;
    }
  }


}
