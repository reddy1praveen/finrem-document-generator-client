package uk.gov.hmcts.reform.finrem.functional.idam;

import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.functional.ResourceLoader;
import uk.gov.hmcts.reform.finrem.functional.model.CreateUserRequest;
import uk.gov.hmcts.reform.finrem.functional.model.UserCode;


import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class IdamUtils {

    @Value("${idam.api.url}")
    private String idamUserBaseUrl;

    @Value("${idam.whitelist.url}")
    private String idamRedirectUri;

    @Value("${idam.api.secret}")
    private String idamSecret;

    private String idamUsername;
    private String idamPassword;
    private String testUserJwtToken;

    public List<Integer> responseCodes = ImmutableList.of(200, 204);

    public String getIdamTestUserToken() {
        if (StringUtils.isBlank(testUserJwtToken)) {
            createUserAndToken();
        }
        return testUserJwtToken;
    }

    public void deleteIdamTestUser() {
        if (!StringUtils.isBlank(testUserJwtToken)) {
            deleteUser();
        }
    }

    private void deleteUser() {
        Response response = RestAssured.given()
            .delete(idamCreateUrl() + "/" + idamUsername);
        if (responseCodes.contains(response.getStatusCode())) {
            testUserJwtToken = null;
        }
    }

    protected void createUserAndToken() {
        createUserInIdam();
        testUserJwtToken = generateUserTokenWithNoRoles(idamUsername, idamPassword);
    }

    private void createUserInIdam() {
        idamUsername = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
        idamPassword = UUID.randomUUID().toString();

        createUser(idamUsername, idamPassword);
    }

    public void createUser(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .email(username)
            .password(password)
            .forename("Test")
            .surname("User")
            .roles(new UserCode[] { UserCode.builder().code("citizen").build() })
            .userGroup(UserCode.builder().code("divorce-private-beta").build())
            .build();

        RestAssured.given()
            .header("Content-Type", "application/json")
            .body(ResourceLoader.objectToJson(userRequest))
            .post(idamCreateUrl());
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));


        Response response = RestAssured.given()
            .header("Authorization", authHeader)
            .relaxedHTTPSValidation()
            .post(idamCodeUrl());

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                + " body: " + response.getBody().prettyPrint());
        }

        response = RestAssured.given()
            .relaxedHTTPSValidation()
            .post(idamTokenUrl(response.getBody().path("code")));

        String token = response.getBody().path("access_token");


        return token;
    }

    private String idamCodeUrl() {
        String myUrl = idamUserBaseUrl + "/oauth2/authorize"
            + "?response_type=code"
            + "&client_id=finrem"
            + "&redirect_uri=" + idamRedirectUri;
        return myUrl;
    }

    private String idamTokenUrl(String code) {
        String myUrl = idamUserBaseUrl + "/oauth2/token"
            + "?code=" + code
            + "&client_id=finrem"
            + "&client_secret=" + idamSecret
            + "&redirect_uri=" + idamRedirectUri
            + "&grant_type=authorization_code";

        return myUrl;
    }
}
