package apitests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


import static io.restassured.RestAssured.*;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.*;

public class APITests {
    static String username;
    static String password;

    @BeforeTest
    @Test
    public void registerUser(){


        // new logic for a registration

        Date date = new Date();

        RegisterPOJO register = new RegisterPOJO();
        baseURI = "http://training.skillo-bg.com:3100";

        register.setUsername("Marti" +  date.getTime());
        username = register.getUsername();
        register.setEmail("M" + date.getTime() +  "@b.b");
        register.setBirthDate("12.22.1993");
        register.setPassword("A931125z");
        password = register.getPassword();
        register.setPublicInfo("Lel");

        Response response = given()
                .config(RestAssured.config().encoderConfig(encoderConfig().encodeContentTypeAs("application-json", ContentType.TEXT)))
                .header("Content-Type", "application/json")
                .body(register)
                .when()
                .post("/users");
        response
                .then()
                .log()
                .all()
                .statusCode(201);

    }

    static String loginToken;
    static Integer userId;
    static Integer postId;
    static Integer commentId;
    static String commentContent;



    @Test  (dependsOnMethods = "registerUser")
    public void loginTest() throws IOException {
        // create new LoginPOJO class object named login
        LoginPOJO login = new LoginPOJO();

//        FileReader reader = new FileReader("credentials.properties"); // reading from the file
//        Properties = new Properties();
//
//        properties.load(reader);
//
//        System.out.println((properties.get("user")));
//        System.out.println((properties.get("password")));

        // set the login credentials to our login object
        login.setUsernameOrEmail(username);
        login.setPassword(password);

        // Convert pojo object to json using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        String convertedJson = objectMapper.writeValueAsString(login);
        System.out.println("CONVERTED JSON IS: ");
        System.out.println(convertedJson);

        baseURI = "http://training.skillo-bg.com:3100";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(login)
                .when()
                .post("/users/login");
        response
                .then()
                .log()
                .all()
                .statusCode(201);

        // convert the response body json into a string
        String loginResponseBody = response.getBody().asString();


        loginToken = JsonPath.parse(loginResponseBody).read("$.token");
        System.out.println(loginToken);

        userId = JsonPath.parse(loginResponseBody).read("$.user.id");

    }

    @AfterTest
    @Test (priority = 3)
    public void deleteUser(){

        given()
                .header("Content-Type","application/json")
                .header("Authorization","Bearer " + loginToken)
                .when()
                .delete("/users/"+ userId)
                .then()
                .log()
                .all()
                .statusCode(200);
    }

    @AfterTest
    @Test (dependsOnMethods = "deleteUser")
    public void loginTestAfterDeletion() throws IOException {
        // create new LoginPOJO class object named login
        LoginPOJO login = new LoginPOJO();

        // set the login credentials to our login object
        login.setUsernameOrEmail(username);
        login.setPassword(password);

        // Convert pojo object to json using Jackson
        ObjectMapper objectMapper = new ObjectMapper();


        baseURI = "http://training.skillo-bg.com:3100";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(login)
                .when()
                .post("/users/login");
        response
                .then()
                .log()
                .all()
                .statusCode(201)
                .assertThat().body("user.isDeleted", equalTo(true));

    }


    @Test (priority = 1)

    public void addPost(){

        ActionsPOJO createPost = new ActionsPOJO();

        createPost.setCaption("Hello");
        createPost.setPostStatus("public");
        createPost.setCoverUrl("hur-hur");
        ValidatableResponse validatableResponse =
                given()
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + loginToken)
                        .body(createPost)
                        .log()
                        .all()
                        .when()
                        .post("/posts")
                        .then()
                        .log()
                        .all()
                        .statusCode(201)
        .assertThat().body("caption",equalTo(createPost.getCaption()));
        postId = validatableResponse.extract().path("id");
    }

    @Test (priority = 1)
    public void likePost() {
        // create an object of ActionsPOJO class and add value for the fields
        ActionsPOJO likePost = new ActionsPOJO();
        likePost.setAction("likePost");

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + loginToken)
                .body(likePost)
                .when()
                .patch("/posts/"+ postId)
                .then()
                .body("post.id", equalTo(postId))
                .log()
                .all()
                .statusCode(200);

    }



    @Test (priority = 1, dependsOnMethods = "addPost")
    public void commentPost() {
        ActionsPOJO commentPost = new ActionsPOJO();
        commentPost.setContent("My New Comment!");

        ValidatableResponse validatableResponse =
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + loginToken)
                .body(commentPost)
                .when()
                .post("/posts/" + postId + "/comment")
                .then()
                .body("content", equalTo("My New Comment!"))
                .log()
                .all()
                .statusCode(201);
                commentId = validatableResponse.extract().path("id");
                commentContent = commentPost.getContent();
    }


    @Test (priority = 1, dependsOnMethods = {"addPost", "commentPost"})
    public void getCommentsForPost(){
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + loginToken)
                .when()
                .get("/posts/" + postId + "/comments")
                .then()
                .log()
                .all()
                .statusCode(200)
                .assertThat().body("content",contains(commentContent));
    }

    @Test (priority =2, dependsOnMethods = "likePost")
    public void getPostById(){
                given()
                        .header("Content-Type", "application/json")
                        .header("Authorization","Bearer " + loginToken)
                        .queryParam("id",postId)
                        .when()
                        .get("/posts/" + postId)
                        .then()
                        .log()
                        .all()
                        .statusCode(200)
                        .assertThat().body("likesCount",greaterThanOrEqualTo(1));


    }
    @Test (priority =1)
    public void getPostByIdBeforeLike(){
                given()
                        .header("Content-Type", "application/json")
                        .header("Authorization","Bearer " + loginToken)
                        .queryParam("id",postId)
                        .when()
                        .get("/posts/" + postId)
                        .then()
                        .log()
                        .all()
                        .statusCode(200)
                        .assertThat().body("likesCount",equalTo(0));


    }
    @Test (priority = 1)
    public void getPosts(){

        given()
                .header("Content-Type", "application/json")
                .header("Authorization","Bearer " + loginToken)
                .queryParam("take",10)
                .queryParam("skip", 0)
                .when()
                .get("/posts")
                .then()
                .log()
                .all()
                .statusCode(200);

        }



    @Test (priority = 1,dependsOnMethods = "addPost")
    public void deletePost(){

        ActionsPOJO deletePost = new ActionsPOJO();
        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + loginToken)
                .body(deletePost)
                .when()
                .delete("/posts/" + postId)
                .then()
                .log()
                .all()
                .statusCode(200)
                .assertThat().body("msg",equalTo("Post was deleted!"));

    }
}
