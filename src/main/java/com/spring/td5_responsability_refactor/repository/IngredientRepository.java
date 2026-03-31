package com.spring.td5_responsability_refactor.repository;

import com.spring.td5_responsability_refactor.entity.Ingredient;
import com.spring.td5_responsability_refactor.entity.StockMovement;
import com.spring.td5_responsability_refactor.entity.StockValue;
import com.spring.td5_responsability_refactor.entity.enums.CategoryEnum;
import com.spring.td5_responsability_refactor.entity.enums.MovementTypeEnum;
import com.spring.td5_responsability_refactor.entity.enums.UnitEnum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientRepository {

    private final JdbcTemplate jdbcTemplate;

    public IngredientRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Ingredient mapIngredient(ResultSet rs, int rowNum) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
        return ingredient;
    }

    public List<Ingredient> findAll(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT id, name, price, category FROM ingredient ORDER BY id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapIngredient, size, offset);
    }

    public Ingredient findById(int id) {
        String sql = "SELECT id, name, price, category FROM ingredient WHERE id = ?";
        List<Ingredient> results = jdbcTemplate.query(sql, this::mapIngredient, id);
        if (results.isEmpty()) {
            throw new RuntimeException("Ingredient.id=" + id + " is not found");
        }
        return results.get(0);
    }

    public StockValue getStockAt(int ingredientId, Instant at, UnitEnum unit) {
        findById(ingredientId);

        String sql = """
        SELECT id, quantity, movement_type, unit, creation_datetime
        FROM stock_movement
        WHERE id_ingredient = ?
        AND creation_datetime <= ?
        ORDER BY creation_datetime ASC
        """;

        List<StockMovement> movements = jdbcTemplate.query(sql, (rs, rowNum) -> {
            StockMovement sm = new StockMovement();
            sm.setId(rs.getInt("id"));
            sm.setValue(new StockValue(
                    rs.getDouble("quantity"),
                    UnitEnum.valueOf(rs.getString("unit"))
            ));
            sm.setMovementType(MovementTypeEnum.valueOf(rs.getString("movement_type"))); // ✅ movement_type
            sm.setCreationDate(rs.getTimestamp("creation_datetime").toInstant());
            return sm;
        }, ingredientId, Timestamp.from(at));

        double total = 0.0;
        for (StockMovement sm : movements) {
            if (sm.getMovementType() == MovementTypeEnum.IN) {
                total += sm.getValue().getQuantity();
            } else {
                total -= sm.getValue().getQuantity();
            }
        }
        return new StockValue(total, unit);
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        List<Ingredient> created = new ArrayList<>();

        for (Ingredient ingredient : newIngredients) {
            String checkSql = "SELECT COUNT(*) FROM ingredient WHERE LOWER(name) = LOWER(?)";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, ingredient.getName());
            if (count != null && count > 0) {
                throw new RuntimeException("Ingredient '" + ingredient.getName() + "' already exists");
            }

            String insertSql = """
                INSERT INTO ingredient (name, price, category)
                VALUES (?, ?, ?::ingredient_category)
                RETURNING id, name, price, category
                """;
            Ingredient saved = jdbcTemplate.queryForObject(insertSql, this::mapIngredient,
                    ingredient.getName(),
                    ingredient.getPrice(),
                    ingredient.getCategory().name()
            );
            created.add(saved);
        }
        return created;
    }
}