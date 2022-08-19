package com.depsdoctor.github.action.http;

import com.depsdoctor.github.action.config.DepsDoctorConfig;

import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import static com.depsdoctor.github.action.utils.Strings.isBlank;

public class HttpClientUtils {

  private static final String RELEASE_ZIP = "deps-doctor-release.zip";

  private static HttpClient httpClient;

  public InputStream getRelease(DepsDoctorConfig config) throws Exception {
    HttpRequest request = getRequestBuilder(RELEASE_ZIP, config).GET().build();
    return client(config).send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
  }

  private HttpRequest.Builder getRequestBuilder(String uri, DepsDoctorConfig config) throws URISyntaxException {
    URI url = getUri(config.getApi().getUrl(), uri);

    HttpRequest.Builder builder = HttpRequest.newBuilder(url)
        .header("Authorization", getBasicAuthenticationHeader(config.getApi().getUsername(), config.getApi().getPassword()));

    if(config.getProxy().isActive() && !isBlank(config.getProxy().getUsername())) {
      String userPass = config.getProxy().getUsername() + ":" + config.getProxy().getPassword();
      String encoded = new String(Base64.getEncoder().encode(userPass.getBytes()));
      builder.setHeader("Proxy-Authorization", "Basic " + encoded);
    }
    return builder;
  }

  private URI getUri(String baseUrl, String uri) throws URISyntaxException {
    if(uri.startsWith("http:") || uri.startsWith("https:")) return new URI(uri);

    if(baseUrl.endsWith("/")) {
      if(uri.startsWith("/")) {
        return new URI(baseUrl + uri.substring(1));
      } else {
        return new URI(baseUrl + uri);
      }
    } else {
      if(uri.startsWith("/")) {
        return new URI(baseUrl + uri);
      } else {
        return new URI(baseUrl + "/" + uri);
      }
    }
  }

  private synchronized HttpClient client(DepsDoctorConfig config) {
    if(httpClient == null) {

      var builder = HttpClient.newBuilder()
          .authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(config.getApi().getUsername(), config.getApi().getPassword().toCharArray());
            }
          })
          .followRedirects(HttpClient.Redirect.NORMAL)
          .connectTimeout(Duration.ofSeconds(300));

      ProxySelector proxySel = proxy(config);
      if(proxySel != null) builder.proxy(proxySel);

      httpClient = builder.build();
    }
    return httpClient;
  }

  private String getBasicAuthenticationHeader(String username, String password) {
    String valueToEncode = username + ":" + password;
    return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
  }

  /**
   * The ProxySelector for connection to proxy servers.
   * This method returns {@code ProxySelector.getDefault()}.
   */
  protected ProxySelector proxy(DepsDoctorConfig config) {
    if(config.getProxy().isActive()) {
      return ProxySelector.of(new InetSocketAddress(config.getProxy().getHost(), config.getProxy().getPort()));
    }
    return ProxySelector.getDefault();
  }
}
