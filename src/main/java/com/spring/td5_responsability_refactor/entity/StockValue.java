package com.spring.td5_responsability_refactor.entity;

import com.spring.td5_responsability_refactor.entity.enums.UnitEnum;

public class StockValue {
    private double quantity;
    private UnitEnum unit;

    public StockValue(double quantity, UnitEnum unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    public StockValue() {

    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public UnitEnum getUnit() {
        return unit;
    }

    public void setUnit(UnitEnum unit) {
        this.unit = unit;
    }
}
