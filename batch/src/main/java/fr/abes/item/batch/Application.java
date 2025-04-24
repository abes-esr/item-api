package fr.abes.item.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {
    @Value("${sudoc.port}")
    private int portDeConnexionAuSudoc;
    public static void main(String[] args) {
        SpringApplication.exit(SpringApplication.run(Application.class, args));
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Connection au sudoc via port:{}", portDeConnexionAuSudoc);
    }
}
