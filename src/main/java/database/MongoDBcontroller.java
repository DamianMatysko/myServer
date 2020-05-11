package database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import sample.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MongoDBcontroller {
    MongoClient mongoClient = null;
    MongoDatabase database = null;
    MongoCollection<org.bson.Document> collectionList = null;
    MongoCollection<org.bson.Document> collectionLogs = null;
    MongoCollection<org.bson.Document> collectionMessages = null;

    public MongoDBcontroller() {
        this.mongoClient = new MongoClient("localhost", 27017);
        this.database = mongoClient.getDatabase("itBanking");
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

    public void addMessages(String from, String to ,String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
        Document document= new Document("from", from)
                .append("to", to)
                .append("message", message)
                .append("time", timeStamp);
        collectionMessages.insertOne(document);
    }

    public void addLogs(String login) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
        Document document= new Document("type", "login")
                .append("login", login)
                .append("time", timeStamp);
        collectionLogs.insertOne(document);
    }

    public void addList(String fname, String lname, String login, String passworld) {
        Document document= new Document("fname", fname)
                .append("lname", lname)
                .append("login", login)
                .append("passworld", passworld);
        collectionList.insertOne(document);
    }

    public void upade(MongoCollection<Document> collection) {
        Bson upadeValue=new Document();
        Bson    Value=new Document();
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