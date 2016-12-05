package com.epf.cloud.ehealth_cloud.server.controller;
import java.net.UnknownHostException;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * SpringMVC Controller that lives on the server side and handles incoming HTTP requests. It is basically a servlet but
 * using the power of SpringMVC we can avoid a lot of the raw servlet and request/response mapping uglies that
 * servlets require and instead just deal with simple, clean Java Objects. For more information on SpringMVC see:
 * http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html
 */
@Controller
public class WelcomeController {

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
    
    @RequestMapping(value="/bodyPosition")
    public void bodyPosition(){
    	//String bodyPosition = data.getData();
    	String bodyPosition = "test";
        log.info("Server Received data " + bodyPosition);
        DB db = mongoClient.getDB(uri.getDatabase());
		DBCollection coll = db.getCollection("positionBodySensor");
		BasicDBObject doc = new BasicDBObject("id", "1")
		        .append("position", bodyPosition);
		        //.append("count", 1)
		        //.append("info", new BasicDBObject("x", 203).append("y", 102));
		coll.insert(doc);
    }
    
    @RequestMapping("/welcome")
    public @ResponseBody WelcomeMessage sayHello(@RequestParam(required = false) String name) {

        log.info("Saying hello to '{}'", name);

        String message;
        if (name != null && name.trim().length() > 0) {
            message = "Hello " + name;
        } else {
            message = "Hello mysterious person";
        }
        return new WelcomeMessage(message, new Date());
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
    
    @RequestMapping(value="/analyse", method = RequestMethod.POST)
    public @ResponseBody WelcomeMessage analyse(@RequestBody ClientData data) {        
    	String strData = data.getData();
        log.info("Server Received data " + strData );

        String message;
        if (strData != null && strData.trim().length() > 0) {            
            message = "Server has already received your data " + strData ;
        } else {
            message = "FHE-Cloud server received a mysterious data from client";
        }
        return new WelcomeMessage(message, Calendar.getInstance().getTime());
    }
    
}
