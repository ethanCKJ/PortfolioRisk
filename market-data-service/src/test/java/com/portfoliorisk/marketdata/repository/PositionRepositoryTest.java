package com.portfoliorisk.marketdata.repository;

import com.portfoliorisk.marketdata.domain.Position;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class PositionRepositoryTest {

  @Container
  @ServiceConnection
  private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");

  @Autowired
  private PositionRepository positionRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  @Test
  public void findByPositionTest() {
    Position expectedPosition = new Position("portfolio1",new BigDecimal("3.90"), "instrument1","equity");
    positionRepository.save(expectedPosition);
    testEntityManager.flush();
    // Clear in-memory database cache
    testEntityManager.clear();
    List<Position> actualPositions = positionRepository.findByPortfolioId(expectedPosition.getPortfolioId());
    assertEquals(1, actualPositions.size());
    assertEquals(0, actualPositions.get(0).getQuantity().compareTo(expectedPosition.getQuantity()));
  }
}
