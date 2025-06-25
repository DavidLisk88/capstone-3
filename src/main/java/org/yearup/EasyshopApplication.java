package org.yearup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.yearup", "DatabaseConfig"})
public class EasyshopApplication
{

    public static void main(String[] args) {
        SpringApplication.run(EasyshopApplication.class, args);
    }

}
