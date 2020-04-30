package sample;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

public class DateAndTime {
    List<User> list =  new ArrayList<User>();

    public DateAndTime() {
        list.add(new User("Roman","Simko","roman","heslo"));
    }

    @RequestMapping("/time")
    public String getTime() {
        return "11:29:15";
    }

    @RequestMapping("/primenumber/{number}")
    public String checkPrimeNumber() {
        return "true / false";
    }

    @RequestMapping("/time/hour")
    public String getHour(){
        return "11";
    }

    @RequestMapping("/hello")
    public String getHello(){
        return "Hello. How are you? ";
    }

    @RequestMapping("/hello/{name}")
    public String getHelloWithName(@PathVariable String name){
        return "Hello "+name+". How are you? ";
    }

    @RequestMapping("/hi")
    public String getHi(@RequestParam(value="fname") String fname, @RequestParam(value="age") String age){
        return "Hello. How are you? Your name is "+fname+" and you are "+age;
    }

    @RequestMapping(method= RequestMethod.POST, value="/login")
    public String login(@RequestBody String credential){
        System.out.println(credential);
        return "{\"Error\":\"Login already exists\"}";
    }

    @RequestMapping(method=RequestMethod.POST, value="/signup")
    public String signup(@RequestBody String data){
        System.out.println(data);

        return data;
    }

}
