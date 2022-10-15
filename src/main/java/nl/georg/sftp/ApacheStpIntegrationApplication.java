package nl.georg.sftp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication(scanBasePackages = "nl.georg")
@IntegrationComponentScan(basePackages = "nl.georg.sftp")
public class ApacheStpIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApacheStpIntegrationApplication.class, args);
    }
}
