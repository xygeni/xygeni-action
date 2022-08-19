package com.depsdoctor.github.action.config;

import lombok.Data;

@Data
public class DepsDoctorConfig {

  private ApiConfig api = new ApiConfig();
  private ProxyConfig proxy = new ProxyConfig();

}
