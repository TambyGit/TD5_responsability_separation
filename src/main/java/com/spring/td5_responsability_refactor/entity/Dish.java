package com.spring.td5_responsability_refactor.entity;

import com.spring.td5_responsability_refactor.entity.enums.DishTypeEnum;

import java.util.ArrayList;
import java.util.List;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;
    private List<Ingredient> ingredients = new ArrayList<>();

    public Dish(int id, String name, DishTypeEnum dishType, Double sellingPrice, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.sellingPrice = sellingPrice;
        this.ingredients = ingredients;
    }

    public Dish() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Double getDishPrice() {
        if (ingredients == null || ingredients.isEmpty()) return 0.0;
        return ingredients.stream()
                .mapToDouble(Ingredient::getPrice)
                .sum();
    }

    public Double getGrossMargin() {
        if (sellingPrice == null) {
            throw new RuntimeException("Dish.id=" + id + " : le prix de vente est NULL.");
        }
        return sellingPrice - getDishPrice();
    }
}
