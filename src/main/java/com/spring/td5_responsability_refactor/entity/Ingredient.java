package com.spring.td5_responsability_refactor.entity;

import com.spring.td5_responsability_refactor.entity.enums.CategoryEnum;
import com.spring.td5_responsability_refactor.entity.enums.UnitEnum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ingredient {
    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList = new ArrayList<>();

    public Ingredient(int id, String name, double price, CategoryEnum category, List<StockMovement> stockMovementList) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockMovementList = stockMovementList;
    }

    public Ingredient() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public StockValue getStockValueAt(Instant t) {
        double total = 0.0;
        UnitEnum unit = UnitEnum.KG;

        for (StockMovement movement : stockMovementList) {
            if (!movement.getCreationDate().isAfter(t)) {
                double qty = movement.getValue().getQuantity();
                unit = movement.getValue().getUnit();
                if (movement.getMovementType().name().equals("IN")) {
                    total += qty;
                } else {
                    total -= qty;
                }
            }
        }
        return new StockValue(total, unit);
    }
}
