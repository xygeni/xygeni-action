package io.xygeni.github.action.http;

import io.xygeni.github.action.config.ApiConfig;
import io.xygeni.github.action.config.DepsDoctorConfig;
import io.xygeni.github.action.config.ProxyConfig;
import io.xygeni.github.action.utils.OS;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * HttpClientUtils - Simplifies calls to Xygeni API for downloading the scanner.
 *
 */
public class HttpClientUtils {
  // The endpoint may change name to xygeni-scanner.zip, for example
  private static final String RELEASE_ZIP = "deps-doctor-release.zip";
  public static final String AUTHORIZATION = "Authorization";

  private static HttpClient httpClient;

  public InputStream downloadScanner(DepsDoctorConfig config) throws Exception {
    HttpRequest request = getRequestBuilder(RELEASE_ZIP, config).GET().build();
    return client(config).send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
  }

  @SuppressWarnings("SameParameterValue")
  private Builder getRequestBuilder(String uri, DepsDoctorConfig config) throws URISyntaxException {
    ApiConfig api = config.getApi();

    URI url = getUri(api.getUrl(), uri);
    Builder builder = HttpRequest.newBuilder(url);

    if(api.isTokenAuthentication()) {
      builder.setHeader(AUTHORIZATION, "Bearer " + api.getToken());

    } else if(api.isBasicAuthentication()) {
      builder.header(AUTHORIZATION, getBasicAuthenticationHeader(api.getUsername(), api.getPassword()));

    } else {
      throw new IllegalArgumentException("Either token or username/password must be provided");
    }

    ProxyConfig proxy = config.getProxy();
    if(proxy.isActive() && !isBlank(proxy.getUsername())) {
      String userPass = proxy.getUsername() + ":" + proxy.getPassword();
      String encoded = Base64.getEncoder().encodeToString(userPass.getBytes());
      builder.setHeader("Proxy-Authorization", "Basic " + encoded);
    }
    return builder;
  }

  private URI getUri(String baseUrl, String uri) throws URISyntaxException {
    if(uri.startsWith("http:") || uri.startsWith("https:")) return new URI(uri);

    // ensure that a single / separates baseUrl and uri
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
    if(httpClient == null) httpClient = buildHttpClient(config);
    return httpClient;
  }

  private HttpClient buildHttpClient(DepsDoctorConfig config) {
    var builder = HttpClient.newBuilder();
    var api = config.getApi();

    if(api.isBasicAuthentication()) {
      // Basic authentication
      builder.authenticator(new Authenticator() {
        @Override protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(api.getUsername(), api.getPassword().toCharArray());
        }
      });
    }

    builder
      .followRedirects(Redirect.NORMAL)
      .connectTimeout(Duration.ofSeconds(300));

    ProxySelector proxySel = proxy(config);
    if(proxySel != null) builder.proxy(proxySel);

    return builder.build();
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
    var proxy = config.getProxy();

    if(proxy.isActive()) {
      return ProxySelector.of(
        new InetSocketAddress(proxy.getHost(), proxy.getPort())
      );
    }

    return ProxySelector.getDefault();
  }

  public InputStream downloadScript(DepsDoctorConfig config) throws URISyntaxException, IOException, InterruptedException {
    HttpRequest request;
    if(OS.isWindows()) {
      request = getRequestBuilder("/latest/scanner/install.ps1", config).GET().build();
    } else {
      request = getRequestBuilder("/latest/scanner/install.sh", config).GET().build();
    }
    return client(config).send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
  }
}
