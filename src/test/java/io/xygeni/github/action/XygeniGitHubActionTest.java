package io.xygeni.github.action;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class XygeniGitHubActionTest {

  @Test @Disabled
  public void testAction() {
    System.setProperty("xygeni_url", "http://localhost:8086/deps-doctor-service");
    System.setProperty("username", "nauik");
    System.setProperty("password", "nauikp");
    System.setProperty("directory", "/home/felix/workspace/DepsDoctor-Dashboard/");
    System.setProperty("project", "actionTest");
    XygeniGitHubAction.main(new String[1]);
  }

}
