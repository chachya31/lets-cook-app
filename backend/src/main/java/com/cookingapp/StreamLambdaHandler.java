package com.cookingapp;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AWS Lambda用のハンドラークラス
 * Spring BootアプリケーションをLambda環境で実行するためのエントリーポイント
 */
public class StreamLambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
    private static final Logger log = LoggerFactory.getLogger(StreamLambdaHandler.class);
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            System.setProperty("spring.main.web-application-type", "servlet");
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(CookingAppApplication.class);
            log.info("StreamLambdaHandler initialized successfully");
        } catch (ContainerInitializationException e) {
            log.error("Failed to initialize Spring Boot application", e);
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest request, Context context) {
        log.info("Request - Method: {}, Path: {}", request.getHttpMethod(), request.getPath());
        log.debug("Request Headers: {}", request.getHeaders());
        log.debug("Query Parameters: {}", request.getQueryStringParameters());
        log.debug("Request Body: {}", request.getBody());
        
        AwsProxyResponse response = handler.proxy(request, context);
        
        log.info("Response - Status: {}", response.getStatusCode());
        log.debug("Response Body: {}", response.getBody());
        
        return response;
    }
}
