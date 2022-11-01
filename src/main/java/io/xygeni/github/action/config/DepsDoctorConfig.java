package io.xygeni.github.action.config;

import lombok.Data;

/** Minimal configuration for the Xygeni scanner (API and Proxy). */
@Data
public class DepsDoctorConfig {

  private ApiConfig api = new ApiConfig();
  // LR - Is proxy needed for GitHub? If not on-prem, no proxy with standard GitHub !
  private ProxyConfig proxy = new ProxyConfig();

}
