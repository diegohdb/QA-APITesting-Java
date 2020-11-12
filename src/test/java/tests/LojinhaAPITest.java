package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LojinhaAPITest {

    private String token;
    private int productId, componentId;

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = "http://165.227.93.41";
        RestAssured.basePath = "lojinha";

        token = RestAssured.
                given().
                    contentType(ContentType.JSON).
                    body("{\n" +
                            " \"usuariologin\": \"test\",\n" +
                            " \"usuariosenha\": \"test123\"\n" +
                            "}").
                when().
                    post("login").
                then().
                    extract().
                    path("data.token");

        productId = RestAssured
                .given()
                    .header("token", token)
                    .contentType(ContentType.JSON)
                    .body("{\n" +
                        " \"produtonome\": \"Product Added\",\n" +
                        " \"produtovalor\": 1000.00,\n" +
                        " \"produtocores\": [\n" +
                        " \"black,white\"\n" +
                        " ],\n" +
                        " \"componentes\": [\n" +
                        " {\n" +
                        " \"componentenome\": \"Component Added\",\n" +
                        " \"componentequantidade\": 10\n" +
                        " }\n" +
                        " ]\n" +
                        "}")
                .when()
                   .post("produto")
                .then()
                    .extract()
                    .path("data.produtoid");

        componentId = RestAssured
                        .given()
                            .header("token", token)
                        .when()
                            .get("produto/"+productId)
                        .then()
                            .extract()
                            .path("data.componentes[0].componenteid");
    }


    @After
    public void tearDown() throws Exception {
        RestAssured
                .given()
                    .header("token", token)
                .when()
                    .delete("produto/"+productId)
                .then()
                    .assertThat().statusCode(204);
    }

    @Test
    public void testGetProduct() {

         RestAssured
            .given()
                 .header("token", token)
            .when()
                .get("produto/"+productId)
            .then()
                .assertThat().statusCode(200)
                .body("data.produtonome", Matchers.equalTo("Product Added"))
                .body("data.componentes[0].componentenome", Matchers.equalTo("Component Added"))
                .body("message", Matchers.equalTo("Detalhando dados do produto"));
    }

    @Test
    public void testEditProduct() {
        RestAssured
                .given()
                    .header("token", token)
                    .contentType(ContentType.JSON)
                    .body("{\n" +
                            " \"produtonome\": \"Product Edited\",\n" +
                            " \"produtovalor\": 2000.00,\n" +
                            " \"produtocores\": [\n" +
                            " \"black,white\"\n" +
                            " ],\n" +
                            " \"componentes\": [\n" +
                            " {\n" +
                            " \"componentenome\": \"Component Edited\",\n" +
                            " \"componentequantidade\": 20\n" +
                            " }\n" +
                            " ]\n" +
                            "}")
                .when()
                    .put("produto/"+productId)
                .then()
                    .assertThat().statusCode(200)
                    .body("data.produtonome", Matchers.equalTo("Product Edited"))
                    .body("data.produtovalor", Matchers.equalTo(2000))
                    .body("data.componentes[0].componentenome", Matchers.equalTo("Component Edited"))
                    .body("message", Matchers.equalTo("Produto alterado com sucesso"));
    }

    @Test
    public void testGetComponent() {
        RestAssured
                .given()
                    .header("token", token)
                .when()
                    .get("produto/"+productId+"/componente/"+componentId)
                .then()
                    .assertThat().statusCode(200)
                    .body("data.componenteid", Matchers.equalTo(componentId))
                    .body("data.componentenome", Matchers.equalTo("Component Added"))
                    .body("data.componentequantidade", Matchers.equalTo(10))
                    .body("message", Matchers.equalTo("Detalhando dados do componente de produto"));
    }

    @Test
    public void testAddNewComponent(){
        RestAssured
            .given()
                .header("token", token)
                .contentType(ContentType.JSON)
                .body("{\n" +
                        " \"componentenome\": \"New Component\",\n" +
                        " \"componentequantidade\": 5\n" +
                        "}")
            .when()
                .post("produto/"+productId+"/componente")
            .then()
                .assertThat().statusCode(201)
                .body("data.componentenome", Matchers.equalTo("New Component"))
                .body("data.componentequantidade", Matchers.equalTo(5))
                .body("message", Matchers.equalTo("Componente de produto adicionado com sucesso"));
    }

    @Test
    public void testEditComponent() {
        RestAssured
            .given()
                .header("token", token)
                .contentType(ContentType.JSON)
                .body("{\n" +
                        " \"componentenome\": \"New Component Edited\",\n" +
                        " \"componentequantidade\": 50\n" +
                        "}")
            .when()
                .put("produto/"+productId+"/componente/"+componentId)
            .then()
                .assertThat().statusCode(200)
                .body("data.componenteid", Matchers.equalTo(componentId))
                .body("data.componentenome", Matchers.equalTo("New Component Edited"))
                .body("data.componentequantidade", Matchers.equalTo(50))
                .body("message", Matchers.equalTo("Componente de produto alterado com sucesso"));
    }

    @Test
    public void testDeleteComponent(){

        int newComponentId = RestAssured
                .given()
                    .header("token", token)
                    .contentType(ContentType.JSON)
                    .body("{\n" +
                            " \"componentenome\": \"New Component\",\n" +
                            " \"componentequantidade\": 5\n" +
                            "}")
                .when()
                    .post("produto/"+productId+"/componente")
                .then()
                    .assertThat().statusCode(201)
                    .extract()
                    .path("data.componenteid");

        RestAssured
                .given()
                    .header("token", token)
                    .contentType(ContentType.JSON)

                .when()
                    .delete("produto/"+productId+"/componente/"+newComponentId)
                .then()
                    .assertThat().statusCode(204);

    }
}
