package com.depsdoctor.github.action.config;

import lombok.Data;


@Data
public class ApiConfig {
  private String url;
  private String version;
  private String username;
  private String password;
}
