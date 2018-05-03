package com.cash.slide;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cash.slide"})
public class ApplicationBoot extends SpringBootServletInitializer implements CommandLineRunner {

    @Override
    public void run(String... arg0) throws Exception {
        if (arg0.length > 0 && "exitcode".equals(arg0[0])) {
            throw new ExitException();
        }
    }

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(ApplicationBoot.class).web(true).run(args);
    }

    class ExitException extends RuntimeException implements ExitCodeGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public int getExitCode() {
            return 10;
        }
    }
}
