package com.example.metatry.integrationTest;

import com.example.metatry.MetaTryApplication;
import org.springframework.boot.SpringApplication;

public class TestMetaTryApplication {

    public static void main(String[] args) {
        SpringApplication.from(MetaTryApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
