package ge.tbc.testautomation.steps;

import ge.tbc.testautomation.auth.invoker.ApiClient;
import ge.tbc.testautomation.auth.model.AuthenticationRequest;
import ge.tbc.testautomation.auth.model.AuthenticationResponse;
import ge.tbc.testautomation.auth.model.RefreshTokenRequest;
import ge.tbc.testautomation.auth.model.RefreshTokenResponse;
import ge.tbc.testautomation.auth.model.RegisterRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static ge.tbc.testautomation.auth.invoker.ResponseSpecBuilders.shouldBeCode;
import static ge.tbc.testautomation.auth.invoker.ResponseSpecBuilders.validatedWith;
import static ge.tbc.testautomation.data.ApiConstants.STATUS_BAD_REQUEST;
import static ge.tbc.testautomation.data.ApiConstants.STATUS_OK;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ACCESS_TOKEN_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ADMIN_RESOURCE_MESSAGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.AUTHORIZATION_HEADER;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.DELETE_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.PASSWORD_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.PASSWORD_VALIDATION_MESSAGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.READ_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.REFRESH_TOKEN_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ROLE_ADMIN;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ROLES_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.UPDATE_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.WRITE_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.bearer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

public class AuthSteps {

    private final ApiClient apiClient;
    private AuthenticationResponse registrationResponse;
    private AuthenticationResponse authenticationResponse;
    private RefreshTokenResponse refreshTokenResponse;
    private String accessToken;
    private String refreshToken;

    public AuthSteps(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    public String getAccessToken() {
        return accessToken;
    }
    @Step("Register admin user")
    public AuthSteps registerAdmin(RegisterRequest request) {
        registrationResponse = apiClient.authentication()
                .register().body(request)
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ACCESS_TOKEN_FIELD, notNullValue())
                                .body(REFRESH_TOKEN_FIELD, notNullValue())
                                .extract().response()));

        accessToken = registrationResponse.getAccessToken();
        refreshToken = registrationResponse.getRefreshToken();
        assertThat(accessToken, notNullValue());
        return this;
    }

    @Step("Call protected admin resource with Bearer token")
    public AuthSteps accessAdminResource() {
        String message = apiClient.authorization()
                .sayHelloWithRoleAdminAndReadAuthority()
                .reqSpec(spec -> spec.addHeader(AUTHORIZATION_HEADER, bearer(accessToken)))
                .execute(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .extract().response()))
                .asString().replace("\"", "");

        assertThat(message, containsString(ADMIN_RESOURCE_MESSAGE.replace(".", "")));
        return this;
    }

    @Step("Authenticate registered user")
    public AuthSteps authenticate(String email, String password) {
        authenticationResponse = apiClient.authentication()
                .authenticate()
                .body(new AuthenticationRequest().email(email).password(password))
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ROLES_FIELD, notNullValue())
                                .extract().response()));

        accessToken = authenticationResponse.getAccessToken();
        refreshToken = authenticationResponse.getRefreshToken();
        return this;
    }

    @Step("Validate admin privileges")
    public AuthSteps validateAdminPrivileges() {
        assertThat(authenticationResponse.getRoles(), hasItems(
                READ_PRIVILEGE, WRITE_PRIVILEGE, DELETE_PRIVILEGE,
                UPDATE_PRIVILEGE, ROLE_ADMIN));
        return this;
    }

    @Step("Refresh access token")
    public AuthSteps refreshToken() {
        refreshTokenResponse = apiClient.authentication()
                .refreshToken()
                .body(new RefreshTokenRequest().refreshToken(refreshToken))
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ACCESS_TOKEN_FIELD, notNullValue())
                                .extract().response()));

        assertThat(refreshTokenResponse.getAccessToken(), notNullValue());
        return this;
    }

    @Step("Check whether the previous access token is still valid")
    public AuthSteps validateOldAccessTokenStillWorks(String previousAccessToken) {
        apiClient.authorization()
                .sayHelloWithRoleAdminAndReadAuthority()
                .reqSpec(spec -> spec.addHeader(AUTHORIZATION_HEADER, bearer(previousAccessToken)))
                .execute(validatedWith(shouldBeCode(STATUS_OK)));
        return this;
    }

    @Step("Register with invalid password and expect validation error")
    public AuthSteps registerWithInvalidPassword(RegisterRequest request) {
        Response response = apiClient.authentication()
                .register().body(request)
                .execute(validatedWith(shouldBeCode(STATUS_BAD_REQUEST)));
        assertThat(response.jsonPath().getString(PASSWORD_FIELD), equalTo(PASSWORD_VALIDATION_MESSAGE));
        return this;
    }
}
