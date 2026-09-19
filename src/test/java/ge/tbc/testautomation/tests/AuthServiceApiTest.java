package ge.tbc.testautomation.tests;

import ge.tbc.testautomation.api.client.ApiClientFactory;
import ge.tbc.testautomation.auth.invoker.ApiClient;
import ge.tbc.testautomation.auth.model.AuthenticationRequest;
import ge.tbc.testautomation.auth.model.AuthenticationResponse;
import ge.tbc.testautomation.auth.model.RefreshTokenRequest;
import ge.tbc.testautomation.auth.model.RefreshTokenResponse;
import ge.tbc.testautomation.auth.model.RegisterRequest;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import net.datafaker.Faker;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

import static ge.tbc.testautomation.auth.invoker.ResponseSpecBuilders.shouldBeCode;
import static ge.tbc.testautomation.auth.invoker.ResponseSpecBuilders.validatedWith;
import static ge.tbc.testautomation.data.ApiConstants.STATUS_BAD_REQUEST;
import static ge.tbc.testautomation.data.ApiConstants.STATUS_OK;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ACCESS_TOKEN_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ADMIN_RESOURCE_MESSAGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.AUTHORIZATION_HEADER;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.DELETE_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.EMAIL_DOMAIN;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.EMAIL_PREFIX;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.PASSWORD_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.PASSWORD_VALIDATION_MESSAGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.READ_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.REFRESH_TOKEN_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ROLE_ADMIN;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.ROLES_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.UPDATE_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.VALID_PASSWORD;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.WRITE_PRIVILEGE;
import static ge.tbc.testautomation.data.ApiConstants.AuthService.bearer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

@Epic("OpenAPI Generator")
@Feature("Local auth service")
public class AuthServiceApiTest {

    private ApiClient apiClient;
    private final Faker faker = new Faker();

    @BeforeMethod
    public void setUp() {
        apiClient = ApiClientFactory.authClient();
    }

    @Test
    @Description("Register as ADMIN, call protected resource with Bearer token, authenticate privileges and refresh token")
    public void registerAdminAccessAuthenticateAndRefresh() {
        String email = uniqueEmail();
        RegisterRequest registerRequest = new RegisterRequest()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName()).email(email).password(VALID_PASSWORD)
                .role(RegisterRequest.RoleEnum.ADMIN);

        AuthenticationResponse registration = apiClient.authentication()
                .register().body(registerRequest)
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ACCESS_TOKEN_FIELD, notNullValue())
                                .body(REFRESH_TOKEN_FIELD, notNullValue())
                                .extract().response()));

        String accessToken = registration.getAccessToken();
        assertThat(accessToken, notNullValue());

        String message = apiClient.authorization()
                .sayHelloWithRoleAdminAndReadAuthority()
                .reqSpec(spec -> spec.addHeader(AUTHORIZATION_HEADER, bearer(accessToken)))
                .execute(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then().extract().response()))
                .asString()
                .replace("\"", "");
        assertThat(message, containsString(ADMIN_RESOURCE_MESSAGE.replace(".", "")));

        AuthenticationResponse authentication = apiClient.authentication()
                .authenticate()
                .body(new AuthenticationRequest().email(email).password(VALID_PASSWORD))
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ROLES_FIELD, notNullValue())
                                .extract().response()));

        assertThat(authentication.getRoles(), hasItems(
                READ_PRIVILEGE, WRITE_PRIVILEGE, DELETE_PRIVILEGE,
                UPDATE_PRIVILEGE,ROLE_ADMIN));

        RefreshTokenResponse refreshed = apiClient.authentication()
                .refreshToken()
                .body(new RefreshTokenRequest().refreshToken(authentication.getRefreshToken()))
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ACCESS_TOKEN_FIELD, notNullValue())
                                .extract().response()));
        assertThat(refreshed.getAccessToken(), notNullValue());

        apiClient.authorization()
                .sayHelloWithRoleAdminAndReadAuthority()
                .reqSpec(spec -> spec.addHeader(AUTHORIZATION_HEADER, bearer(accessToken)))
                .execute(validatedWith(shouldBeCode(STATUS_OK)));
    }

    @Test(dataProvider = "invalidPasswords")
    @Description("Optional: invalid password combinations during registration")
    public void registerWithInvalidPasswordFormat(String password, String caseName) {
        Allure.parameter("invalidPasswordCase", caseName);

        RegisterRequest request = new RegisterRequest()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(uniqueEmail())
                .password(password)
                .role(RegisterRequest.RoleEnum.ADMIN);

        apiClient.authentication()
                .register()
                .body(request)
                .execute(validatedWith(shouldBeCode(STATUS_BAD_REQUEST))
                        .andThen(response -> response.then()
                                .body(PASSWORD_FIELD, equalTo(PASSWORD_VALIDATION_MESSAGE))
                                .extract().response()));
    }

    @DataProvider(name = "invalidPasswords")
    public Object[][] invalidPasswords() {
        return new Object[][]{
                {faker.regexify("[A-Za-z0-9@#$]{7}"), "shorter than 8 characters"},
                {faker.regexify("[A-Z]{4}[a-z]{4}[@#$]{2}"), "missing number"},
                {faker.regexify("[A-Z]{4}[0-9]{4}[@#$]{2}"), "missing lowercase letter"},
                {faker.regexify("[a-z]{4}[0-9]{4}[@#$]{2}"), "missing uppercase letter"},
                {faker.regexify("[A-Z]{3}[a-z]{3}[0-9]{3}"), "missing special character"},
                {faker.regexify("[A-Z]{8}"), "uppercase only"},
                {faker.regexify("[a-z]{8}"), "lowercase only"},
                {faker.regexify("[0-9]{8}"), "numbers only"},
                {faker.regexify("[@#$%^&+=!*()]{8}"), "special characters only"},
                {faker.regexify("[A-Z]{4}[a-z]{4}"), "uppercase and lowercase only"},
                {faker.regexify("[A-Z]{4}[0-9]{4}"), "uppercase and numbers only"},
                {faker.regexify("[a-z]{4}[0-9]{4}"), "lowercase and numbers only"},
                {faker.regexify("[A-Z]{4}[@#$%]{4}"), "uppercase and special only"},
                {faker.regexify("[a-z]{4}[@#$%]{4}"), "lowercase and special only"},
                {faker.regexify("[0-9]{4}[@#$%]{4}"), "numbers and special only"}
        };
    }

    private String uniqueEmail() {
        return EMAIL_PREFIX + UUID.randomUUID().toString().substring(0, 8) + EMAIL_DOMAIN;
    }
}
