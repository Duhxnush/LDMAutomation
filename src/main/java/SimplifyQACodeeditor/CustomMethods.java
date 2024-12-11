package SimplifyQACodeeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.microsoft.schemas.office.visio.x2012.main.PagesDocument;
import com.simplifyqa.Utility.HttpUtility;
import com.simplifyqa.Utility.KeyBoardActions;
import com.simplifyqa.customMethod.SqaAutowired;
import com.simplifyqa.method.GeneralMethod;
import com.simplifyqa.sqadrivers.webdriver;
import com.simplifyqa.web.implementation.DriverOption;
import com.simplifyqa.web.implementation.WebAutomationManager;

import org.apache.commons.httpclient.util.TimeoutController.TimeoutException;
import org.apache.poi.xddf.usermodel.text.TabAlignment;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.json.JSONArray;
import org.json.JSONObject;

public class CustomMethods {

    public boolean fileupload(String path) {
        try {
          String value = ((JsonNode)webdriver.getCurrentObject().getAttributes().get(0)).get("value").asText();
          String id = webdriver.findElement("xpath", value);
          webdriver.elementSendkeys(id, path);
          return true;
        } catch (Exception e) {
          return false;
        } 
      }

      public static void parseDocx(String filePath, String outputFilePath,String outputruntime) throws IOException {
        try{ 
            FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument doc = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
             Writer writer = new OutputStreamWriter(new FileOutputStream(outputFilePath)); 
            writer.write(extractor.getText());
            webdriver.storeruntime(outputruntime, extractor.getText());
            writer.close();
            extractor.close();
        }
        catch(Exception e)
        {
     
            }
        }
     


      public boolean isStringPresentInWordFile(String filePath, String searchString) {

        try {
            FileInputStream fis = new FileInputStream(filePath);
            XWPFDocument doc = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String fileContent=extractor.getText();
            String normalizedFileContent = fileContent.replaceAll("\\s+", "");
            String normalizedSearchString = searchString.replaceAll("\\s+", "");

            return Pattern.compile(Pattern.quote(normalizedSearchString), Pattern.CASE_INSENSITIVE)
                    .matcher(normalizedFileContent)
                    .find();
        } catch (IOException e) {
            return false;
        }       
    }

