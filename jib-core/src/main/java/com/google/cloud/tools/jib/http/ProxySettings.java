package com.google.cloud.tools.jib.http;

import java.net.URL;
import java.util.function.Function;
import org.apache.http.HttpHost;

public class ProxySettings implements Function<URL, HttpHost> {

  private final String host;
  private final int port;

  public ProxySettings(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public HttpHost apply(URL url) {
    return new HttpHost(host, port);
  }
}
