package com.portfoliorisk.marketdata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "position")
public class Position {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "portfolio_id", nullable = false, length = 64)
  private String portfolioId;

  @Column(nullable = false, precision = 18, scale = 8)
  private BigDecimal quantity;

  @Column(name = "instrument_id", nullable = false, length = 32)
  private String instrumentId;

  @Column(name = "asset_class", nullable = false, length = 32)
  private String assetClass;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Generated(event = EventType.INSERT)
  private OffsetDateTime createdAt;

  protected Position() {
  }

  public Position(String portfolioId, BigDecimal quantity, String instrumentId,
      String assetClass) {
    this.portfolioId = portfolioId;
    this.quantity = quantity;
    this.instrumentId = instrumentId;
    this.assetClass = assetClass;
  }

  public Long getId() {
    return id;
  }

  public String getPortfolioId() {
    return portfolioId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public String getInstrumentId() {
    return instrumentId;
  }

  public String getAssetClass() {
    return assetClass;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setPortfolioId(String portfolioId) {
    this.portfolioId = portfolioId;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public void setInstrumentId(String instrumentId) {
    this.instrumentId = instrumentId;
  }

  public void setAssetClass(String assetClass) {
    this.assetClass = assetClass;
  }


}