    public static String getFirstJsonValue(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            JsonNode firstValue = jsonNode.elements().next();
            return firstValue.toString();
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

	public String shadowScriptBuilder(String shadowtree,String child){
        try {
            String script="";
            String[] shadows=shadowtree.split(";");
            script+="document.querySelector(\""+shadows[0]+"\")";
            for(int i=1;i<shadows.length;i++){
                if(shadows[i].contains("[") && shadows[i].contains("]") && !shadows[i].split("\\[")[1].contains("=")){
                    int index=Integer.parseInt(shadows[i].split("\\[")[1].split("\\]")[0]);
                    script+=".shadowRoot.querySelectorAll(\""+shadows[i].split("\\[")[0]+"\")["+index+"]";
                }
                else{
                    script+=".shadowRoot.querySelector(\""+shadows[i]+"\")";
                }
                
            }
            // script+=".shadowRoot.querySelector(\""+child+"\")"; 
            if(child.equals("slot")){
                script+=".assignedNodes()";
            }          
            if(child.contains("[") && child.contains("]") && !child.split("\\[")[1].contains("=")){
                int index=Integer.parseInt(child.split("\\[")[1].split("\\]")[0]);
                script+=".shadowRoot.querySelectorAll(\""+child.split("\\[")[0]+"\")["+index+"]";
            }
            else{
                script+=".shadowRoot.querySelector(\""+child+"\")";
            }
            return script;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject filterSlot(String script,String tagName){
        JSONObject jo=new JSONObject();
        jo=webdriver.executeScript2("return Array.from("+script+").filter(node => {return node.nodeType === Node.ELEMENT_NODE && node.tagName.toLowerCase() === '"+tagName+"';})");
        return jo;
    }

    public boolean getShadowCellData(String shadowtree,String child, String indexRow,String indexCol,String runtime){
        try {
            String tablerow="sb-table-row";
            String script=shadowScriptBuilder(shadowtree, child);
            int length=Integer.parseInt(webdriver.executeScript2("return "+script+".length").get("value").toString());
            JSONObject jo=filterSlot(script, tablerow);
            for(int i=0;i<length;i++){
                String rowScript=script+"["+i+"].getAttribute(\"index\")";
                int rowIndex=-5;
                try {
                    rowIndex=Integer.parseInt(webdriver.executeScript2("return "+rowScript).get("value").toString());
                } catch (Exception e) {
                    //TODO: handle exception
                }
                if(rowIndex==Integer.parseInt(indexRow)){
                    String cellscript=script+"["+i+"]";
                    cellscript+=".shadowRoot.querySelector(\"sb-swipe-container\").shadowRoot.querySelector(\"slot\").assignedNodes()[1].querySelectorAll(\"sb-group\")[1].shadowRoot.querySelectorAll(\"sb-table-cell\")["+
                    indexCol+"].shadowRoot.querySelector(\"sb-field\").shadowRoot.querySelector(\"span\").innerText";
                    String celldata=webdriver.executeScript2(cellscript).getString("value").toString();
                    GeneralMethod gm = new GeneralMethod();
                    int[] array = {1};
                    String[] value = gm.runtimeparameter(array);
                    for (int j = 0; j < value.length; j++) {
                        runtime = value[j];
                        webdriver.storeruntime(runtime, celldata);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean shadowelementclick(String shadowtree,String child){
        try {
            String script=shadowScriptBuilder(shadowtree, child)+".click()";
            JSONObject jo=webdriver.executeScript2("return "+script);
            Thread.sleep(3000);
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean shadowelemententertext(String shadowtree,String child, String value){
        try {
            String script=shadowScriptBuilder(shadowtree, child);
            JSONObject jo=webdriver.executeScript2("return "+script);
            String id=jo.get("value").toString().split(":")[1].split("}")[0].replaceAll("\"","");
            webdriver.elementSendkeys(id, value);
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean shadowelementtext(String shadowtree,String child,String runtime){
        try {
            String script=shadowScriptBuilder(shadowtree, child)+".innerText";
            JSONObject jo=webdriver.executeScript2("return "+script);
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            GeneralMethod gm = new GeneralMethod();
            int[] array ={2};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, jo.get("value").toString());
            }
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean scrollTillEnd(String stopXpath){
        try{
            String xpath=webdriver.getCurrentObject().getAttributes().get(0).get("value").asText();
            String script="var element = document.evaluate(\""+xpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;return element.scrollHeight";
            int height=Integer.parseInt(webdriver.executeScript2(script).get("value").toString());
            for(int i=0;i<height;i+=100){
                String increment="var element = document.evaluate(\""+xpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;return element.scrollTop="+i+";";
                webdriver.executeScript(increment);
                height=Integer.parseInt(webdriver.executeScript2(script).get("value").toString());
                String eleID=webdriver.findElement("xpath", stopXpath);
                if(!eleID.contains("no such element")){
                    return true;
                }
            }
            return true;
        }
        catch(Exception e){
            return false;
        }
    }

    public boolean scrollDocument1(){
        try {
            String stopXpath=webdriver.getCurrentObject().getAttributes().get(0).get("value").asText();
            int height=Integer.parseInt(webdriver.executeScript2("return document.scrollingElement.scrollHeight").get("value").toString());
            for(int i=0;i<height;i+=100){
                String increment="document.scrollingElement.scrollTop="+i;
                webdriver.executeScript(increment);
                height=Integer.parseInt(webdriver.executeScript2("return document.scrollingElement.scrollHeight").get("value").toString());
                String eleID=webdriver.findElement("xpath", stopXpath);
                if(!eleID.contains("no such element")){
                    return true;
                }
            }
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean ValidateTwoParameters(String val1, String val2){
        try {
            return val1.equals(val2);
        } catch (Exception e) {
            return false;
        }
    }

    public int getRow(String value){
        try {
            int col=getCol("PRODUCT NAME");
            String script="document.querySelector(\"sb-page-container\").shadowRoot.querySelector(\"sb-line-editor\").shadowRoot.querySelector(\"sb-le-group-layout\").shadowRoot.querySelector(\"sb-le-group:not(.hiddenGroup)\").shadowRoot.querySelector(\"sb-tabs\").shadowRoot.querySelector(\"sb-group-tabs\").shadowRoot.querySelector(\"sf-standard-table\").shadowRoot.querySelectorAll(\"sf-le-table-row\")";
            JSONObject jo=webdriver.executeScript2("return "+script);
            int length=((JSONArray)jo.get("value")).length();
            for(int i=0;i<length;i++){
                String nameScript=script+"["+i+"].shadowRoot.querySelectorAll(\"div.td\")[3].innerText";
                String text=webdriver.executeScript2("return "+nameScript).get("value").toString();
                if(text.equals(value)){
                    return i;
                }
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public int getCol(String header){
        try {
            String script="document.querySelector('sb-page-container').shadowRoot.querySelector('sb-line-editor').shadowRoot.querySelector('sb-le-group-layout').shadowRoot.querySelector('sb-le-group:not(.hiddenGroup)').shadowRoot.querySelector('sb-tabs').shadowRoot.querySelector('sb-group-tabs').shadowRoot.querySelector('sf-standard-table').shadowRoot.querySelector('sf-le-table-header').shadowRoot.querySelectorAll('div.th')";
            JSONObject jo= webdriver.executeScript2("return "+script);
            int length=((JSONArray)jo.get("value")).length();
            for(int i=0;i<length;i++){
                String nameScript=script+"["+i+"].innerText";
                String text=webdriver.executeScript2("return "+nameScript).get("value").toString();
                if(text.equals(header)){
                    return i;
                }
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
        
    }

    public String formatdecimal(Double val){
        DecimalFormat f = new DecimalFormat("0.00");
        return f.format(val);
    }

    public String formatdecimal1(Double val){
        DecimalFormat f = new DecimalFormat("#.##");
        return f.format(val);
    }

    public boolean addValues(String xpath, String runtime){
        try {
            int len=webdriver.findElements("xpath", xpath).length();
            double sum=0;
          for(int i=1;i<=len;i++){
                sum+=Double.parseDouble(webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"),"innerText").replaceAll(",", ""));
            }
            GeneralMethod gm = new GeneralMethod();
            int[] array = {1};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                gm.storeruntime(runtime, formatdecimal(sum));
            }
            return true;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }

    public String buildScript(int row, int col){
        return "document.querySelector(\"sb-page-container\").shadowRoot.querySelector(\"sb-line-editor\").shadowRoot.querySelector(\"sb-le-group-layout\").shadowRoot.querySelector(\"sb-le-group:not(.hiddenGroup)\").shadowRoot.querySelector(\"sb-tabs\").shadowRoot.querySelector(\"sb-group-tabs\").shadowRoot.querySelector(\"sf-standard-table\").shadowRoot.querySelectorAll(\"sf-le-table-row\")["+row+"].shadowRoot.querySelectorAll(\"div.td\")["+col+"]";
    }

    public boolean entertexWithOneParams(String replacevalue1, String ValueTOEnter) {
        try {
          String xpath = getUnique();
          xpath = xpath.replaceAll("#replace", replacevalue1);
          return webdriver.elementSendkeys(webdriver.findElement("xpath", xpath), ValueTOEnter);
        } catch (Exception e) {
          e.printStackTrace();
          return false;
        } 
      }

    public boolean entertextbyname(String value, String header, String text){
        try {
            int row=getRow(value);
            int col=getCol(header);
            if(row==-1 || col==-1){
                return false;
            }
            JSONObject j2=webdriver.executeScript2("return "+buildScript(row,col));
            String element=j2.get("value").toString().split(":")[1].split("}")[0].replaceAll("\"","");
            JSONObject jx=webdriver.executeScript2("return document.querySelector(\"body\")");
            String elementx=jx.get("value").toString().split(":")[1].split("}")[0].replaceAll("\"","");
            webdriver.moveToElement(elementx);
            webdriver.moveToElement(element);
            String script=buildScript(row,col)+".querySelector(\".pencil\").click()";
            JSONObject j1=webdriver.executeScript2("return "+script);
            if(j1.get("status").toString().equals("500")){
                return false;
            }
            script=buildScript(row,col)+".querySelector(\"sb-textarea, sb-input\").shadowRoot.querySelector(\"input,textarea\")";
            JSONObject jo=webdriver.executeScript2("return "+script);
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            String id=jo.get("value").toString().split(":")[1].split("}")[0].replaceAll("\"","");
            Thread.sleep(3000);
            webdriver.elementClear(id);
            webdriver.elementSendkeys(id, KeyBoardActions.ENTER.value());
            Thread.sleep(3000);
            script=buildScript(0,0)+".click()";
            Thread.sleep(3000);
            webdriver.movetoelement("xpath", "(//*)[1]");
            webdriver.moveToElement(element);
            script=buildScript(row,col)+".querySelector(\".pencil\").click()";
            j1=webdriver.executeScript2("return "+script);
            if(j1.get("status").toString().equals("500")){
                return false;
            }
            script=buildScript(row,col)+".querySelector(\"sb-textarea, sb-input\").shadowRoot.querySelector(\"input,textarea\")";
            jo=webdriver.executeScript2("return "+script);
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            id=jo.get("value").toString().split(":")[1].split("}")[0].replaceAll("\"","");
            webdriver.elementSendkeys(id, text);
            webdriver.elementSendkeys(id, KeyBoardActions.ENTER.value());
            Thread.sleep(3000);
            script=buildScript(0,0)+".click()";
            Thread.sleep(3000);
            webdriver.executeScript2("return "+script);
            Thread.sleep(3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean downloadonefile(String url) {
        try {
          webdriver.opennewtabwithurl(url);
          return true;
        } catch (Exception e) {
          return false;
        } 
      }

      public boolean downloadPDF(String urlString,String runtime) throws IOException {
          try {
            String downloadDir=webdriver.getUserPreference("download.path");
            URL url = new URL(urlString);
    
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
    
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to connect to URL: " + connection.getResponseMessage());
            }
    
            File dir = new File(downloadDir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Failed to create directory: " + downloadDir);
                }
            }
    
            String fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
            File outputFile = new File(dir, fileName);
    
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
    
            String filepath=outputFile.getAbsolutePath();
    
            GeneralMethod gm = new GeneralMethod();
            int[] array = {1};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime,filepath);
            }
    
            return true;
          } catch (Exception e) {
              //TODO: handle exception
              return false;
          }

        
    }

    public boolean scrollandstop(String xpath,String header) {
        try {
            int length=webdriver.findElements("xpath", xpath).length();
            int i=0;
            for(i=1;i<=length;i++){
                String headerText=webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"), "innerText");
                if(headerText.equals(header)){
                    break;
                }
            }
            String innerText=webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"), "innerText");
            return true;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }

    public boolean removeDuplicateProducts(){
        try {
            String navbar="document.querySelector(\"#sbPageContainer\").shadowRoot.querySelector(\"#content > sb-line-editor\").shadowRoot.querySelector(\"#groupLayout\").shadowRoot.querySelectorAll(\".nav-item\")";
            String tableRow="document.querySelector(\"#sbPageContainer\").shadowRoot.querySelector(\"#content > sb-line-editor\").shadowRoot.querySelector(\"#groupLayout\").shadowRoot.querySelector(\"#Group_2\").shadowRoot.querySelector(\"#groupTabs\").shadowRoot.querySelector(\"#pages > div > sb-group-tabs\").shadowRoot.querySelector(\"sf-standard-table\").shadowRoot.querySelectorAll(\"sf-le-table-row\")";
            String navsize=webdriver.executeScript2("return "+navbar+".length").get("value").toString();
            int navItem=Integer.parseInt(navsize);
            for(int i=0;i<navItem;i++){
                String groupClick="document.querySelector(\"#sbPageContainer\").shadowRoot.querySelector(\"#content > sb-line-editor\").shadowRoot.querySelector(\"#groupLayout\").shadowRoot.querySelector(\"#panel\").shadowRoot.querySelector(\"span > div.closedArrows.container\").click()";
                webdriver.executeScript("return "+groupClick);
                Thread.sleep(3000);
                webdriver.executeScript("return "+navbar+"["+i+"].click()");
                Thread.sleep(3000);
                webdriver.executeScript("return "+groupClick);
                String rowSize=webdriver.executeScript2("return "+tableRow+".length").get("value").toString();
                int rowItem=Integer.parseInt(rowSize);
                for(int j=0;j<rowItem;j++){
                    String check=webdriver.executeScript2("return "+tableRow+"["+j+"].shadowRoot.querySelectorAll(\"img\").length").get("value").toString();
                    if(check.equals("1")){
                        webdriver.executeScript2("return "+tableRow+"["+j+"].shadowRoot.querySelector('.paper-checkbox').click()");
                    }
                }
                Thread.sleep(5000);
                webdriver.executeScript("return document.querySelector(\"#sbPageContainer\").shadowRoot.querySelector(\"#content > sb-line-editor\").shadowRoot.querySelector(\"#actions > sb-custom-action:nth-child(4)\").shadowRoot.querySelector(\"#mainButton\").click()");
                Thread.sleep(3000);
                webdriver.executeScript("return document.querySelector(\"#sbPageContainer\").shadowRoot.querySelector(\"#content > sb-line-editor\").shadowRoot.querySelector(\"#actions > sb-custom-action:nth-child(10)\").shadowRoot.querySelector(\"#mainButton\").click()");
            }   
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean dynamicstoreandreplace(String query, String replace, String runtime){
        try {
            query=query.replaceAll("#replace", replace);
            GeneralMethod gm = new GeneralMethod();
            int[] array = {2};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime,query);
            }

            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean getQuoteId(String runtime){
        try {
            String quoteId=webdriver.geturl().split("\\/")[webdriver.geturl().split("\\/").length-2];
            GeneralMethod gm = new GeneralMethod();
            int[] array = {0};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime,quoteId);
            }
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean getTextByName(String value1, String header,String sum, String runtime){
        try {
            int row=getRow(value1);
            int col=getCol(header);
            if(row==-1 || col==-1){
                return false;
            }
            String script=buildScript(row,col)+".textContent";
            JSONObject jo=webdriver.executeScript2("return "+script);
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            else{
                Double output=Double.parseDouble(jo.getString("value").replaceAll("%", ""))+Double.parseDouble(sum);
                GeneralMethod gm = new GeneralMethod();
                int[] array = {3};
                String[] value = gm.runtimeparameter(array);
                for (int j = 0; j < value.length; j++) {
                    runtime = value[j];
                    webdriver.storeruntime(runtime,""+output);
                }
            }
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean checkboxstatus(String value1, String header,String check, String val){
        try {
            int row=getRow(value1);
            int col=getCol(header);
            if(row==-1 || col==-1){
                return false;
            }
            else{
                Double output=Double.parseDouble(val.replaceAll("%", ""));
                String checkscript=buildScript(row,col)+".querySelector(\"input\").checked";
                String checkstatus=webdriver.executeScript2("return "+checkscript).get("value").toString();
                if(output < Double.parseDouble(check) && checkstatus.equals("true")){
                    return true;
                }
                else if(output >= Double.parseDouble(check) && checkstatus.equals("false")){
                    return true;
                }   
                else{
                    return false;
                }
            }
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean notexists(String param1){
        try{
            String xpath=getUnique();
            if(xpath==null){
                return false;
            }
            xpath=xpath.replace("#replace", param1);
            return webdriver.notexists("xpath",xpath);
        }catch(Exception e){
            return false;
        }
    }

    public boolean selectgroup(String param){
        try {
            String script="document.querySelector(\"sb-page-container\").shadowRoot.querySelector(\"sb-line-editor\").shadowRoot.querySelector(\"sb-le-group-layout\").shadowRoot.querySelectorAll(\"div.nav-item\")";
            JSONObject jo=webdriver.executeScript2("return "+script);
            int length=((JSONArray)jo.get("value")).length();
            for(int i=0;i<length;i++){
                String text=webdriver.executeScript2("return "+script+"["+i+"].innerText").getString("value").toString();
                if(text.equals(param)){
                    JSONObject test=webdriver.executeScript2("return "+script+"["+i+"].click()");
                    if(test.get("status").toString().equals("500")){
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean dynamicclickifexist(String replace){
        try {
            String xpath=getuniquexpath();
            xpath=xpath.replaceAll("#replace", replace);
            webdriver.click("xpath",xpath);
        } catch (Exception e) {
            //TODO: handle exception
        }
        return true;
    }

    public boolean getMondayOfCurrentWeek(String runtime) {
        LocalDate today = LocalDate.now();
        int daysToMonday = DayOfWeek.MONDAY.getValue() - today.getDayOfWeek().getValue();
        if (daysToMonday > 0) {
            daysToMonday -= 7;
        }
        today.plusDays(daysToMonday);
        GeneralMethod gm = new GeneralMethod();
        int[] array = {0};
        String[] value = gm.runtimeparameter(array);
        for (int j = 0; j < value.length; j++) {
            runtime = value[j];
            webdriver.storeruntime(runtime, today.toString());
        } 
        return true;
    }

    public boolean ReadValuefromDisabledField(String runtime){
        try {
            String xpath=null;
            for(int i=0;i<webdriver.getCurrentObject().getAttributes().size();i++){
                try{
                    if(webdriver.getCurrentObject().getAttributes().get(i).get("unique").asBoolean()){
                        xpath=webdriver.getCurrentObject().getAttributes().get(i).get("value").asText();
                    }
                }
                catch(Exception e){

                } 
            }
            if(xpath==null){
                return false;
            }
            String text=webdriver.executeScript2("var element = document.evaluate(\"" + xpath + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; return element.value;").get("value").toString();
            GeneralMethod gm = new GeneralMethod();
            int[] array = {0};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, text);
            } 
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    


    public boolean nextMonday(String dateString, String runtime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            LocalDate date = LocalDate.parse(dateString, formatter);
            LocalDate newDate = date.plusDays(7);
            GeneralMethod gm=new GeneralMethod();
            int[] array = {1};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, newDate.toString());
            }
            return true;
        }
        catch(Exception e){
            return false;
        }
    }



    public boolean validateCumualtive(String values, String cumulatives){
        try {
            String[] valArr=values.split(" ");
            // String[] cumArr=cumulatives.split(" ");
            double sum=0.00;
            String cumString="";
            for(int i=valArr.length-1;i>=0;i--){
                sum+=Double.parseDouble(valArr[i]);
                cumString+=(formatdecimal1(sum/(valArr.length-i)))+" ";
            }

            String[] numbers = cumString.split(" ");
            StringBuilder reversed = new StringBuilder();
            for (int i = numbers.length - 1; i >= 0; i--) {
                reversed.append(numbers[i]);
                if (i != 0) { 
                    reversed.append(" ");
                }
            }

            return reversed.toString().trim().equals(cumulatives.trim());
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }
        

    public boolean calculatePercentages(String numerators, String denominators, String runtime){
        try {
            String[] numArr=numerators.split(" ");
            String[] denomArr=denominators.split(" ");
            double nume=0.00;
            double denom=0.00;
            String cumString="";
            for(int i=0;i<numArr.length;i++){
                nume=Double.parseDouble(numArr[i]);
                denom=Double.parseDouble(denomArr[i]);
                cumString+=(formatdecimal1((nume/denom)*100))+" ";
            }
            GeneralMethod gm=new GeneralMethod();
            int[] array = {2};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, cumString);
            }
            return true;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }

    public String getUnique(){
        String stopXpath=null;
        for(int i=0;i<webdriver.getCurrentObject().getAttributes().size();i++){
            try{
                if(webdriver.getCurrentObject().getAttributes().get(i).get("unique").asBoolean()){
                    stopXpath=webdriver.getCurrentObject().getAttributes().get(i).get("value").asText();
                }
            }
            catch(Exception e){

            } 
        }
        if(stopXpath==null){
            return null;
        }
        return stopXpath;
    }

    public boolean dynamicScrollIntoView(String tableXpath,String replace){
    try{
            String stopXpath=getUnique();
            if(stopXpath==null){
                return false;
            }
            stopXpath=stopXpath.replaceAll("#replace", replace);
            int height=Integer.parseInt(webdriver.executeScript2("return (document.evaluate(\""+tableXpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue).scrollHeight").get("value").toString());
            for(int i=0;i<height;i+=100){
                String increment="(document.evaluate(\""+tableXpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue).scrollTop="+i;
                webdriver.executeScript(increment);
                // height=Integer.parseInt(webdriver.executeScript2("return (document.evaluate(\""+tableXpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue).scrollHeight").get("value").toString());
                String eleID=webdriver.findElement("xpath", stopXpath);
                if(!eleID.contains("no such element")){
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean dynamicScrollVertical(String tableXpath,String replace){
        try{
                String stopXpath=getUnique();
                if(stopXpath==null){
                    return false;
                }
                stopXpath=stopXpath.replaceAll("#replace", replace);
                int height=Integer.parseInt(webdriver.executeScript2("return (document.evaluate(\""+tableXpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue).scrollWidth").get("value").toString());
                for(int i=0;i<height;i+=100){
                    String increment="(document.evaluate(\""+tableXpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue).scrollLeft="+i;
                    webdriver.executeScript(increment);
                    // height=Integer.parseInt(webdriver.executeScript2("return (document.evaluate(\""+tableXpath+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue).scrollHeight").get("value").toString());
                    String eleID=webdriver.findElement("xpath", stopXpath);
                    if(!eleID.contains("no such element")){
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                //TODO: handle exception
                return false;
            }
        }

    public boolean readtextusingjs(String replace,String runtime){
        try {
            JSONObject jo=webdriver.executeScript2("return document.evaluate(\""+getUnique().replaceAll("#replace", replace)+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.innerText");
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            GeneralMethod gm = new GeneralMethod();
            int[] array ={1};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, jo.get("value").toString());
            }
            return true;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }

    public boolean validatetextusingjs(String replace,String runtime){
        try {
            JSONObject jo=webdriver.executeScript2("return document.evaluate(\""+getUnique().replaceAll("#replace", replace)+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.innerText");
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            if(jo.getString("value").equals(runtime)){
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }

    public boolean validatepartialtextusingjs(String replace,String runtime){
        try {
            JSONObject jo=webdriver.executeScript2("return document.evaluate(\""+getUnique().replaceAll("#replace", replace)+"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.innerText");
            if(jo.get("status").toString().equals("500")){
                return false;
            }
            if(jo.getString("value").contains(runtime)){
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }



    public boolean validateTwoValues(String value1, String value2) {
        try {
            if (value1 == null || value2 == null) {
                return false;
            }
            return value1.equals(value2);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean dynamicexiststwoparameter(String replace1){
        return dynamicexiststwoparameter(replace1,"");
    }

    public boolean dynamicexiststwoparameter(String replace1, String replace2){
        try {
            for(int j=0;j<20;j++){
                String xpath=null;
                for(int i=0;i<webdriver.getCurrentObject().getAttributes().size();i++){
                    try{
                        if(webdriver.getCurrentObject().getAttributes().get(i).get("unique").asBoolean()){
                            xpath=webdriver.getCurrentObject().getAttributes().get(i).get("value").asText();
                        }
                    }
                    catch(Exception e){

                    } 
                }
                if(xpath==null){
                    return false;
                }
                xpath=xpath.replace("#replace1", replace1);
                xpath=xpath.replace("#replace2", replace2);
                xpath=xpath.replace("#replace",replace1);
                if(webdriver.findElements("xpath", xpath).length()>0){
                    return true;
                }
                Thread.sleep(500);
            }
            return false;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean checkNotEquals(String param1, String param2)
    {
        return !param1.equalsIgnoreCase(param2);
    }

    public String getuniquexpath(){
        String xpath=null;
        for(int i=0;i<webdriver.getCurrentObject().getAttributes().size();i++){
            try{
                if(webdriver.getCurrentObject().getAttributes().get(i).get("unique").asBoolean()){
                    xpath=webdriver.getCurrentObject().getAttributes().get(i).get("value").asText();
                }
            }
            catch(Exception e){

            } 
        }
        return xpath;
      }

    public boolean readMultipleElements(String runtime){
        try {
            String xpath=getuniquexpath();
            int count=webdriver.findElements("xpath", xpath).length();
            String vals="";
            for(int i=1;i<=count;i++){
                String text=webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"), "innerText");
                vals+=text+" ";
            }
            GeneralMethod gm = new GeneralMethod();
            int[] array = {0};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, vals);
            } 
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean readMultipleElementsValue(String runtime){
        try {
            String xpath=getuniquexpath();
            int count=webdriver.findElements("xpath", xpath).length();
            String vals="";
            for(int i=1;i<=count;i++){
                String text=webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"), "value");
                vals+=text+" ";
            }
            GeneralMethod gm = new GeneralMethod();
            int[] array = {0};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, vals);
            } 
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean readMultipleElementsValuePagenation(String next, String pageStatus, String runtime){
        try {
            String xpath=getuniquexpath();
            String vals="";
            while(true){
                String pagetext=webdriver.getElementproperty(webdriver.findElement("xpath", pageStatus), "innerText");
                int count=webdriver.findElements("xpath", xpath).length();
                for(int i=1;i<=count;i++){
                    String text=webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"), "value");
                    if(text.equals(""))
                        text="0";
                    vals+=text+" ";
                }
                if(pagetext.split("to")[1].trim().split("of")[0].trim().equals(pagetext.split("to")[1].trim().split("of")[1].trim())){
                    break;
                }
            }
            
            GeneralMethod gm = new GeneralMethod();
            int[] array = {2};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, vals);
            } 
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean readMultipleElementsTextPagenation(String next, String pageStatus, String runtime){
        try {
            String xpath=getuniquexpath();
            String vals="";
            while(true){
                String pagetext=webdriver.getElementproperty(webdriver.findElement("xpath", pageStatus), "innerText");
                int count=webdriver.findElements("xpath", xpath).length();
                for(int i=1;i<=count;i++){
                    String text=webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"), "innerText");
                    vals+=text+" ";
                }
                if(pagetext.split("to")[1].trim().split("of")[0].trim().equals(pagetext.split("to")[1].trim().split("of")[1].trim())){
                    break;
                }
            }
            
            GeneralMethod gm = new GeneralMethod();
            int[] array = {2};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, vals);
            } 
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public boolean checkmultiplevalues(String value){
        try {
            String xpath=getuniquexpath();
            int size=webdriver.findElements("xpath", xpath).length();
            for(int i=1;i<=size;i++){
                String text=webdriver.getElementproperty(webdriver.findElement("xpath", "("+xpath+")["+i+"]"), "innerText");
                if(!value.contains(text.replace(",", "").replace("INR", "").replace("\\%", "").trim())){
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean quoteLineCalculation() {
        try {
            String query="";
            webdriver.elementSendkeys(webdriver.findElement("xpath", "//textarea"), query);
            webdriver.click("xpath","//button[text()='Export']");
            String text=webdriver.getElementproperty(webdriver.findElement("xpath", ""), "innerText");
            return true;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }

    public boolean validateCumualtiveNumeric(String values, String cumulatives){
        try {
            String[] valArr=values.split(" ");
            // String[] cumArr=cumulatives.split(" ");
            double sum=0.00;
            String cumString="";
            for(int i=valArr.length-1;i>=0;i--){
                sum+=Double.parseDouble(valArr[i]);
                cumString+=(formatdecimal1(sum))+" ";
            }

            String[] numbers = cumString.split(" ");
            StringBuilder reversed = new StringBuilder();
            for (int i = numbers.length - 1; i >= 0; i--) {
                reversed.append(numbers[i]);
                if (i != 0) { 
                    reversed.append(" ");
                }
            }
            return reversed.toString().trim().equals(cumulatives.trim());
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

     public boolean mouseHoverOnElement(){
        return mouseHoverOnElement("");
    }
    
    public boolean mouseHoverOnElement(String replace){
        HashMap<String, String> headers = new HashMap();
        String host = String.valueOf(webdriver.webmethods.getDriver().getActionManger().getHostAddress()) + "/session/" + webdriver.webmethods.getDriver().getActionManger().getSessionId() + "/actions";
        JsonObject param = null;
        param = new JsonObject();
          try {
            String xpath=getuniquexpath();
            xpath=xpath.replaceAll("#replace", replace);
            JSONObject jo=webdriver.executeScript2("return (document.evaluate(\"" + xpath + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue).getBoundingClientRect()");
            int x=((int)Double.parseDouble(((JSONObject)jo.get("value")).get("x").toString()))+2;
            int y=((int)Double.parseDouble(((JSONObject)jo.get("value")).get("y").toString()))+2;
            JSONObject jsonObject = new JSONObject();
            JSONArray actionsArray = new JSONArray();
            JSONObject initialAction = new JSONObject();
            initialAction.put("type", "pointer");
            initialAction.put("id", "mouse");
            JSONObject parameters = new JSONObject();
            parameters.put("pointerType", "mouse");
            initialAction.put("parameters", parameters);
            JSONArray pointerActions = new JSONArray();
            JSONObject pointerMove = new JSONObject();
            pointerMove.put("type", "pointerMove");
            pointerMove.put("duration", 0);
            pointerMove.put("origin", "viewport");
            pointerMove.put("x", x);
            pointerMove.put("y", y);
            pointerActions.put(pointerMove);
            initialAction.put("actions", pointerActions);
            actionsArray.put(initialAction);
            jsonObject.put("actions", actionsArray);
            JSONObject res = HttpUtility.sendPost(host, jsonObject.toString(), headers);
            if (res.get("value").equals(null))
              return true; 
            String errMsg = res.getJSONObject("value").get("error").toString();
              return false;
          } catch (Exception e) {
              return false;
          }
    
      }

      public boolean copytoruntime(String local, String runtime) {
        try {
          GeneralMethod gm = new GeneralMethod();
          int[] myarray = { 1 };
          String[] value = gm.runtimeparameter(myarray);
          for (int i = 0; i < value.length; i++) {
            runtime = value[i];
            webdriver.storeruntime(runtime, local);
          } 
          return true;
        } catch (Exception e) {
          return false;
        } 
      }

      public boolean getValuefromDisabledField(String runtime){
        try {
            String xpath=null;
            for(int i=0;i<webdriver.getCurrentObject().getAttributes().size();i++){
                try{
                    if(webdriver.getCurrentObject().getAttributes().get(i).get("unique").asBoolean()){
                        xpath=webdriver.getCurrentObject().getAttributes().get(i).get("value").asText();
                    }
                }
                catch(Exception e){

                } 
            }
            if(xpath==null){
                return false;
            }
            String text=webdriver.executeScript2("var element = document.evaluate(\"" + xpath + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; return element.value;").get("value").toString();
            GeneralMethod gm = new GeneralMethod();
            int[] array = {0};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, text);
            } 
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public boolean urlGetter(String runtime){
        try {
            String url=webdriver.geturl();
            GeneralMethod gm = new GeneralMethod();
            int[] array = {0};
            String[] value = gm.runtimeparameter(array);
            for (int j = 0; j < value.length; j++) {
                runtime = value[j];
                webdriver.storeruntime(runtime, url);
            } 
            return true;
        } catch (Exception e) {
            return false;
            //TODO: handle exception
        }
    }
}



