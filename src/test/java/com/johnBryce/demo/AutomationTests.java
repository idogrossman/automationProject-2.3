package com.johnBryce.demo;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.springframework.http.MediaType;

import com.johnBryce.demo.beans.Client;
import com.johnBryce.demo.beans.FoodCoupon;
import com.johnBryce.demo.beans.Restaurant;
import com.johnBryce.demo.enums.Roles;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest // (classes = com.johnBryce.demo.class)
public class AutomationTests {

	private static String adminUserToken = "";
	private static String restaurantUserToken = "";
	private static String clientUserToken = "";
	private static Restaurant restaurants[] = null;
	private static Client clients[] = null;
	private static FoodCoupon[] copuns = null;

	@BeforeEach
	private void initPort() {
		RestAssured.port = 8081;
	}

	// General:
	// 1. Register two clients

	@Order(0)
	@Test
	void RegisterClients() {

		String registerResult = "";

		Client client1 = new Client(0, "", "", null, "first1", "last1", 10);
		Client client2 = new Client(0, "", "", null, "first2", "last2", 20);

		Map<String, Object> headerRegisterClient1 = new HashMap<String, Object>();
		Map<String, Object> headerRegisterClient2 = new HashMap<String, Object>();
		createClientHeader (headerRegisterClient1, "client1", "client1");
		createClientHeader (headerRegisterClient2, "client2", "client2");

		System.out.println("\nStart test Register a clients");
		System.out.println("Register client_1");

		registerResult = RestAssured.given().headers(headerRegisterClient1).body(client1)
				.contentType(MediaType.APPLICATION_JSON_VALUE).post("/register/client").then().statusCode(201).extract()
				.asString();
		System.out.println(registerResult + " :client_1");

		System.out.println("Register client_2");
		registerResult = RestAssured.given().headers(headerRegisterClient2).body(client2)
				.contentType(MediaType.APPLICATION_JSON_VALUE).post("/register/client").then().statusCode(201).extract()
				.asString();
		System.out.println(registerResult + " :client_2");

		System.out.println("End test Register a clients");
	}

	// Admin:
	// 2. Login as admin

	@Order(1)
	@Test
	void adminLogin() {

		Map<String, Object> headerRegisterAdmin = new HashMap<String, Object>();
		headerRegisterAdmin.put("user", "admin");
		headerRegisterAdmin.put("pass", "admin");

		System.out.println("\nStart test Admin Login");
		adminUserToken = RestAssured.given().headers(headerRegisterAdmin).get("/login").then().statusCode(200).extract()
				.asString();
		System.out.println("Token is: " + adminUserToken);
		System.out.println("End test Admin Login");
	}

	@Order(2)
	@Test
	// 3. Register two restaurant

	void registerARestaurant() {

		String registerResult = "";
		Restaurant restaurant1 = new Restaurant();
		Restaurant restaurant2 = new Restaurant();

		createRestaurant(restaurant1, 0, "", "", null, "first1", new ArrayList<FoodCoupon>()); // Anonymous list
		createRestaurant(restaurant2, 0, "", "", null, "first2", new ArrayList<FoodCoupon>());

		Map<String, Object> headerRegisterRestaurant1 = new HashMap<String, Object>();
		Map<String, Object> headerRegisterRestaurant2 = new HashMap<String, Object>();

		createRestaurantHeader(headerRegisterRestaurant1, "restaurant1", "restaurant1", adminUserToken);
		createRestaurantHeader(headerRegisterRestaurant2, "restaurant2", "restaurant2", adminUserToken);

		System.out.println("\nStart test Register a restaurant");
		System.out.println("Register restaurant_1");

		registerResult = RestAssured.given().headers(headerRegisterRestaurant1).body(restaurant1)
				.contentType(MediaType.APPLICATION_JSON_VALUE).post("/register/restaurant").then().statusCode(201)
				.extract().asString();
		System.out.println(registerResult + " restaurant_1");

		System.out.println("Register restaurant_2");
		registerResult = RestAssured.given().headers(headerRegisterRestaurant2).body(restaurant2)
				.contentType(MediaType.APPLICATION_JSON_VALUE).post("/register/restaurant").then().statusCode(201)
				.extract().asString();
		System.out.println(registerResult + " restaurant_2");

		System.out.println("End test Register a restaurant");
	}

	@Order(3)
	@Test
	// 4. Get all restaurants
	void getAllRestaurants() {
		System.out.println("\nStart test get all restaurants");

		restaurants = RestAssured.given().header("token", adminUserToken).get("/admin/getAllRestaurants").then()
				.statusCode(200).extract().as(Restaurant[].class);

		System.out.println("End test get all restaurants");
	}

	@Order(4)
	@Test
	// 5. Delete one of the restaurants
	void deleteRestaurant() {
		System.out.println("\nStart test delete restaurant");
		int counter = 0;

		assertTrue(restaurants != null);
		for (int i = 0; i < restaurants.length; i++)
			if (restaurants[i] != null) {
				counter = i;
				break;
			}

		RestAssured.given().header("token", adminUserToken).body(restaurants[counter])
				.contentType(MediaType.APPLICATION_JSON_VALUE).delete("/admin/deleteRestaurant").then().statusCode(200);

		System.out.println("End test delete restaurant");
		System.out.println("\nStart update restaurants list");

		restaurants = RestAssured.given().header("token", adminUserToken).get("/admin/getAllRestaurants").then()
				.statusCode(200).extract().as(Restaurant[].class);

		System.out.println("End update restaurants list");
	}

