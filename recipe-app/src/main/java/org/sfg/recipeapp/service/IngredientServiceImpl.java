package org.sfg.recipeapp.service;

import java.util.Optional;

import org.sfg.recipeapp.commands.IngredientCommand;
import org.sfg.recipeapp.converters.IngredientCommandToIngredient;
import org.sfg.recipeapp.converters.IngredientToIngredientCommand;
import org.sfg.recipeapp.domain.Ingredient;
import org.sfg.recipeapp.domain.Recipe;
import org.sfg.recipeapp.repositories.RecipeRepository;
import org.sfg.recipeapp.repositories.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class IngredientServiceImpl implements IngredientService {



	private final IngredientToIngredientCommand ingredientToIngredientCommand;
	private final IngredientCommandToIngredient ingredientCommandToIngredient;
	private final RecipeRepository recipeRepository;
	private final UnitOfMeasureRepository uomRepo;





	public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand, IngredientCommandToIngredient ingredientCommandToIngredient, RecipeRepository recipeRepository, UnitOfMeasureRepository unitOfMeasureRepository) {
		this.ingredientToIngredientCommand = ingredientToIngredientCommand;
		this.ingredientCommandToIngredient = ingredientCommandToIngredient;
		this.recipeRepository = recipeRepository;
		this.uomRepo = unitOfMeasureRepository;
	}

	@Override
	public IngredientCommand findByRecipeIdAndIngredientId(Long recipeId, Long ingredId) {
		Optional<Recipe> recipeOptional = recipeRepository.findById(recipeId);

		if (!recipeOptional.isPresent()) {
			//todo impl error handling
			log.trace("recipe id not found. Id: " + recipeId);
		}

		Recipe recipe = recipeOptional.get();

		// @formatter:off
		Optional<IngredientCommand> ingredCommand	=	recipe.getIngredients().stream()
															.filter(ingred->ingred.getId().equals(ingredId))
																.map(ingred->ingredientToIngredientCommand.convert(ingred)).findFirst();
		// 	@formatter:on

		if (!ingredCommand.isPresent()) {
			log.trace("Ingredient id not found: " + ingredId);
		}

		return ingredCommand.get();
	}

	@Override
	@Transactional
	public IngredientCommand saveIngredientCommand(IngredientCommand command) {
		Optional<Recipe> recipeOptional = recipeRepository.findById(command.getRecipeId());

		if (!recipeOptional.isPresent()) {

			//todo toss error if not found!
			log.error("Recipe not found for id: " + command.getRecipeId());
			return new IngredientCommand();
		} else {
			Recipe recipe = recipeOptional.get();

			Optional<Ingredient> ingredientOptional = recipe.getIngredients().stream().filter(ingredient -> ingredient.getId().equals(command.getId())).findFirst();

			if (ingredientOptional.isPresent()) {
				Ingredient ingredientFound = ingredientOptional.get();
				ingredientFound.setDescription(command.getDescription());
				ingredientFound.setAmount(command.getAmount());
				ingredientFound.setUom(uomRepo.findById(command.getUom().getId()).orElseThrow(() -> new RuntimeException("UOM NOT FOUND"))); //todo address this
			} else {
				//add new Ingredient
				recipe.addIngredient(ingredientCommandToIngredient.convert(command));
			}

			Recipe savedRecipe = recipeRepository.save(recipe);

			//to do check for fail
			return ingredientToIngredientCommand.convert(savedRecipe.getIngredients().stream().filter(recipeIngredients -> recipeIngredients.getId().equals(command.getId())).findFirst().get());
		}

	}

}
