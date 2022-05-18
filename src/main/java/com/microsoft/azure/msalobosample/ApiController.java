// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.msalobosample;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200") //frontend url
public class ApiController {

    @Autowired
    OboAuthProvider oboAuthProvider;

    @GetMapping("/getHello")
    public String hello(){
        return "hello from here";
    }

    @RequestMapping("/test/graphMeApi")
    public ResponseEntity<String> graphMeApi() {
        log.info("---------------- graphMeApi called ----------------");
        try {
            GraphServiceClient<Request> graphClient = GraphServiceClient
                    .builder()
                    .authenticationProvider(oboAuthProvider)
                    .buildClient();

            User user = graphClient.me().buildRequest().get();
            ObjectMapper objectMapper = new ObjectMapper();
            return ResponseEntity.status(200).body(objectMapper.writeValueAsString(user));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(String.format("%s: %s", ex.getCause(), ex.getMessage()));
        }
    }
}
