package com;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class controller {
  @GetMapping("/health")
  public ResponseEntity<HealthResponse> getHealth(){
    HealthResponse healthResponse = new HealthResponse(HealthStatus.UP, "market-data-service");
    return new ResponseEntity<>(healthResponse, HttpStatus.OK);
  }
}
