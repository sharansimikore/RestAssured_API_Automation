package api.utilities;

import java.io.IOException;

import org.testng.annotations.DataProvider;

public class DataProviders {
	
	@DataProvider(name = "Data")
	public String[][] getData() throws IOException {

	    String path = System.getProperty("user.dir") + "//testData//userdata.xlsx";
	    XLUtility xl = new XLUtility(path);

	    int rows = xl.getRowCount("Sheet1");
	    int cols = xl.getCellCount("Sheet1", 1);

	    String[][] apidata = new String[rows][cols];

	    for (int i = 1; i <= rows; i++) {
	        for (int j = 0; j < cols; j++) {
	            apidata[i - 1][j] = xl.getCellData("Sheet1", i, j);
	        }
	    }

	    return apidata;
	}

	
		
	@DataProvider(name = "UserNames")
	public String[] getUserNames() throws IOException {

	    String path = System.getProperty("user.dir") + "//testData//userdata.xlsx";
	    XLUtility xl = new XLUtility(path);

	    int rows = xl.getRowCount("Sheet1");
		    

	    String[] apidata = new String[rows];

	    for (int i = 1; i <= rows; i++)
    	{
	    	apidata[i - 1] = xl.getCellData("Sheet1", i, 1);
		}
		    

		    return apidata;
		}
}

