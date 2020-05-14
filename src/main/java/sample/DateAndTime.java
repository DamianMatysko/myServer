package sample;

import database.MongoDBcontroller;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

@RestController
public class DateAndTime {
    List<User> list = new ArrayList<User>();
    List<String> log = new ArrayList<String>();
    List<String> messages = new ArrayList<String>();
    List<String> tokens = new ArrayList<String>();
    List<String> bans = new ArrayList<String>();
    List<String> wrongImputs = new ArrayList<String>();

    public DateAndTime() {
        list.add(new User("Roman", "Simko", "roman", "heslo"));
    }

    @RequestMapping("/time")
    public ResponseEntity getTime(@RequestParam(value = "login") String login, @RequestParam(value = "token") String token) {
        if (token.equals(findInformation(login).getToken())) {
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
        return "Hello. How are you? ";
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
    public ResponseEntity<String> login(@RequestBody String data) {
        JSONObject obj = new JSONObject(data);
        //if (findLogin(obj.getString("login")) && checkUserPass(obj.getString("login"), obj.getString("password"))) {

        if (userHaveBan(obj.getString("login"))) {
            JSONObject res = new JSONObject();
            res.put("error", "too many inputs");
            return ResponseEntity.status(401).body(res.toString());
        }
        if (new MongoDBcontroller().existUserMongo(obj.getString("login"))) {
            if (new MongoDBcontroller().checkUserPassMongo(obj.getString("login"), obj.getString("password"))) {


                JSONObject user = new MongoDBcontroller().findInformationFromMongo(obj.getString("login"));
                user.put("token", generateToken());
                tokens.add(user.toString());

                JSONObject history = new JSONObject();
                history.put("type", "login");
                history.put("login", user.getString("login"));
                String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
                history.put("datetime", timeStamp);
                log.add(history.toString());

                MongoDBcontroller mongoDBcontroller = new MongoDBcontroller();
                mongoDBcontroller.addLogs(user.getString("login"));


                return ResponseEntity.status(200).body(user.toString());
            }
        }
/*
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("login", obj.getString("login"));
        jsonObject.put("try", 1);
        wrongImputs.add(jsonObject.toString());

if (countOfWrongInputs(obj.getString("login"))) {
 */
    JSONObject banForUser = new JSONObject();
    banForUser.put("login", obj.getString("login"));
    String timeStamp = new SimpleDateFormat("HH:mm dd/MM/yyyy").format(Calendar.getInstance().getTime());
    banForUser.put("time", timeStamp);
    bans.add(banForUser.toString());
//}

        return ResponseEntity.status(401).body("wrong login or password");
    }

    private boolean userHaveBan(String login) {
        for (String string : bans) {
            JSONObject jsonObject = new JSONObject(string);
            if (jsonObject.getString("login").equalsIgnoreCase(login)) {
                String timeStamp = new SimpleDateFormat("HH:mm dd/MM/yyyy").format(Calendar.getInstance().getTime());
                if (!jsonObject.getString("time").equals(timeStamp)) {//not same time
                    bans.remove(string);
                    return false;
                }
                return true;
            }
        }
        return false;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/signup")
    public ResponseEntity<String> signup(@RequestBody String data) {
        //System.out.println(data);
        JSONObject obj = new JSONObject(data);

        if (obj.has("fname") && obj.has("lname") && obj.has("login") && obj.has("password")) { // vstup je ok, mame vsetky kluce
            if (findLogin(obj.getString("login"))) {
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

            User user = new User(obj.getString("fname"), obj.getString("lname"), obj.getString("login"), hashPass);
            list.add(user);
            new MongoDBcontroller().addList(obj.getString("fname"), obj.getString("lname"), obj.getString("login"), hashPass);

            JSONObject res = new JSONObject();
            res.put("fname", obj.getString("fname"));
            res.put("lname", obj.getString("lname"));
            res.put("login", obj.getString("login"));
            return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(res.toString());
        } else {
            JSONObject res = new JSONObject();
            res.put("error", "Invalid body request");
            return ResponseEntity.status(400).body(res.toString());
        }
    }

    private String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(11));
    }

    private boolean findLogin(String login) {
        for (User user : list) {
            if (user.getLogin().equalsIgnoreCase(login))
                return true;
        }
        return false;
        // if (new MongoDBcontroller().getCollectionList().find(Bson bson ))
    }

    private boolean findPassword(String password) {
        for (User user : list) {
            if (user.getPassword() == hash(password))
                return true;
        }
        return false;
    }

    private boolean checkUserPass(String login, String password) {
        for (User user : list) {
            if (user != null && user.getLogin().equalsIgnoreCase(login)) {
                if (BCrypt.checkpw(password, user.getPassword()))
                    return true;
            }
        }
        return false;
    }

    private User findInformation(String login) {
        for (User user : list) {
            if (user.getLogin().equalsIgnoreCase(login))
                return user;
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/logout")
    public ResponseEntity<String> logout(@RequestBody String data, @RequestParam(value = "token") String userToken) {
        JSONObject obj = new JSONObject(data);
        //new MongoDBcontroller().existUserMongo(obj.getString("login"))&&
        if (checkToken(obj.getString("login"), userToken)) {


            //findInformation(obj.get("login").toString()).tokenResetter();
            deletTokenFromList(userToken);


            User user = findInformation(obj.getString("login"));
            JSONObject history = new JSONObject();
            history.put("type", "login");

            history.put("login", obj.getString("login"));
            String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
            history.put("datetime", timeStamp);
            log.add(history.toString());


            return ResponseEntity.status(200).body("OK");
        } else {
            return ResponseEntity.status(401).body("error");
        }
    }

    private boolean deletTokenFromList(String tokenToDelete) {
        for (String string : tokens) {
            System.out.println(string);
            JSONObject jsonObject = new JSONObject(string);
            if (jsonObject.getString("token").equals(tokenToDelete)) {
                tokens.remove(jsonObject);
                return true;
            }
        }
        return false;
    }

    private boolean checkToken(String login, String tokenToCompare) {

        for (String string : tokens) {
            System.out.println(string);
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

    @RequestMapping(method = RequestMethod.POST, value = "/log?type={type}")
    public ResponseEntity log(@RequestBody String data, @RequestParam(value = "token") String userToken, @PathVariable String type) {
        JSONObject obj = new JSONObject(data);
        JSONObject format = new JSONObject();
        if (findLogin(obj.getString("login")) && findInformation(obj.getString("login")).getToken().equals(userToken)) {

            List<String> myList = new ArrayList<String>();

            int count = 0;
            for (String list : log) {

                JSONObject information = new JSONObject(list);

                if (information.getString("login").equals(obj.getString("login")) || obj.getString("type").equals(type)) {
                    myList.add(list);
                    format.put(String.valueOf(count), list);
                    count++;
                }
            }
            String string = format.toString();

            return ResponseEntity.status(200).body(string);


        } else {
            return ResponseEntity.status(401).body("error");
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/changepassword")
    public ResponseEntity<String> changePassword(@RequestBody String data, @RequestParam(value = "token") String userToken) {
        JSONObject obj = new JSONObject(data);
        if (findLogin(obj.getString("login")) && BCrypt.checkpw(obj.getString("oldpassword"), findInformation(obj.getString("login")).getPassword()) && findInformation(obj.getString("login")).getToken().equals(userToken)) {
            findInformation(obj.getString("login")).setPassword(hash(obj.getString("newpassword")));
            return ResponseEntity.status(200).body("changed");
        }
        return ResponseEntity.status(401).body("error");

    }

    @RequestMapping(method = RequestMethod.POST, value = "/message/new")
    public ResponseEntity<String> newMessage(@RequestBody String data, @RequestParam(value = "token") String userToken) {
        JSONObject obj = new JSONObject(data);
        if (findInformation(obj.getString("from")).getToken().equals(userToken) && findLogin(obj.getString("from")) && findLogin(obj.getString("to"))) {
/*
            JSONObject message = new JSONObject();
            message.put("from", "login");
            message.put("to", "login");
            message.put("message", "login");

 */
            System.out.println(obj.toString());

            MongoDBcontroller mongoDBcontroller = new MongoDBcontroller();
            mongoDBcontroller.addMessages(obj.getString("from"), obj.getString("to"), obj.getString("message"));


            String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
            obj.put("time", timeStamp);


            messages.add(obj.toString());
            return ResponseEntity.status(201).body("Message send");
        } else {
            return ResponseEntity.status(400).body("error");
        }
    }
/*
    @RequestMapping(method = RequestMethod.POST, value = "/messages?from={fromUser}")
    public ResponseEntity<String> showMessages(@RequestBody String data, @RequestParam(value = "token") String userToken, @PathVariable String fromUser) {
        JSONObject obj = new JSONObject(data);
        if (findLogin(obj.getString("login")) && findInformation(obj.getString("login")).getToken().equals(userToken)) {
            JSONObject format = new JSONObject();
            int count = 0;
            for (String list : messages) {
                JSONObject information = new JSONObject(list);
                if (!fromUser.equals("") && information.getString("from").equals(fromUser)) {
                    format.put(String.valueOf(count), list);
                    count++;
                } else if (information.getString("from").equals(obj.getString("login")) || information.getString("to").equals(obj.getString("login"))) {
                    format.put(String.valueOf(count), list);
                    count++;
                }
            }
            String string = format.toString();
            return ResponseEntity.status(201).body(string);
        } else {
            return ResponseEntity.status(400).body("error");
        }
    }

 */

    @RequestMapping(method = RequestMethod.POST, value = "/messages?from={fromUser}")
    public ResponseEntity<String> showMessages(@RequestBody String data, @RequestParam(value = "token") String userToken, @PathVariable String fromUser) {
        JSONObject obj = new JSONObject(data);
        if (null != null) {
            JSONObject mongo = new MongoDBcontroller().findInformationFromMongo(obj.getString("login"));
            return ResponseEntity.status(400).body("error");
        } else {
            return ResponseEntity.status(400).body("error");
        }
    }

    @DeleteMapping(value = "/delete/{login}")
    public ResponseEntity<String> deleteUser(@RequestHeader(value = "token") String token, @PathVariable String login) {
        if (findLogin(login) && findInformation(login).getToken().equals(token)) {
            list.remove(findInformation(login));
            return ResponseEntity.status(201).body("removed");
        } else {
            return ResponseEntity.status(400).body("error wrong login or token");
        }

    }

    @PatchMapping(value = "update/{login}")
    public ResponseEntity<String> updateLogin(@RequestBody String data, @RequestHeader String token, @PathVariable String login) {
        JSONObject obj = new JSONObject(data);
        if (findInformation(login).getToken().equals(token)) {
            if (obj.has("firstName")) {
                findInformation(login).setFname(obj.getString("firstName"));
            }
            if (obj.has("lastName")) {
                findInformation(login).setFname(obj.getString("lastName"));
            }
            return ResponseEntity.status(201).body("data changed");
        } else {
            return ResponseEntity.status(400).body("error wrong token or login");
        }
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