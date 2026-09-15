package ge.tbc.testautomation.data;

public final class ApiConstants {

    private ApiConstants() {
    }

    public static final int STATUS_OK = 200;
    public static final int STATUS_BAD_REQUEST = 400;

    public static final class PetStore {
        public static final String BASE_URI = "https://petstore3.swagger.io/api/v3";
        public static final String STORE_ORDER = "/store/order";
        public static final String PET = "/pet";
        public static final String ID_FIELD = "id";
        public static final String PET_ID_FIELD = "petId";
        public static final String STATUS_FIELD = "status";
        public static final String NAME_FIELD = "name";
        public static final long ORDER_ID = 10L;
        public static final long PET_ID = 198772L;
        public static final int ORDER_QUANTITY = 7;
        public static final String ORDER_SHIP_DATE = "2026-09-15T13:24:02.551Z";
        public static final String APPROVED_STATUS = "approved";
        public static final boolean ORDER_COMPLETE = true;
        public static final String AVAILABLE_STATUS = "available";
        public static final long RANDOM_PET_ID_MIN = 100_000L;
        public static final long RANDOM_PET_ID_MAX = 1_000_000_000L;
    }

    public static final class AuthService {
        public static final String BASE_URI = "http://localhost:8086";
        public static final String SWAGGER_UI = "http://localhost:8086/swagger-ui.html";
        public static final String REGISTER = "/api/v1/auth/register";
        public static final String AUTHENTICATE = "/api/v1/auth/authenticate";
        public static final String REFRESH_TOKEN = "/api/v1/auth/refresh-token";
        public static final String ADMIN_RESOURCE = "/api/v1/admin/resource";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String ACCESS_TOKEN_FIELD = "access_token";
        public static final String REFRESH_TOKEN_FIELD = "refresh_token";
        public static final String ROLES_FIELD = "roles";
        public static final String PASSWORD_FIELD = "password";
        public static final String ADMIN_RESOURCE_MESSAGE =
                "Hello, you have access to a protected resource that requires admin role and read authority.";
        public static final String PASSWORD_VALIDATION_MESSAGE =
                "Must be 8 characters long and combination of uppercase letters, lowercase letters, numbers, special characters.";
        public static final String VALID_PASSWORD = "Admin#123";
        public static final String EMAIL_PREFIX = "admin.";
        public static final String EMAIL_DOMAIN = "@example.com";
        public static final String READ_PRIVILEGE = "READ_PRIVILEGE";
        public static final String WRITE_PRIVILEGE = "WRITE_PRIVILEGE";
        public static final String DELETE_PRIVILEGE = "DELETE_PRIVILEGE";
        public static final String UPDATE_PRIVILEGE = "UPDATE_PRIVILEGE";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";

        public static String bearer(String accessToken) {
            return BEARER_PREFIX + accessToken;
        }
    }
}
