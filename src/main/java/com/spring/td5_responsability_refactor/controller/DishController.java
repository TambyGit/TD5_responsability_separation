package com.spring.td5_responsability_refactor.controller;

import com.spring.td5_responsability_refactor.entity.Dish;
import com.spring.td5_responsability_refactor.entity.Ingredient;
import com.spring.td5_responsability_refactor.repository.DishRepository;
import com.spring.td5_responsability_refactor.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    public DishController(DishRepository dishRepository, IngredientRepository ingredientRepository) {
        this.dishRepository = dishRepository;
       this.ingredientRepository = ingredientRepository;
    }

    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes() {
        List<Dish> dishes = dishRepository.findAll();
        return ResponseEntity.ok(dishes);
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateIngredients(@PathVariable int id, @RequestBody(required = false) List<Ingredient> ingredients) {
        if (ingredients == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("ingredients is null");
        }
        try{
            Dish exist = dishRepository.findById(id);
            exist.setIngredients(ingredients);
            Dish update = dishRepository.saveDish(exist);
            return ResponseEntity.ok().
                    body(update);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(e.getMessage());
            }
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
