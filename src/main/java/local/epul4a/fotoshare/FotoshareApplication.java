package local.epul4a.fotoshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "local.epul4a.fotoshare", "local.epul4a.springbootdatajpa",
    }
)
public class FotoshareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FotoshareApplication.class, args);
    }
}
