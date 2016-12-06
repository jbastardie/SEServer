package com.epf.cloud.ehealth_cloud.server.controller;
import java.net.UnknownHostException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import javax.annotation.PostConstruct;
import com.epf.cloud.ehealth_cloud.common.ClientData;

import com.epf.cloud.ehealth_cloud.common.WelcomeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.io.*;
import java.util.logging.Level;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
/**
 * SpringMVC Controller that lives on the server side and handles incoming HTTP requests. It is basically a servlet but
 * using the power of SpringMVC we can avoid a lot of the raw servlet and request/response mapping uglies that
 * servlets require and instead just deal with simple, clean Java Objects. For more information on SpringMVC see:
 * http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html
 */
@Controller
public class WelcomeController extends HttpServlet{

    private static final Logger log = LoggerFactory.getLogger(WelcomeController.class);

    /**
     * This method is exposed as a REST service. The @RequestMapping parameter tells Spring that when a request comes in
     * to the server at the sub-url of '/welcome' (e.g. http://localhost:8080/ehealth-cloud-server/welcome)
     * it should be directed to this method.
     * <p/>
     * In normal SpringMVC you would typically handle the request, attach some data to the 'Model' and redirect to a
     * JSP for rendering. In our REST example however we want the result to be an XML response. Thanks to some Spring
     * magic we can just return our bean, annotate it with @ResponseBody and Spring will magically turn this into XML
     * for us.
     * <p/>
     * We really didn't need the whole WelcomeMessage object here and could just have easily returned a String. That
     * wouldn't have made a very good example though, so the WelcomeMessage is here to show how Spring turns objects
     * into XML and back again for easy REST calls. The 'date' parameter was added just to give it some spice.
     *
     * @param name the name of the person to say hello to. This is pulled from the input URL. In this case we use a
     *             request parameter (i.e. ?name=someone), but you could also map it directly into the URL if you
     *             prefer. See the very good SpringMVC documentation on this for more information.
     * @return 
     * @return
     */
    String textUri = "mongodb://sysembed:sysembed@ds113958.mlab.com:13958/embedsys";
    MongoClient mongoClient;
    MongoClientURI uri;
    private boolean realTime = true;
    
    @PostConstruct
    public void init(){
    	uri = new MongoClientURI(textUri);
    	try {
    		mongoClient = new MongoClient(uri);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		}
    }
    
    @RequestMapping(value="/bodyPosition",method = RequestMethod.POST)
    public void bodyPosition(HttpServletRequest dataRequest, HttpServletResponse response){
    	
    	String bodyPosition = dataRequest.getParameter("bodyPosition");
        log.info("Server Received data " + bodyPosition);
        DB db = mongoClient.getDB(uri.getDatabase());
		DBCollection coll = db.getCollection("positionBodySensor");
		BasicDBObject doc = new BasicDBObject("id", "1")
		        .append("position", bodyPosition);
		        
		        //.append("info", new BasicDBObject("x", 203).append("y", 102));
		coll.insert(doc);
    }
     
    
    @RequestMapping("connectMongo")
    public void initiateConnection(){
    	String textUri = "mongodb://sysembed:sysembed@ds113958.mlab.com:13958/embedsys";
    	DB db;
    	MongoClientURI uri = new MongoClientURI(textUri);
    	try {
			MongoClient m = new MongoClient(uri);
			 db = m.getDB(uri.getDatabase());
			DBCollection coll = db.getCollection("positionBodySensor");
			BasicDBObject doc = new BasicDBObject("name", "HRK")
			        .append("type", "database")
			        .append("count", 1)
			        .append("info", new BasicDBObject("x", 203).append("y", 102));
			coll.insert(doc);
			
			DBCursor cursor = coll.find();
			try {
			   while(cursor.hasNext()) {
			       System.out.println(cursor.next());
			   }
			} finally {
			   cursor.close();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @RequestMapping("getDataSensor")
    public void getDataSensor(){
    	
    	int lastDataPosition =  getLastData("/home/pi/DEV/eHealth_raspberrypi_v2.3/Data/positionData.txt"); 
    	sendDataToCollection("positionBodySensor",lastDataPosition);
    	int lastDataTemperature =  getLastData("/home/pi/DEV/eHealth_raspberrypi_v2.3/Data/temperatureData.txt"); 
    	sendDataToCollection("temperature",lastDataTemperature);
    	int lastDataAirflow =  getLastData("/home/pi/DEV/eHealth_raspberrypi_v2.3/Data/airflowData.txt"); 
    	sendDataToCollection("airflow",lastDataAirflow);
    	String json = "{ \"id\":1,\"data\":{\"bodyPositionSensor\": {\"id\":1,\"data\":"+lastDataPosition+"},\"temperatureSensor\": {\"id\":2,\"data\":"+lastDataTemperature+"},\"airflowSensor\": {\"id\":3,\"data\":"+lastDataAirflow+"}}}";
    	
    	log.info(json);
    	sendDataToSocket(json);
    	
    }
    
    @RequestMapping("realTime")
    public void realTimeData(){
    	while(realTime){
    		getDataSensor();
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    @RequestMapping("stopRealTime")
    public void stopRealTimeData(){
    	realTime = false;
    }
    
    @RequestMapping("startRealTime")
    public void startRealTimeData(){
    	realTime = true;
    }
    
    
    public int getLastData(String filePath){
    	List<Integer> list = new ArrayList<Integer>();
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                list.add(Integer.parseInt(text));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        int data = 0;
        if(list.get(list.size()-1) != null){
        	data = list.get(list.size()-1);
        }
    	return data;
    }
    
    
    public void sendDataToCollection(String collectionName,int data){
    	//log.info("Server Received data " + data);
    	DB db = mongoClient.getDB(uri.getDatabase());
		DBCollection coll = db.getCollection(collectionName);
		BasicDBObject doc = new BasicDBObject("id", "1")
				.append("idSensorPosition", "1")
		        .append("position", data);				
		        //.append("count", 1)
		        //.append("info", new BasicDBObject("x", 203).append("y", 102));
		
		coll.insert(doc);
	}
    
    public void sendDataToSocket(String json){
    	
    	String url = "http://192.168.43.224:8080/JbossPost";
    	HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);

		// add header
		//post.setHeader("User-Agent", USER_AGENT);

		
		StringEntity input = null;
		try {
			input = new StringEntity(json);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		input.setContentType("application/json");
		post.setEntity(input);
		

		HttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("\nSending 'POST' request to URL : " + url);
		log.info("Post parameters : " + post.getEntity());
		log.info("Response Code : " + response.getStatusLine().getStatusCode());

		BufferedReader rd = null;
		try {
			rd = new BufferedReader(
			                new InputStreamReader(response.getEntity().getContent()));
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer result = new StringBuffer();
		String line = "";
		try {
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.info(result.toString());

		
    }
    
}
