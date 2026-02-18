package api.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import api.endpoints.UserEndPoints;
import api.payloads.User;
import api.utilities.DataProviders;
import io.restassured.response.Response;



public class DDTests {
	
	
	public Logger logger;//for logs

	@BeforeClass
	public void setup()
		{
			//logs
		logger=LogManager.getLogger(this.getClass());
		}


	
	@Test(priority=1, dataProvider="Data", dataProviderClass=DataProviders.class)
	public void testPostUser(String UserId, String UserName, String fname, String lname, String useremail, String pwd, String ph)
	{
		
		logger.info("****** Creating User********");
		
		
		User userPayload = new User();
		userPayload.setId(Integer.parseInt(UserId));
		userPayload.setUsername(UserName);
		userPayload.setFirstName(fname);
		userPayload.setLastName(lname);
		userPayload.setEmail(useremail);
		userPayload.setPassword(pwd);
		userPayload.setPhone(ph);
		
		Response response=UserEndPoints.createUser(userPayload);
		Assert.assertEquals(response.getStatusCode(), 200);
		
		logger.info("********User Created***********");
	}
	
	@Test(priority=2, dataProvider="UserNames", dataProviderClass=DataProviders.class)
	public void testGetUser(String userName)
	{
		
		logger.info("*************Getting user*************");
		
		Response response=UserEndPoints.getUser(userName);
		Assert.assertEquals(response.getStatusCode(), 200);
		
		logger.info("*************Got user Info*************");
	}
	
	@Test(priority=3, dataProvider="UserNames", dataProviderClass=DataProviders.class)
	public void testDeleteUser(String userName)
	{
		logger.info("*************Deleting user*************");
		
		Response response=UserEndPoints.deleteUser(userName);
		Assert.assertEquals(response.getStatusCode(), 200);
		
		logger.info("*************User Deleted*************");
		logger.info("************CRUD Successful***************");

}
