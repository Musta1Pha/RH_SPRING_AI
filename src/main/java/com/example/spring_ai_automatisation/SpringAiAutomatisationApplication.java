package com.example.spring_ai_automatisation;

import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(exclude = {PgVectorStoreAutoConfiguration.class, OpenAiAutoConfiguration.class})
public class SpringAiAutomatisationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAutomatisationApplication.class, args);
    }
}
