package SimplifyQACodeeditor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.simplifyqa.customMethod.SqaAutowired;
import com.simplifyqa.sqadrivers.webdriver;

import org.json.JSONObject;

public class Sample {

    @SqaAutowired
    public Object web_object;

    /**
     * zipfile method will zip a file and stores the zipped file in the specified
     * location. Sample method to demonstrate custom method creation with string
     * inputs in code editor
     * 
     * @param filepath        path of a file to be zipped
     * @param destzipfilepath destination location for the zipped file to be stored
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean zipfile(String filepath, String destzipfilepath) {
        try {
            File file = new File(destzipfilepath);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry zipentry = new ZipEntry(filepath);
            out.putNextEntry(zipentry);
            out.closeEntry();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * unzip method will unzip a file/directory and stores the unzipped file in the
     * specified location. Sample method to demonstrate custom method creation with
     * string inputs in code editor
     * 
     * @param zipFilePath   path of a zipfile to be unzipped
     * @param destDirectory destination directory location for the unzipped file to
     *                      be stored
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean unzip(String zipFilePath, String destDirectory) throws IOException {
        try {
            File destDir = new File(destDirectory);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                    byte[] bytesIn = new byte[100];
                    int read = 0;
                    while ((read = zipIn.read(bytesIn)) != -1) {
                        bos.write(bytesIn, 0, read);
                    }
                    bos.close();
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * verifyifemployeeexistohrm method will login to OrangeHRM application and
     * verify if specified employee is present. Sample method to demonstrate web
     * based automation custom method creation in code editor
     * 
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean verifyIfEmployeeExistOhrm() {
        try {
            // webdriver is an object for web based driver
            // launchapplication action will launch url in chrome browser
            webdriver.launchapplication("https://opensource-demo.orangehrmlive.com");
            // entertext action will enter the text in object using specified object
            // identifiers
            webdriver.entertext("xpath", "//input[@id='txtUsername']", "Admin");
            webdriver.entertext("xpath", "//input[@id='txtPassword']", "admin123");
            // click action will click in object using specified object identifiers
            webdriver.click("xpath", "//input[@id='btnLogin']");
            webdriver.click("xpath", "//a[@id='menu_pim_viewPimModule']");
            webdriver.click("xpath", "//a[@id='menu_pim_addEmployee']");
            webdriver.entertext("xpath", "//input[@id='empsearch_employee_name_empName']", "Shubhashini");
            webdriver.click("xpath", "//input[@id='searchBtn']");
            // validatetext action will validate the text in object using sepcified object
            // identifiers with the expected data
            webdriver.validatetext("xpath", "//td[text()='No Records Found']", "No Records Found");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * apitest method to execute a REST API for Cryptocurrency data. Sample method
     * to demonstrate API based automation custom method creation in code editor
     * 
     * @param responseBodyRuntime runtime parameter name to store the response
     *                            payload in runtime parameters
     * @param responseCodeRuntime runtime parameter name to store the response code
     *                            in runtime parameters
     * @param responseMsgRuntime  runtime parameter name to store the response
     *                            message in runtime parameters
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean apitest(String responseBodyRuntime, String responseCodeRuntime, String responseMsgRuntime) {
        try {
            // restapi action will execute the Rest API and stores ResponseBody,
            // ResponseCode and ResponseMsg in runtime parameters
            webdriver.restapi("https://www.cryptingup.com/api/markets", "GET",
                    "{\"Content-Type\":\"application/json\"}", "", "", responseBodyRuntime, responseCodeRuntime,
                    responseMsgRuntime);
            // getfromruntime will get the stored parameter data to the current execution
            webdriver.getfromruntime(responseCodeRuntime);
            webdriver.validateparameter(responseCodeRuntime, "200");
            webdriver.getfromruntime(responseBodyRuntime);
            webdriver.validatefromjson(responseBodyRuntime, "markets[0]/exchange_id", "BINANCE");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean regularexpression(String actualstring, String expectedpattern) {
        Pattern pattern = Pattern.compile(expectedpattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(actualstring);
        boolean matchFound = matcher.find();
        if (matchFound) {
            System.out.println("Match found");
        } else {
            System.out.println("Match not found");
        }
        return true;
    }

    /**
     * createjson method will create a JSON with two values and stores the JSON in
     * runtime parameter. Sample method to demonstrate custom method creation with
     * string and integer inputs in code editor
     * 
     * @param str          String value
     * @param num          Integer value
     * @param runtimeparam Runtime parameter name to store the created JSON in
     *                     runtime parameters
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean createjson(String str, int num, String runtimeparam) {
        try {
            JSONObject jsonobj = new JSONObject();
            jsonobj.put("name", str);
            jsonobj.put("age", num);
            // storeruntime will store the specified data in runtime parameters with the
            // specified parameter name
            webdriver.storeruntime(runtimeparam, jsonobj.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * findmedian method will find a median of numbers. Sample method to demonstrate
     * custom method creation with integer inputs in code editor
     * 
     * @param numbers
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean findmedian(int... numbers) {
        try {
            double median = 0;
            int[] numbStr = new int[numbers.length - 1];
            for (int i = 0; i < numbers.length - 1; i++)
                numbStr[i] = numbers[i];
            for (int temp : numbStr) {
                if (temp % 2 == 1) {
                    median = numbStr[(temp + 1) / 2 - 1];
                } else {
                    median = (numbStr[temp / 2 - 1] + numbStr[temp / 2]) / 2;
                }
            }
            System.out.println("Median :" + median);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * averageoftwonumbers method will calculate average of two numbers. Sample
     * method to demonstrate custom method creation with float inputs in code editor
     * 
     * @param num1
     * @param num2
     * @param runtimeparam Runtime parameter name to store the calculated average in
     *                     runtime parameters
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean averageoftwonumbers(float num1, float num2, String runtimeparam) {
        try {
            float sum = 0;
            float avg = 0;
            sum = num1 + num2;
            avg = sum / 2;
            webdriver.storeruntime(runtimeparam, String.valueOf(avg));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    /**
     * jsonmanipulation method will store the value of specified key and validates
     * its value Sample method to demonstrate custom method creation for json
     * manipulation with SimplifyQA in-built methods
     * 
     * @param json
     * @param key
     * @param runtimeparam    Runtime parameter name to store the calculated average
     *                        in runtime parameters
     * @param valuetovalidate value to be validated
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean jsonmanipulation(String json, String key, String runtimeparam, String valuetovalidate) {
        try {
            webdriver.storejsonkey(json, key, runtimeparam);
            webdriver.getfromruntime(runtimeparam);
            webdriver.validateparameter(runtimeparam, valuetovalidate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * browsersample method launches the specified URL in specified browser Sample
     * method to demonstrate custom method creation to launch application in
     * specified browser
     * 
     * @param url         URL of the application to be launched
     * @param browsername Specified browser
     * @return Boolean value True/False based on success of this function
     * @author SimplifyQA
     */
    public boolean browsersample(String url, String browsername) {
        try {
            webdriver.launchapplication(url, browsername);
            webdriver.click("xpath", "//a[text()='SIGN IN']");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}

