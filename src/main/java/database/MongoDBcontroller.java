package database;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mindrot.jbcrypt.BCrypt;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MongoDBcontroller {
    String urlConfig, portConfig, databaseConfig;
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<org.bson.Document> collectionList, collectionLogs, collectionMessages;

    //MongoClient mongoClient = new MongoClient("localhost", 27017);
    //MongoDatabase database = mongoClient.getDatabase("itBanking");

    public MongoDBcontroller() throws FileNotFoundException {
        configReader();
        this.mongoClient = new MongoClient(urlConfig, Integer.parseInt(portConfig));
        this.database = mongoClient.getDatabase(databaseConfig);
        this.collectionList = collectionList = database.getCollection("list");
        this.collectionLogs = collectionLogs = database.getCollection("log");
        this.collectionMessages = collectionMessages = database.getCollection("messages");
        System.out.println("Database connected");
    }


    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<org.bson.Document> getCollectionList() {
        return collectionList;
    }

    public MongoCollection<org.bson.Document> getCollectionLogs() {
        return collectionLogs;
    }

    public MongoCollection<Document> getCollectionMessages() {
        return collectionMessages;
    }

    public void addMessages(String from, String to, String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
        Document document = new Document("from", from)
                .append("to", to)
                .append("message", message)
                .append("time", timeStamp);
        collectionMessages.insertOne(document);
    }

    public void addLogsLogin(String login) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
        Document document = new Document("type", "login")
                .append("login", login)
                .append("time", timeStamp);
        collectionLogs.insertOne(document);
    }

    public void addLogsLogout(String login) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
        Document document = new Document("type", "logout")
                .append("login", login)
                .append("time", timeStamp);
        collectionLogs.insertOne(document);
    }

    public void addList(String fname, String lname, String login, String password) {
        Document document = new Document("fname", fname)
                .append("lname", lname)
                .append("login", login)
                .append("password", password);
        collectionList.insertOne(document);
    }

    public JSONObject findInformationFromMongo(String login) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("login", login);

        MongoCursor<Document> mongoCursor = collectionList.find().iterator();
        while (mongoCursor.hasNext()) {
            Document doc = mongoCursor.next();
            JSONObject object = new JSONObject(doc.toJson());
            if (object.getString("login").equals(login)) {
                System.out.println(object);
                return object;
            }
        }
        return null;
    }

    public JSONObject findMessagesFromMongo(String login) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("login", login);
        JSONObject messages = new JSONObject();
        MongoCursor<Document> mongoCursor = collectionMessages.find().iterator();
        int count = 0;
        while (mongoCursor.hasNext()) {
            Document doc = mongoCursor.next();
            JSONObject object = new JSONObject(doc.toJson());

            if (object.getString("from").equals(login) || object.getString("to").equals(login)) {
                count++;
                messages.put(String.valueOf(count), object);

            }
        }
        return messages;
    }

    public List<String> findLogsFromMongo(String login) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("login", login);
        List<String> myList = new ArrayList<String>();
        MongoCursor<Document> mongoCursor = collectionLogs.find().iterator();
        while (mongoCursor.hasNext()) {
            Document doc = mongoCursor.next();
            JSONObject object = new JSONObject(doc.toJson());
            if (object.getString("login").equals(login)) {
                myList.add(object.toString());
            }
        }
        System.out.println(myList);
        return myList;
    }

    private String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(11));
    }

    public boolean checkUserPassMongo(String login, String passwordToCompare) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("login", login);

        if (BCrypt.checkpw(passwordToCompare, findInformationFromMongo(login).getString("password"))) {
            return true;
        }
        return false;
    }

    public void changePassword(String login, String passHash) {
        Bson filter = new Document("login", login);
        Bson newValue = new Document("password", passHash);
        Bson updateOperationDocument = new Document("$set", newValue);
        collectionList.updateOne(filter, updateOperationDocument);
    }

    public void deleteUser(String login) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("login", login);
        collectionList.deleteOne(basicDBObject);

        try (MongoCursor<Document> cursor = collectionMessages.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                JSONObject object = new JSONObject(doc.toJson());
                if (object.getString("from").equals(login)) {
                    basicDBObject = new BasicDBObject();
                    basicDBObject.put("from", login);
                    collectionMessages.deleteOne(basicDBObject);
                }
            }
        }
    }

    public void updateFname(String name, String fname) {
        Bson filter = new Document("fname", name);
        Bson newValue = new Document("fname", fname);
        Bson updateOperationDocument = new Document("$set", newValue);
        collectionList.updateOne(filter, updateOperationDocument);
    }

    public void updateLname(String name, String lname) {
        Bson filter = new Document("lname", name);
        Bson newValue = new Document("lname", lname);
        Bson updateOperationDocument = new Document("$set", newValue);
        collectionList.updateOne(filter, updateOperationDocument);
    }

    public void deleteAllMessagess(String login) {
        BasicDBObject basicDBObject = new BasicDBObject();
        try (MongoCursor<Document> cursor = collectionMessages.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                JSONObject object = new JSONObject(doc.toJson());
                if (object.getString("from").equals(login)) {
                    basicDBObject = new BasicDBObject();
                    basicDBObject.put("from", login);
                    collectionMessages.deleteOne(basicDBObject);
                }
                if (object.getString("to").equals(login)) {
                    basicDBObject = new BasicDBObject();
                    basicDBObject.put("to", login);
                    collectionMessages.deleteOne(basicDBObject);
                }
            }
        }
    }

    public boolean existUserMongo(String loginToCompare) {
        if (findInformationFromMongo(loginToCompare) != null) {
            return true;
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public void configReader() throws FileNotFoundException {
        String filePath = "C:\\Users\\FireflySK\\IdeaProjects\\myServer\\src\\main\\java\\database\\config.json";
        try {
            FileReader fileReader = new FileReader(filePath);
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) parser.parse(fileReader);
            System.out.println(jsonObject);
            if (jsonObject.containsKey("url") && jsonObject.containsKey("port") && jsonObject.containsKey("database")) {
                this.urlConfig = jsonObject.get("url").toString();
                this.portConfig = jsonObject.get("port").toString();
                this.databaseConfig = jsonObject.get("database").toString();
            } else {
                System.out.println("error: wrong configuration file");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
/*



db.createCollection( <list>,
   {
     fname: <string>,
     lname: <string>,
     login: <string>,
     password: <string>
   }
)
db.createCollection( <log>,
   {

   }
)
db.createCollection( <messages>,
   {

   }
)

 */