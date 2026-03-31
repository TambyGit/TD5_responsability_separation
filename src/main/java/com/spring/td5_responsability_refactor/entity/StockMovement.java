package com.spring.td5_responsability_refactor.entity;

import com.spring.td5_responsability_refactor.entity.enums.MovementTypeEnum;

import java.time.Instant;

public class StockMovement {
    private int id;
    private StockValue value;
    private MovementTypeEnum movementType;
    private Instant creationDate;

    public StockMovement(int id, StockValue value, MovementTypeEnum movementType, Instant creationDate) {
        this.id = id;
        this.value = value;
        this.movementType = movementType;
        this.creationDate = creationDate;
    }

    public StockMovement() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StockValue getValue() {
        return value;
    }

    public void setValue(StockValue value) {
        this.value = value;
    }

    public MovementTypeEnum getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementTypeEnum movementType) {
        this.movementType = movementType;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

}
