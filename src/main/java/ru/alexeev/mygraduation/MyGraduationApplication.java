package ru.alexeev.mygraduation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class MyGraduationApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MyGraduationApplication.class, args);
    }

}

