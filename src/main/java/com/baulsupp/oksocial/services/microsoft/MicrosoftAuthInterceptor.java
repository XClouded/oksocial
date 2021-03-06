package com.baulsupp.oksocial.services.microsoft;

import com.baulsupp.oksocial.authenticator.AuthInterceptor;
import com.baulsupp.oksocial.authenticator.AuthUtil;
import com.baulsupp.oksocial.authenticator.JsonCredentialsValidator;
import com.baulsupp.oksocial.authenticator.ValidatedCredentials;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2ServiceDefinition;
import com.baulsupp.oksocial.authenticator.oauth2.Oauth2Token;
import com.baulsupp.oksocial.output.OutputHandler;
import com.baulsupp.oksocial.secrets.Secrets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.baulsupp.oksocial.authenticator.JsonCredentialsValidator.fieldExtractor;

/**
 * https://graph.microsoft.io/en-us/docs/authorization/app_authorization
 * http://graph.microsoft.io/en-us/docs/authorization/permission_scopes
 */
public class MicrosoftAuthInterceptor implements AuthInterceptor<Oauth2Token> {
  @Override public Oauth2ServiceDefinition serviceDefinition() {
    return new Oauth2ServiceDefinition("graph.microsoft.com", "Microsoft API", "microsoft");
  }

  @Override public Response intercept(Interceptor.Chain chain, Oauth2Token credentials)
      throws IOException {
    Request request = chain.request();

    String token = credentials.accessToken;

    request =
        request.newBuilder().addHeader("Authorization", "Bearer " + token).build();

    return chain.proceed(request);
  }

  @Override public Oauth2Token authorize(OkHttpClient client, OutputHandler outputHandler,
      List<String> authArguments) throws IOException {
    System.err.println("Authorising Microsoft API");

    String clientId =
        Secrets.prompt("Microsoft Client Id", "microsoft.clientId", "", false);
    String clientSecret =
        Secrets.prompt("Microsoft Client Secret", "microsoft.clientSecret", "", true);

    return MicrosoftAuthFlow.login(client, outputHandler, clientId, clientSecret);
  }

  @Override public boolean canRenew(Response result, Oauth2Token credentials) {
    return result.code() == 401
        && credentials.refreshToken.isPresent()
        && credentials.clientId.isPresent()
        && credentials.clientSecret.isPresent();
  }

  @Override
  public Optional<Oauth2Token> renew(OkHttpClient client, Oauth2Token credentials)
      throws IOException {

    RequestBody body = new FormBody.Builder().add("grant_type", "refresh_token")
        .add("redirect_uri", "http://localhost:3000/callback")
        .add("client_id", credentials.clientId.get())
        .add("client_secret", credentials.clientSecret.get())
        .add("refresh_token", credentials.refreshToken.get())
        .add("resource", "https://graph.microsoft.com/")
        .build();

    Request request =
        new Request.Builder().url("https://login.microsoftonline.com/common/oauth2/token")
            .post(body)
            .build();

    Map<String, Object> responseMap = AuthUtil.makeJsonMapRequest(client, request);

    return Optional.of(new Oauth2Token((String) responseMap.get("access_token"),
        (String) responseMap.get("refresh_token"), credentials.clientId.get(),
        credentials.clientSecret.get()));
  }

  @Override public Future<Optional<ValidatedCredentials>> validate(OkHttpClient client,
      Request.Builder requestBuilder, Oauth2Token credentials) throws IOException {
    return new JsonCredentialsValidator(
        MicrosoftUtil.apiRequest("/v1.0/me", requestBuilder),
        fieldExtractor("displayName")).validate(
        client);
  }

  @Override public Set<String> hosts() {
    return MicrosoftUtil.API_HOSTS;
  }
}
