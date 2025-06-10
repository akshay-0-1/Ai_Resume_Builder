package com.project.resumeTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResumeTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeTrackerApplication.class, args);
	}

}
