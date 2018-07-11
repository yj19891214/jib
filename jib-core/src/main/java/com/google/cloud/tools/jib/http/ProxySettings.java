package com.google.cloud.tools.jib.http;

public class ProxySettings {

  private final String host;
  private final int port;

  public ProxySettings(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}
