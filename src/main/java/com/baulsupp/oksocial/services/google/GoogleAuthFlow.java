package com.baulsupp.oksocial.services.google;

import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.SimpleWebServer;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static java.util.stream.Collectors.joining;

public class GoogleAuthFlow {
  public static Oauth2Token login(OkHttpClient client, OutputHandler outputHandler, String clientId,
      String clientSecret, Set<String> scopes) throws IOException {
    try (SimpleWebServer<String> s = SimpleWebServer.forCode()) {

      String scopesString =
          URLEncoder.encode(scopes.stream().collect(joining(" ")), "UTF-8");

      String redirectUri = s.getRedirectUri();

      String loginUrl = "https://accounts.google.com/o/oauth2/v2/auth"
          + "?client_id=" + URLEncoder.encode(clientId, "UTF-8")
          + "&response_type=code"
          + "&scope=" + scopesString
          + "&state=x"
          + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");

      outputHandler.openLink(loginUrl);

      String code = s.waitForCode();

      String tokenUrl = "https://www.googleapis.com/oauth2/v4/token";
      RequestBody body =
          new FormBody.Builder().add("client_id", clientId)
              .add("redirect_uri", redirectUri)
              .add("client_secret", clientSecret)
              .add("code", code)
              .add("grant_type", "authorization_code")
              .build();
      Request request = new Request.Builder().url(tokenUrl).method("POST", body).build();

      Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

      return new Oauth2Token((String) responseMap.get("access_token"),
          (String) responseMap.get("refresh_token"), clientId, clientSecret);
    }
  }
}
