package sample;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RestController
public class DateAndTime {
    List<User> list = new ArrayList<User>();
    List<String> log = new ArrayList<String>();

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
        if (findLogin(obj.getString("login")) && checkUserPass(obj.getString("login"), obj.getString("password"))) {
            JSONObject res = new JSONObject();
            User user = findInformation(obj.getString("login"));
            user.generateToken();
            res.put("fname", user.getFname());
            res.put("lname", user.getLname());
            res.put("login", user.getLogin());
            res.put("token", user.getToken());


            JSONObject history = new JSONObject();
            history.put("type", "login");
            history.put("login", user.getLogin());
            history.put("datetime", getTime(user.getLogin(), user.getToken()));
            log.add(history.toString());

            return ResponseEntity.status(200).body(res.toString());
        } else {
            JSONObject res = new JSONObject();
            res.put("error", "wrong login or password");
            return ResponseEntity.status(401).body(res.toString());
        }
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
                    //if (user.getPassword().equalsIgnoreCase(password))
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
        if (findLogin(obj.getString("login")) && findInformation(obj.getString("login")).getToken().equals(userToken)) {
            System.out.println("if ok");
            findInformation(obj.get("login").toString()).tokenResetter();

            User user = findInformation(obj.getString("login"));
            JSONObject history = new JSONObject();
            history.put("type", "login");
            //history.put("login", user.getFname() + " " + user.getLname());
            history.put("login", user.getLogin());
            String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
            history.put("datetime", timeStamp);
            log.add(history.toString());


            return ResponseEntity.status(200).body("OK");
        } else {
            return ResponseEntity.status(401).body("error");
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/log")
    public ResponseEntity log(@RequestBody String data, @RequestParam(value = "token") String userToken) {
        JSONObject obj = new JSONObject(data);
        JSONObject format = new JSONObject();
        if (findLogin(obj.getString("login")) && findInformation(obj.getString("login")).getToken().equals(userToken)) {

            List<String> myList = new ArrayList<String>();

            int count = 0;
            for (String list : log) {

                JSONObject information = new JSONObject(list);

                System.out.println(information.getString("login")+"   "+obj.getString("login"));

                System.out.println(list);
                if (information.getString("login").equals(obj.getString("login"))) {
                    System.out.println(list);


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

}