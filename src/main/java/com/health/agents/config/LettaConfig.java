package com.health.agents.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.health.agents.integration.letta.LettaApiClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;

@Configuration
public class LettaConfig {
    
    @Value("${letta.api.base-url}")
    private String lettaBaseUrl;
    
    @Value("${letta.api.api-key}")
    private String lettaApiKey;
    
    @Value("${letta.api.timeout}")
    private Duration timeout;
    
    @Bean
    public OkHttpClient okHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout);
            
        // Add authentication if API key is provided
        if (lettaApiKey != null && !lettaApiKey.trim().isEmpty()) {
            builder.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + lettaApiKey)
                    .header("Content-Type", "application/json")
                    .build();
                return chain.proceed(request);
            });
        }
        
        // Add logging interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);
        
        return builder.build();
    }
    
    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl(lettaBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper()))
            .build();
    }
    
    @Bean
    public LettaApiClient lettaApiClient(Retrofit retrofit) {
        return retrofit.create(LettaApiClient.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}

