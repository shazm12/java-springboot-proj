import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import io.restassured.RestAssured;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {

    @BeforeAll
    static void setUp() {
        RestAssured,basURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOkWithValidToken() {

        String loginPayload = """
                        {
                            "email": "testuser@test.com",
                            "password": "password123"
                        }
                """;

        Response reponse = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token: " + reponse.jsonPath().getString("token"));

    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {

        String loginPayload = """
                        {
                            "email": "invalid@user.com",
                            "password": "wrongpassword"
                        }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);

    }

}
