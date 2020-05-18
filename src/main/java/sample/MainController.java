package sample;

import database.MongoDBcontroller;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;

@SpringBootApplication
public class MainController {
    public static void main(String[] args) throws FileNotFoundException {
        SpringApplication.run(MainController.class,args);
    }
}
