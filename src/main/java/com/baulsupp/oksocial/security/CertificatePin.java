package com.baulsupp.oksocial.security;

import java.util.List;
import okhttp3.CertificatePinner;

import static java.util.stream.Collectors.groupingBy;

public class CertificatePin {
  private final String pattern;
  private final String pin;

  public CertificatePin(String patternAndPin) {
    String[] parts = patternAndPin.split(":", 2);
    pattern = parts[0];
    pin = parts[1];
  }

  public String getPattern() {
    return pattern;
  }

  public String getPin() {
    return pin;
  }

  public static CertificatePinner buildFromCommandLine(List<CertificatePin> pins) {
    CertificatePinner.Builder builder = new CertificatePinner.Builder();

    pins.stream().collect(groupingBy(CertificatePin::getPattern)).forEach((host, pinList) -> {
      String[] pinArray = pinList.stream().map(CertificatePin::getPin).toArray(String[]::new);

      builder.add(host, pinArray);
    });

    return builder.build();
  }
}