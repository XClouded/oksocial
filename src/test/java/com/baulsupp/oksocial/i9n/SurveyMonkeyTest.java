package com.baulsupp.oksocial.i9n;

import com.baulsupp.oksocial.Main;
import com.baulsupp.oksocial.services.surveymonkey.SurveyMonkeyAuthInterceptor;
import com.baulsupp.oksocial.services.surveymonkey.SurveyMonkeyToken;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SurveyMonkeyTest {

  private Main main = new Main();
  private TestOutputHandler output = new TestOutputHandler();
  private TestCompletionVariableCache completionCache = new TestCompletionVariableCache();
  private TestCredentialsStore credentialsStore = new TestCredentialsStore();

  {
    main.outputHandler = output;
    main.completionVariableCache = completionCache;
    main.credentialsStore = credentialsStore;
  }

  @Test public void completeEndpointWithReplacements() throws Throwable {
    main.urlCompletion = "https://api.surveymonkey.net/";
    completionCache.store("surveymonkey", "surveys", Lists.newArrayList("AA", "BB"));
    credentialsStore.storeCredentials(new SurveyMonkeyToken("", ""),
        new SurveyMonkeyAuthInterceptor().serviceDefinition());

    main.run();

    assertEquals(Lists.newArrayList(), output.failures);
    assertEquals(1, output.stdout.size());
    assertTrue(output.stdout.get(0).contains("/v3/surveys/AA/details"));
    assertTrue(output.stdout.get(0).contains("/v3/surveys/BB/details"));
  }
}
