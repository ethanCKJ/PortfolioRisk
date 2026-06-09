package com.portfoliorisk.marketdata.controller;

import com.portfoliorisk.marketdata.HealthResponse;
import com.portfoliorisk.marketdata.HealthStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/health")
  public ResponseEntity<HealthResponse> getHealth(){
    HealthResponse healthResponse = new HealthResponse(HealthStatus.UP, "market-data-service");
    return new ResponseEntity<>(healthResponse, HttpStatus.OK)
  }
}