	@Order(5)
	@Test
	// 6. Get all clients
	void getAllClients() {
		System.out.println("\nStart test get all clients");

		clients = RestAssured.given().header("token", adminUserToken).get("/admin/getAllClients").then().statusCode(200)
				.extract().as(Client[].class);
		System.out.println("End test get all clients");
	}

	@Order(6)
	@Test
	// Logout as admin
	// 7. Delete one of the clients
	void deleteClient() {
		System.out.println("\nStart test delete client");
		int counter = 0;

		assertTrue(clients != null);
		for (int i = 0; i < clients.length; i++)
			// Checking that the admin user woun't be deleted
			if (clients[i] != null && !clients[i].getUsername().equalsIgnoreCase("admin")) {
				counter = i;
				break;
			}

		RestAssured.given().header("token", adminUserToken).body(clients[counter])
				.contentType(MediaType.APPLICATION_JSON_VALUE).delete("/admin/deleteClient").then().statusCode(200);

		System.out.println(clients[counter].getFirstName());
		System.out.println("End test delete client");
		System.out.println("\nStart update clients list");

		clients = RestAssured.given().header("token", adminUserToken).get("/admin/getAllClients").then().statusCode(200)
				.extract().as(Client[].class);

		System.out.println("End update restaurants list");
	}

	@Order(7)
	@Test
	// 8. Logout as admin
	void adminLogout() {

		System.out.println("\nStart test Admin Logout");
		RestAssured.given().header("token", adminUserToken).get("/logout").then().statusCode(200);
		System.out.println("End test Admin Logout");
	}

	// Restaurant:
	// 1. Login as a restaurant
	@Order(8)
	@Test
	void restaurantLogin() {
		System.out.println("\nStart test restaurant login");
		int counter = 0;

		assertTrue(restaurants != null);
		for (int i = 0; i < restaurants.length; i++)
			if (restaurants[i] != null) {
				counter = i;
				break;
			}

		Map<String, Object> headerRestaurantLogin = new HashMap<String, Object>();
		headerRestaurantLogin.put("user", restaurants[counter].getUsername());
		headerRestaurantLogin.put("pass", restaurants[counter].getPassword());

		restaurantUserToken = RestAssured.given().headers(headerRestaurantLogin).get("/login").then().statusCode(200)
				.extract().asString();

		System.out.println("Restaurant Token: " + restaurantUserToken);
		System.out.println("End test restaurant login");
	}

	@Order(9)
	@Test
	// 2. Add 2 coupon
	void addCopuns() {

		System.out.println("\nStart test add copuns");
		FoodCoupon coupon1 = new FoodCoupon(0, "coupon1", "10% discount on falafel", 0);
		FoodCoupon coupon2 = new FoodCoupon(0, "coupon2", "10% discount on drinks", 0);

		// Add copun1
		RestAssured.given().header("token", restaurantUserToken).body(coupon1)
				.contentType(MediaType.APPLICATION_JSON_VALUE).post("/restaurant/addCoupon").then().statusCode(201);

		// Add copun2
		RestAssured.given().header("token", restaurantUserToken).body(coupon2)
				.contentType(MediaType.APPLICATION_JSON_VALUE).post("/restaurant/addCoupon").then().statusCode(201);
		System.out.println("End test add copuns");

	}

	@Order(10)
	@Test
	// 3. Get all coupons
	void getRestaurantCopuns() {
		System.out.println("\nStart test get restaurant copuns");

		copuns = RestAssured.given().header("token", restaurantUserToken).get("/restaurant/myCoupons").then()
				.statusCode(200).extract().as(FoodCoupon[].class);
		System.out.println("End test get restaurant copuns");
	}

	@Order(11)
	@Test
	// 4. Delete one of the coupons
	void deleteCopun() {
		System.out.println("\nStart test delete copun");
		int counter = 0;

		assertTrue(copuns != null);
		for (int i = 0; i < copuns.length; i++)
			if (copuns[i] != null) {
				counter = i;
				break;
			}

		RestAssured.given().params("id", copuns[counter].getId()).header("token", restaurantUserToken)
				.delete("/restaurant/removeCoupon").then().statusCode(200);

		System.out.println("End test delete copun");

		System.out.println("\nStart update copuns list");
		copuns = RestAssured.given().header("token", restaurantUserToken).get("/restaurant/myCoupons").then()
				.statusCode(200).extract().as(FoodCoupon[].class);
		System.out.println("End update copuns list");
	}

