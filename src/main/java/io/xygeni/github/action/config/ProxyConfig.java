package io.xygeni.github.action.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class ProxyConfig {
  private String protocol = "http";
  private String host;
  private int port = 3128;
  private long timeout = 30_000;
  private String authentication = "none"; // none. basic
  private String username;
  private String password;

  /** If true (host is not blank), there is a proxy configured. */
  public boolean isActive() { return host != null && host.trim().length() > 0; }

  public boolean isAuthenticationNone() {
    return "none".equalsIgnoreCase(authentication);
  }
  public boolean isAuthenticationBasic() { return "basic".equalsIgnoreCase(authentication); }
}
