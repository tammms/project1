package vttp.ssf.project;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import vttp.ssf.project.service.MapService;

@SpringBootApplication
public class ProjectApplication implements CommandLineRunner {


	@Value("${api.key}")
    private String apiKey;

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

	@Autowired
	MapService mapSvc;


	@Override
	public void run(String... args) throws Exception {

		
		apiKey = mapSvc.getToken();
		//System.out.println("\nResult:\n"+apiKey );

		
	}

}
