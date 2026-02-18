/*import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import api.endpoints.UserEndPoints;
import io.restassured.response.Response;

public class UserTests {

	public void setupdata()
	{
		//data=new User();
		
		JSONObject data = new JSONObject();
		data.put("username", "API Testing");
		data.put("firstname", "Sharan");
		data.put("lastname", "Sharan");
		data.put("email", "xyz@gmail.com");
		data.put("password", "India");
		data.put("phone", "1234567");
	}
	
	//@Test(priority=1)
	public void testPostUser()
	{
		
		Response response =UserEndPoints.createUser(data.toString);
		response.then().log().all();
		
		Assert.assertEquals(response.getStatusCode(), 200);
	}
}

 */

