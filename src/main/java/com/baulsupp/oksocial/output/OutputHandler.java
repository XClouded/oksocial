package com.baulsupp.oksocial.output;

import java.io.IOException;
import okhttp3.Response;

public interface OutputHandler {
  void showOutput(Response response, boolean showHeaders) throws IOException;

  default void showError(String s, Throwable e) {
    System.err.println(s);
    e.printStackTrace();
  }

  default void openLink(String url) throws IOException {
    System.err.println(url);
  }

  default void info(String s) {
    System.out.println(s);
  }
}
