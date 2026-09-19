package ge.tbc.testautomation.api.client;

import ge.tbc.testautomation.data.ApiConstants;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static io.restassured.config.RestAssuredConfig.config;

public final class ApiClientFactory {

    private ApiClientFactory() {
    }

    public static ge.tbc.testautomation.petstore.invoker.ApiClient petStoreClient() {
        return ge.tbc.testautomation.petstore.invoker.ApiClient.api(
                ge.tbc.testautomation.petstore.invoker.ApiClient.Config.apiConfig().reqSpecSupplier(
                        () -> commonSpec(ApiConstants.PetStore.BASE_URI)
                                .setConfig(config().objectMapperConfig(objectMapperConfig()
                                        .defaultObjectMapper(ge.tbc.testautomation.petstore.invoker.JacksonObjectMapper.jackson())))
                )
        );
    }

    public static ge.tbc.testautomation.auth.invoker.ApiClient authClient() {
        return ge.tbc.testautomation.auth.invoker.ApiClient.api(
                ge.tbc.testautomation.auth.invoker.ApiClient.Config.apiConfig().reqSpecSupplier(
                        () -> commonSpec(ApiConstants.AuthService.BASE_URI)
                                .setConfig(config().objectMapperConfig(objectMapperConfig()
                                        .defaultObjectMapper(ge.tbc.testautomation.auth.invoker.JacksonObjectMapper.jackson())))
                )
        );
    }

    private static RequestSpecBuilder commonSpec(String baseUri) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri).setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .log(LogDetail.ALL);
    }
}
