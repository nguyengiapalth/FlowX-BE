package project.ii.flowx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlowXApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowXApplication.class, args);
    }
}
