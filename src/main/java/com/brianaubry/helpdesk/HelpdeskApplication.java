package com.brianaubry.helpdesk;

import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HelpdeskApplication {

	public static void main(String[] args) {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(9000);
		tomcat.getConnector();
		SpringApplication.run(HelpdeskApplication.class, args);
	}

}
