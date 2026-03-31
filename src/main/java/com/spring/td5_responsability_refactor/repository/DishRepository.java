package com.spring.td5_responsability_refactor.repository;

import com.spring.td5_responsability_refactor.entity.Dish;
import com.spring.td5_responsability_refactor.entity.Ingredient;
import com.spring.td5_responsability_refactor.entity.enums.CategoryEnum;
import com.spring.td5_responsability_refactor.entity.enums.DishTypeEnum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final JdbcTemplate jdbcTemplate;

    public DishRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Dish mapDish(ResultSet rs, int rowNum) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));

        String dishTypeStr = rs.getString("dish_type");
        DishTypeEnum dishType = null;
        if (dishTypeStr != null && !dishTypeStr.trim().isEmpty()) {
            try {
                dishType = DishTypeEnum.valueOf(dishTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Dish id=" + dish.getId() + " has invalid dish_type: '" + dishTypeStr + "'");
            }
        }
        dish.setDishType(dishType);

        double sp = rs.getDouble("selling_price");
        dish.setSellingPrice(rs.wasNull() ? null : sp);
        dish.setIngredients(new ArrayList<>());
        return dish;
    }

    private Ingredient mapIngredientInDish(ResultSet rs, int rowNum) throws SQLException {
        Ingredient i = new Ingredient();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        i.setPrice(rs.getDouble("price"));

        String categoryStr = rs.getString("category");
        CategoryEnum category = null;
        if (categoryStr != null && !categoryStr.trim().isEmpty()) {
            try {
                category = CategoryEnum.valueOf(categoryStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Ingredient id=" + i.getId() + " has invalid category: '" + categoryStr + "'");
            }
        }
        i.setCategory(category);
        return i;
    }

    public List<Dish> findAll() {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish ORDER BY id";
        List<Dish> dishes = jdbcTemplate.query(sql, this::mapDish);
        for (Dish dish : dishes) {
            try {
                dish.setIngredients(findIngredientsByDishId(dish.getId()));
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement des ingrédients du plat id=" + dish.getId());
                dish.setIngredients(new ArrayList<>());
            }
        }
        return dishes;
    }

    private List<Ingredient> findIngredientsByDishId(int dishId) {
        String sql = """
            SELECT i.id, i.name, i.price, i.category
            FROM ingredient i
            JOIN dish_ingredient di ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
            ORDER BY i.id
            """;
        return jdbcTemplate.query(sql, this::mapIngredientInDish, dishId);
    }

    public Dish findById(int id) {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";
        List<Dish> results = jdbcTemplate.query(sql, this::mapDish, id);
        if (results.isEmpty()) {
            throw new RuntimeException("Dish.id= " + id + " is not found");
        }
        Dish dish = results.get(0);
        dish.setIngredients(findIngredientsByDishId(id));
        return dish;
    }

    public Dish saveDish(Dish dishToSave) {
        if (dishToSave.getDishType() == null) {
            throw new RuntimeException("DishType cannot be null for saving");
        }

        Dish saved;

        if (dishToSave.getId() == 0) {
            String sql = """
            INSERT INTO dish (name, dish_type, selling_price)
            VALUES (?, ?::dish_type, ?)
            RETURNING id, name, dish_type, selling_price
            """;
            saved = jdbcTemplate.queryForObject(sql, this::mapDish,
                    dishToSave.getName(),
                    dishToSave.getDishType().name(),
                    dishToSave.getSellingPrice()
            );
        } else {
            String sql = """
            UPDATE dish SET name=?, dish_type=?::dish_type, selling_price=?
            WHERE id=?
            RETURNING id, name, dish_type, selling_price
            """;
            saved = jdbcTemplate.queryForObject(sql, this::mapDish,
                    dishToSave.getName(),
                    dishToSave.getDishType().name(),
                    dishToSave.getSellingPrice(),
                    dishToSave.getId()
            );
        }

        if (saved == null) {
            throw new RuntimeException("Erreur lors de la sauvegarde du plat");
        }

        jdbcTemplate.update("DELETE FROM dish_ingredient WHERE id_dish = ?", saved.getId());

        if (dishToSave.getIngredients() != null && !dishToSave.getIngredients().isEmpty()) {
            for (Ingredient ingredient : dishToSave.getIngredients()) {
                if (ingredient.getId() == 0) {
                    throw new RuntimeException("Ingredient id cannot be 0");
                }
                jdbcTemplate.update(
                        "INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, 1.0, 'KG')",
                        saved.getId(), ingredient.getId()
                );
            }
        }

        saved.setIngredients(findIngredientsByDishId(saved.getId()));
        return saved;
    }
}