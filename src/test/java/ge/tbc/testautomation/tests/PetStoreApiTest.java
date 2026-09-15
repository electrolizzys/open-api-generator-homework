package ge.tbc.testautomation.tests;

import ge.tbc.testautomation.api.client.ApiClientFactory;
import ge.tbc.testautomation.petstore.invoker.ApiClient;
import ge.tbc.testautomation.petstore.model.Order;
import ge.tbc.testautomation.petstore.model.Pet;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import net.datafaker.Faker;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static ge.tbc.testautomation.data.ApiConstants.STATUS_OK;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.APPROVED_STATUS;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.AVAILABLE_STATUS;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.ID_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.NAME_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.ORDER_COMPLETE;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.ORDER_ID;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.ORDER_QUANTITY;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.ORDER_SHIP_DATE;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.PET_ID;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.PET_ID_FIELD;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.RANDOM_PET_ID_MAX;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.RANDOM_PET_ID_MIN;
import static ge.tbc.testautomation.data.ApiConstants.PetStore.STATUS_FIELD;
import static ge.tbc.testautomation.petstore.invoker.ResponseSpecBuilders.shouldBeCode;
import static ge.tbc.testautomation.petstore.invoker.ResponseSpecBuilders.validatedWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Epic("OpenAPI Generator")
@Feature("Petstore v3")
public class PetStoreApiTest {

    private ApiClient apiClient;
    private final Faker faker = new Faker();

    @BeforeMethod
    public void setUp() {
        apiClient = ApiClientFactory.petStoreClient();
    }

    @Test
    @Description("POST /store/order using generated ApiClient, shouldBeCode, validatedWith, andThen and executeAs")
    public void placeStoreOrder() {
        Order order = new Order()
                .id(ORDER_ID).petId(PET_ID).quantity(ORDER_QUANTITY)
                .shipDate(OffsetDateTime.parse(ORDER_SHIP_DATE))
                .status(Order.StatusEnum.APPROVED)
                .complete(ORDER_COMPLETE);

        Order createdOrder = apiClient.store()
                .placeOrder().body(order)
                .executeAs(validatedWith(shouldBeCode(STATUS_OK))
                        .andThen(response -> response.then()
                                .body(ID_FIELD, notNullValue())
                                .body(PET_ID_FIELD, equalTo((int) PET_ID))
                                .body(STATUS_FIELD, equalTo(APPROVED_STATUS))
                                .extract().response()));

        assertThat(createdOrder.getId(), notNullValue());
        assertThat(createdOrder.getPetId(), equalTo(order.getPetId()));
        assertThat(createdOrder.getQuantity(), equalTo(order.getQuantity()));
        assertThat(createdOrder.getStatus(), equalTo(Order.StatusEnum.APPROVED));
    }

    @Test
    @Description("Optional: POST /pet and validate the Rest Assured Response with execute lambda")
    public void addNewPet() {
        Pet pet = new Pet()
                .id(ThreadLocalRandom.current().nextLong(RANDOM_PET_ID_MIN, RANDOM_PET_ID_MAX))
                .name(faker.dog().name())
                .addPhotoUrlsItem(faker.internet().url())
                .status(Pet.StatusEnum.AVAILABLE);

        apiClient.pet()
                .addPet()
                .body(pet)
                .execute(response -> response.then()
                        .statusCode(STATUS_OK)
                        .body(NAME_FIELD, equalTo(pet.getName()))
                        .body(STATUS_FIELD, equalTo(AVAILABLE_STATUS))
                        .extract().response());
    }
}
