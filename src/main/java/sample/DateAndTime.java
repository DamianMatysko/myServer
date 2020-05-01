package sample;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RestController
public class DateAndTime {
    List<User> list = new ArrayList<User>();

    public DateAndTime() {
        list.add(new User("Roman", "Simko", "roman", "heslo"));
    }

    @RequestMapping("/time")
    public String getTime() {
        String timeStamp = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(Calendar.getInstance().getTime());
        return timeStamp;
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
        if (findLogin(obj.getString("login")) && findPassword(obj.getString("password"))) {
            JSONObject res = new JSONObject();
            User user = findInformation(obj.getString("login"));
            res.put("fname", user.getFname());
            res.put("lname", user.getLname());
            res.put("login", user.getLogin());
            res.put("token", user.getToken());
            return ResponseEntity.status(200).body(res.toString());
        } else {
            JSONObject res = new JSONObject();
            res.put("error", "wrong login or password");
            return ResponseEntity.status(401).body(res.toString());
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/signup")
    public ResponseEntity<String> signup(@RequestBody String data) {
        System.out.println(data);
        JSONObject obj = new JSONObject(data);
        if (obj.has("fname") && obj.has("lname") && obj.has("login") && obj.has("password")) {
            if (findLogin(obj.getString("login"))) {
                JSONObject res = new JSONObject();
                res.put("error", "user already exists");
                return ResponseEntity.status(400).body(res.toString());
            }
            String password = obj.getString("password");
            if (password.isEmpty()) {
                JSONObject res = new JSONObject();
                res.put("error", "password is a mandatory field");
                return ResponseEntity.status(400).body(res.toString());
            }
            User user = new User(obj.getString("fname"), obj.getString("lname"), obj.getString("login"), obj.getString("password"));
            list.add(user);
            JSONObject res = new JSONObject();
            res.put("fname", obj.getString("fname"));
            res.put("lname", obj.getString("lname"));
            res.put("login", obj.getString("login"));
            return ResponseEntity.status(201).body(res.toString());
        } else {
            JSONObject res = new JSONObject();
            res.put("error", "invalid input");
            return ResponseEntity.status(400).body(res.toString());
        }
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
            if (user.getPassword().equalsIgnoreCase(password))
                return true;
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
    public ResponseEntity<String> logout(@RequestBody String data) {
        JSONObject obj = new JSONObject(data);
        JSONObject res = new JSONObject();
        System.out.println(obj.getString("login"));
        System.out.println(data);
        res.put("message", "Logouot succesful");
        res.put("login", "kral");
        return ResponseEntity.status(200).body(res.toString());
    }
}