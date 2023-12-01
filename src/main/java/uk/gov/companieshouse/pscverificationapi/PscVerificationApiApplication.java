package uk.gov.companieshouse.pscverificationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PscVerificationApiApplication {

	public static final String APP_NAMESPACE = "psc-verification-api";

	public static void main(String[] args) {
		SpringApplication.run(PscVerificationApiApplication.class, args);
	}

}
