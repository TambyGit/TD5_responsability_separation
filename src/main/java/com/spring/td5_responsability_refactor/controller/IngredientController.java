package com.spring.td5_responsability_refactor.controller;

import com.spring.td5_responsability_refactor.entity.Ingredient;
import com.spring.td5_responsability_refactor.entity.StockMovement;
import com.spring.td5_responsability_refactor.entity.StockValue;
import com.spring.td5_responsability_refactor.entity.enums.UnitEnum;
import com.spring.td5_responsability_refactor.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RequestMapping("/ingredients")
@RestController
public class IngredientController {
    private final IngredientRepository ingredientRepository;

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping
    public ResponseEntity<List<Ingredient>> getAllIngredients(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        List<Ingredient> ingredients = ingredientRepository.findAll(page, size);
        return ResponseEntity.ok(ingredients);
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getIngredientStock(@PathVariable int id ,@RequestParam(required = false) String at, @RequestParam(required = false)String unit) {
        if (at == null || unit == null || at.isEmpty() || unit.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Either mandatory query parameter `at` or \n" +
                            "`unit` is not provided.");
        }
        try{
            Instant instant = Instant.parse(at);
            UnitEnum unitEnum = UnitEnum.valueOf(unit);
            StockValue stockValue = ingredientRepository.getStockAt(id, instant, unitEnum);
            return  ResponseEntity.ok(stockValue);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Invalid request")) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Either mandatory query parameter `at` or \\n\" +\n" +
                                "                            \"`unit` is not provided.");
            }
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable int id) {
        try {
          Ingredient ingredient = ingredientRepository.findById(id);
          return ResponseEntity.ok(ingredient);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createIngredient(@RequestBody(required = false) List<Ingredient> ingredients) {
        if (ingredients == null ||  ingredients.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request");
        }
        try{
            List<Ingredient> created = ingredientRepository.createIngredients(ingredients);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return  ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}/stockMovements")
    public ResponseEntity<?> getStockMovements(
            @PathVariable int id,
            @RequestParam String from,
            @RequestParam String to) {


        try {
            Instant instantFrom = Instant.parse(from);
            Instant instantTo = Instant.parse(to);

            List<StockMovement> movements = ingredientRepository
                    .findStockMovementsByIngredientIdBetween(id, instantFrom, instantTo);
            return ResponseEntity.ok(movements);

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("is not found")) {
                e.printStackTrace();
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Ingredient.id=" + id + " is not found");
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Format de date invalide. Utilisez le format ISO 8601 : 2024-01-01T00:00:00Z");
        }
    }

    @PostMapping("/{id}/stockMovements")
    public ResponseEntity<?> addStockMovements(
            @PathVariable int id,
            @RequestBody List<StockMovement> movements) {

        try {
            List<StockMovement> saved = ingredientRepository.addStockMovements(id, movements);
            return ResponseEntity.ok(saved);

        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("is not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(msg);
            }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(msg != null ? msg : "Erreur lors de la création des mouvements");
        }
    }
}
