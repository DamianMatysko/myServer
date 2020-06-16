package sample;

import database.MongoDBcontroller;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class Requests {
    List<String> tokens = new ArrayList<String>();
    List<String> bans = new ArrayList<String>();
    //List<String> wrongImputs = new ArrayList<String>();

    @RequestMapping("/time")
    public ResponseEntity getTime(@RequestParam(value = "login") String login, @RequestParam(value = "token") String token) {
        if (checkToken(login, token)) {
            String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
            return ResponseEntity.status(200).body(timeStamp);
        } else {
            return ResponseEntity.status(401).body("error: you must be login to get time");
        }
    }

    @RequestMapping("/primenumber/{number}")
    public ResponseEntity<String> checkPrimeNumber(@PathVariable int number) {
        boolean flag = false;
        JSONObject obj = new JSONObject();
        obj.put("number", number);

        for (int i = 2; i <= number / 2; ++i) {
            if (number % i == 0) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            System.out.println(number + " is a prime number.");
            obj.put("primenumber", true);
            return ResponseEntity.status(200).body(obj.toString());
        } else {
            System.out.println(number + " is not a prime number.");
            obj.put("primenumber", false);
            return ResponseEntity.status(200).body(obj.toString());
        }
    }

    @RequestMapping("/time/hour")
    public ResponseEntity<String> getHour() {
        JSONObject obj = new JSONObject();
        String timeStamp = new SimpleDateFormat("HH").format(Calendar.getInstance().getTime());
        obj.put("hour", timeStamp);
        return ResponseEntity.status(200).body(obj.toString());
    }

    @RequestMapping("/hello")
    public String getHello() {
        return "Hello. How are you?";
    }

    @RequestMapping("/hello/{name}")
    public String getHelloWithName(@PathVariable String name) {
        return "Hello " + name + ". How are you? ";
    }

    @RequestMapping("/hi")
    public String getHi(@RequestParam(value = "fname") String fname, @RequestParam(value = "age") String age) {
        return "Hello. How are you? Your name is " + fname + " and you are " + age;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public ResponseEntity<String> login(@RequestBody String data) throws FileNotFoundException, ParseException {
        JSONObject obj = new JSONObject(data);
        JSONObject res = new JSONObject();

        if (!new MongoDBcontroller().existUserMongo(obj.getString("login"))) {
            res.put("error", "User doesn't exist");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }

        if (isUserBanned(obj.getString("login"))) {
            res.put("error", "Too many inputs");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }

        if (existToken(obj.getString("login"))) {
            res.put("error", "Arealy login");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }

        if (!new MongoDBcontroller().checkUserPassMongo(obj.getString("login"), obj.getString("password"))) {
            JSONObject banForUser = new JSONObject();
            banForUser.put("login", obj.getString("login"));

            Date date = Calendar.getInstance().getTime();
            date = addMinutesToJavaUtilDate(date, 5);
            String timeStamp = new SimpleDateFormat("HH:mm dd/MM/yyyy").format(date);

            System.out.println(timeStamp);
            banForUser.put("time", timeStamp);
            bans.add(banForUser.toString());

            addBan(obj.getString("login"), 1);

            res.put("error", "Wrong password");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }

        JSONObject user = new MongoDBcontroller().findInformationFromMongo(obj.getString("login"));
        user.put("token", generateToken());
        tokens.add(user.toString());

        MongoDBcontroller mongoDBcontroller = new MongoDBcontroller();
        mongoDBcontroller.addLogsLogin(obj.getString("login"));

        return ResponseEntity.status(200).body(user.toString());
    }

    private void addBan(String login, int minits) {
        JSONObject banForUser = new JSONObject();
        banForUser.put("login", login);

        Date date = Calendar.getInstance().getTime();
        date = addMinutesToJavaUtilDate(date, minits);
        String timeStamp = new SimpleDateFormat("HH:mm dd/MM/yyyy").format(date);

        banForUser.put("time", timeStamp);
        bans.add(banForUser.toString());
    }

    public Date addMinutesToJavaUtilDate(Date date, int minits) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minits);//TODO add 5min no 1
        return calendar.getTime();
    }

    private boolean isUserBanned(String login) throws ParseException {
        for (String string : bans) {
            JSONObject jsonObject = new JSONObject(string);
            if (jsonObject.getString("login").equalsIgnoreCase(login)) {
                String timeStamp = new SimpleDateFormat("HH:mm dd/MM/yyyy").format(Calendar.getInstance().getTime());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                Date date = simpleDateFormat.parse(jsonObject.getString("time"));

                System.out.println(date);
                System.out.println(Calendar.getInstance().getTime());
                System.out.println(Calendar.getInstance().getTime().after(date));
                if (Calendar.getInstance().getTime().after(date)) {
                    bans.remove(string);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/signup")
    public ResponseEntity<String> signup(@RequestBody String data) throws FileNotFoundException {
        //System.out.println(data);
        JSONObject obj = new JSONObject(data);

        if (!(obj.has("fname") && obj.has("lname") && obj.has("login") && obj.has("password"))) {// vstup je ok, mame vsetky kluce
            JSONObject res = new JSONObject();
            res.put("error", "Invalid body request");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        if (new MongoDBcontroller().existUserMongo(obj.getString("login"))) {
            JSONObject res = new JSONObject();
            res.put("error", "User already exists");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        String password = obj.getString("password");
        if (password.isEmpty()) {
            JSONObject res = new JSONObject();
            res.put("error", "Password is a mandatory field");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        String hashPass = hash(obj.getString("password"));

        new MongoDBcontroller().addList(obj.getString("fname"), obj.getString("lname"), obj.getString("login"), hashPass);
        JSONObject res = new JSONObject();
        res.put("fname", obj.getString("fname"));
        res.put("lname", obj.getString("lname"));
        res.put("login", obj.getString("login"));
        return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(res.toString());
    }

    private String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(11));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/logout")
    public ResponseEntity<String> logout(@RequestBody String data, @RequestParam(value = "token") String userToken) throws FileNotFoundException {
        JSONObject obj = new JSONObject(data);
        JSONObject res = new JSONObject();
        if (!checkToken(obj.getString("login"), userToken)) {

            res.put("error", "Wrong token");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }

        if (!new MongoDBcontroller().existUserMongo(obj.getString("login"))) {
            res.put("error", "Wrong token");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }

        MongoDBcontroller mongoDBcontroller = new MongoDBcontroller();
        mongoDBcontroller.addLogsLogout(obj.getString("login"));

        deletTokenFromList(userToken);

        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body("{}");
    }

    private boolean deletTokenFromList(String tokenToDelete) {
        for (String string : tokens) {
            System.out.println(string);
            JSONObject jsonObject = new JSONObject(string);
            if (jsonObject.getString("token").equals(tokenToDelete)) {
                tokens.remove(string);
                return true;
            }
        }
        return false;
    }

    private boolean checkToken(String login, String tokenToCompare) {

        for (String string : tokens) {

            JSONObject jsonObject = new JSONObject(string);
            System.out.println("login compares" + " " + jsonObject.getString("login") + " " + login);
            if (jsonObject.getString("login").equals(login)) {
                if (jsonObject.getString("token").equals(tokenToCompare)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existToken(String login) {
        for (String string : tokens) {
            JSONObject jsonObject = new JSONObject(string);
            System.out.println("login compares" + " " + jsonObject.getString("login") + " " + login);
            if (jsonObject.getString("login").equals(login)) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/log")
    public ResponseEntity log(@RequestBody String data, @RequestParam(value = "token") String userToken) throws FileNotFoundException {
        JSONObject obj = new JSONObject(data);
        System.out.println(new MongoDBcontroller().existUserMongo(obj.getString("login")));
        System.out.println(checkToken(obj.getString("login"), userToken));
        if (new MongoDBcontroller().existUserMongo(obj.getString("login")) && checkToken(obj.getString("login"), userToken)) {
            return ResponseEntity.status(200).body(new MongoDBcontroller().findLogsFromMongo(obj.getString("login")).toString());

        }
        return ResponseEntity.status(401).body("error");
    }

    @RequestMapping(method = RequestMethod.POST, value = "/changepassword")
    public ResponseEntity<String> changePassword(@RequestBody String data, @RequestParam(value = "token") String userToken) throws FileNotFoundException {
        JSONObject obj = new JSONObject(data);
        JSONObject res = new JSONObject();
        if (!new MongoDBcontroller().existUserMongo(obj.getString("login"))) {
            res.put("error", "This user login was not found");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        if (!new MongoDBcontroller().checkUserPassMongo(obj.getString("login"), obj.getString("oldpassword"))) {
            res.put("error", "Wrong password");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        if (!checkToken(obj.getString("login"), userToken)) {
            res.put("error", "Wrong token");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        new MongoDBcontroller().changePassword(obj.getString("login"), hash(obj.getString("newpassword")));
        res.put("success", "Password changed");
        return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(res.toString());
    }

    @RequestMapping(method = RequestMethod.POST, value = "/message/new")
    public ResponseEntity<String> newMessage(@RequestBody String data, @RequestParam(value = "token") String userToken) throws FileNotFoundException {
        JSONObject obj = new JSONObject(data);
        JSONObject res = new JSONObject();
        if (!checkToken(obj.getString("from"), userToken)) {
            res.put("error", "Wrong token");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        if (!new MongoDBcontroller().existUserMongo(obj.getString("from"))) {
            res.put("error", "This user login was not found!");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        if (!new MongoDBcontroller().existUserMongo(obj.getString("to"))) {
            res.put("error", "This user login was not found");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }


        MongoDBcontroller mongoDBcontroller = new MongoDBcontroller();
        mongoDBcontroller.addMessages(obj.getString("from"), obj.getString("to"), obj.getString("message"));
        res.put("success", "Message send");
        return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(res.toString());
    }

    @RequestMapping(method = RequestMethod.POST, value = "/messages")
    public ResponseEntity<String> showMessages(@RequestBody String data, @RequestParam(value = "token") String userToken) throws FileNotFoundException {
        JSONObject obj = new JSONObject(data);
        if (checkToken(obj.getString("login"), userToken)) {

            JSONObject mongoMessages = new MongoDBcontroller().findMessagesFromMongo((obj.getString("login")));

            return ResponseEntity.status(200).body(mongoMessages.toString());
        } else {
            return ResponseEntity.status(400).body("error");
        }
    }

    @DeleteMapping(value = "/delete/{login}")
    public ResponseEntity<String> deleteUser(@RequestHeader(value = "token") String token, @PathVariable String login) throws FileNotFoundException {
        if (new MongoDBcontroller().existUserMongo(login) && checkToken(login, token)) {
            new MongoDBcontroller().deleteUser(login);
            return ResponseEntity.status(201).body("removed");
        } else {
            return ResponseEntity.status(400).body("error wrong login or token");
        }
    }

    @PatchMapping(value = "update/{login}")
    public ResponseEntity<String> updateLogin(@RequestBody String data, @RequestHeader String token, @PathVariable String login) throws FileNotFoundException {
        JSONObject obj = new JSONObject(data);
        if (checkToken(obj.getString("login"), token)) {
            if (obj.has("firstName")) {
                //findInformation(login).setFname(obj.getString("firstName"));
                new MongoDBcontroller().updateFname(login, obj.getString("firstName"));
            }
            if (obj.has("lastName")) {
                //findInformation(login).setFname(obj.getString("lastName"));
                new MongoDBcontroller().updateFname(login, obj.getString("lastName"));
            }
            return ResponseEntity.status(201).body("data changed");
        } else {
            return ResponseEntity.status(400).body("error wrong token or login");
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete/messages")
    public ResponseEntity<String> deleteMessages(@RequestBody String data, @RequestParam(value = "token") String userToken) throws FileNotFoundException {
        JSONObject obj = new JSONObject(data);
        JSONObject res = new JSONObject();
        if (!new MongoDBcontroller().existUserMongo(obj.getString("login"))) {
            res.put("error", "User doesn't exist");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        if (!checkToken(obj.getString("login"), userToken)) {
            res.put("error", "Wrong token");
            return ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        }
        new MongoDBcontroller().deleteAllMessagess(obj.getString("login"));
        res.put("success", "Deleted");
        return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(res.toString());
    }

    public String generateToken() {
        int size = 25;
        Random rnd = new Random();
        String generatedString = "";
        for (int i = 0; i < size; i++) {
            int type = rnd.nextInt(4);
            switch (type) {
                case 0:
                    generatedString += (char) ((rnd.nextInt(26)) + 65);
                    break;
                case 1:
                    generatedString += (char) ((rnd.nextInt(10)) + 48);
                    break;
                default:
                    generatedString += (char) ((rnd.nextInt(26)) + 97);
            }
        }
        return generatedString;
    }
}