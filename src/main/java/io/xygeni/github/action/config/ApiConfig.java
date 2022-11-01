package io.xygeni.github.action.config;

import lombok.Data;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/** Configuration for access to the Xygeni REST API  */
@Data
public class ApiConfig {
  /** Xygeni REST API base URL */
  private String url;
  private String version;

  /** The (Bearer) API token, base-64 encoded.  */
  private String token;

  /** Username for basic authentication */
  private String username;
  /** Password for basic authentication */
  private String password;

  public boolean isBasicAuthentication() {
    return isNotBlank(getUsername()) && isNotBlank(getPassword());
  }

  public boolean isTokenAuthentication() {
    return isNotBlank(getToken());
  }
}
