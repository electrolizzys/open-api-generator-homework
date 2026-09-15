package ge.tbc.testautomation.steps;

import ge.tbc.testautomation.petstore.invoker.ApiClient;
import ge.tbc.testautomation.petstore.model.Order;
import ge.tbc.testautomation.petstore.model.Pet;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static ge.tbc.testautomation.data.ApiConstants.STATUS_OK;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.ID_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.NAME_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.STATUS_FIELD;
import static ge.tbc.testautomation.petstore.invoker.ResponseSpecBuilders.shouldBeCode;
import static ge.tbc.testautomation.petstore.invoker.ResponseSpecBuilders.validatedWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class PetStoreSteps {

    private final ApiClient apiClient;
    private Order createdOrder;
    private Pet createdPet;

    public PetStoreSteps(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Step("Place a store order")
    public PetStoreSteps placeOrder(Order order) {
        createdOrder = apiClient.store()
                .placeOrder()
                .body(order)
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ID_FIELD, notNullValue())
                                .body(STATUS_FIELD, equalTo(order.getStatus().getValue()))
                                .extract().response()));
        return this;
    }

    @Step("Validate created order POJO")
    public PetStoreSteps validateCreatedOrder(Order expected) {
        assertThat(createdOrder, notNullValue());
        assertThat(createdOrder.getId(), notNullValue());
        assertThat(createdOrder.getPetId(), equalTo(expected.getPetId()));
        assertThat(createdOrder.getQuantity(), equalTo(expected.getQuantity()));
        assertThat(createdOrder.getStatus(), equalTo(expected.getStatus()));
        return this;
    }

    @Step("Add a new pet")
    public PetStoreSteps addPet(Pet pet) {
        Response response = apiClient.pet()
                .addPet().body(pet)
                .execute(r -> r.then()
                        .statusCode(STATUS_OK)
                        .body(NAME_FIELD, equalTo(pet.getName()))
                        .body(STATUS_FIELD, equalTo(pet.getStatus().getValue()))
                        .extract().response());

        createdPet = response.as(Pet.class);
        assertThat(createdPet.getId(), notNullValue());
        assertThat(createdPet.getName(), equalTo(pet.getName()));
        return this;
    }
}