	@Order(12)
	@Test
	// 5. Update restaurant info
	void updateRestaurantInfo() {

		System.out.println("\nStart test update restaurant info");

		Restaurant uRestaurant = new Restaurant();
		uRestaurant.setId(0);
		uRestaurant.setUsername(null);
		uRestaurant.setPassword(null);
		uRestaurant.setRole(null);
		uRestaurant.setName("name1 updated");
		uRestaurant.setCoupons(null);

		RestAssured.given().header("token", restaurantUserToken).body(uRestaurant)
				.contentType(MediaType.APPLICATION_JSON_VALUE).put("/restaurant/update").then().statusCode(200);

		System.out.println("End test update restaurant info");
	}

	@Order(13)
	@Test
	// 6. Get the restaurant info
	void getRestaurantInfo() {

		System.out.println("\nStart test get restaurant info");

		String responseRestaurant;
		responseRestaurant = RestAssured.given().header("token", restaurantUserToken).get("/restaurant/myInfo").then()
				.statusCode(200).extract().response().asString();

		System.out.println("Update is done to the following restaurant: \n" + responseRestaurant);
		System.out.println("End test get restaurant info");
	}

	@Order(14)
	@Test
	// 7. Logout as restaurant
	void logoutAsRestaurant() {
		System.out.println("\nStart test logout as restaurant");
		RestAssured.given().pathParams("token", restaurantUserToken).get("/logout/{token}").then().statusCode(200);
		System.out.println("End test logout as restaurant");
	}

	// Client
	// 1. Login as a client
	@Order(15)
	@Test
	void clientLogin() {
		System.out.println("\nStart test client login");
		int counter = 0;

		assertTrue(clients != null);
		for (int i = 0; i < clients.length; i++)
			if (clients[i] != null && !clients[i].getUsername().equalsIgnoreCase("admin")) {
				counter = i;
				break;
			}

		Map<String, Object> headerClientLogin = new HashMap<String, Object>();
		headerClientLogin.put("user", clients[counter].getUsername());
		headerClientLogin.put("pass", clients[counter].getPassword());

		clientUserToken = RestAssured.given().headers(headerClientLogin).get("/login").then().statusCode(200).extract()
				.asString();

		System.out.println("Client Token: " + clientUserToken);
		System.out.println("End test client login");
	}

	@Order(16)
	@Test
	// 2. Get all coupons
	void getAllCopuns() {

		System.out.println("\nStart test get all copuns");

		RestAssured.given().header("token", clientUserToken).get("/client/allCoupons").then().statusCode(200);

		System.out.println("End test get all copuns");
	}

	@Order(17)
	@Test
	// 3. Buy a coupon
	void buyACopuns() {
		System.out.println("\nStart test buy a copuns");

		int counter = 0;
		assertTrue(copuns != null);
		for (int i = 0; i < copuns.length; i++)
			if (copuns[i] != null) {
				counter = i;
				break;
			}

		RestAssured.given().pathParams("id", copuns[counter].getId()).header("token", clientUserToken)
				.post("/client/buy/{id}").then().statusCode(200);

		System.out.println("End test buy a copuns");
	}

	@Order(18)
	@Test
	// 4. Use a coupon
	void useACopuns() {
		System.out.println("\nStart test use a copuns");

		int counter = 0;

		assertTrue(copuns != null);
		for (int i = 0; i < copuns.length; i++)
			if (copuns[i] != null) {
				counter = i;
				break;
			}

		RestAssured.given().pathParams("id", copuns[counter].getId()).header("token", clientUserToken)
				.delete("/client/use/{id}").then().statusCode(200);

		System.out.println("End test use a copuns");
	}

	@Order(19)
	@Test
	// 5. Update client info
	void updateClientInfo() {
		System.out.println("\nStart test update client info");
		Client uClient = new Client(0, null, null, null, "first1 updated", "last1 updated", 30);

		RestAssured.given().header("token", clientUserToken).body(uClient).contentType(MediaType.APPLICATION_JSON_VALUE)
				.put("/client/update").then().statusCode(200);

		System.out.println("End test update client info");
	}

	@Order(20)
	@Test
	// 6. Get the client info
	void getClientInfo() {
		System.out.println("\nStart test get client info");

		RestAssured.given().header("token", clientUserToken).get("/client/myInfo").then().statusCode(200);

		System.out.println("End test get client info");

	}

	@Order(21)
	@Test
	// 7. Logout as client
	void logoutAsClient() {
		System.out.println("\nStart test logout as client");
		RestAssured.given().pathParams("token", clientUserToken).get("/logout/{token}").then().statusCode(200);
		System.out.println("End test logout as client");
	}

	private void createRestaurant(Restaurant restaurant, int id, String username, String password, Roles roles,
			String name, ArrayList<FoodCoupon> coupon) {
		restaurant.setId(id);
		restaurant.setUsername(username);
		restaurant.setPassword(password);
		restaurant.setRole(roles);
		restaurant.setName(name);
		restaurant.setCoupons(coupon); // Anonymous list
	}

	private void createClientHeader(Map<String, Object> name, String user, String password) {
		name.put("user", user);
		name.put("pass", password);
	}
	
	private void createRestaurantHeader(Map<String, Object> name, String user, String password, String token) {
		name.put("user", user);
		name.put("pass", password);
		name.put("token", token);
	}
}
