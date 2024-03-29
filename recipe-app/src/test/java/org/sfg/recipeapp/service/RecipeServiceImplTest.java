package org.sfg.recipeapp.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sfg.recipeapp.commands.RecipeCommand;
import org.sfg.recipeapp.converters.RecipeCommandToRecipe;
import org.sfg.recipeapp.converters.RecipeToRecipeCommand;
import org.sfg.recipeapp.domain.Recipe;
import org.sfg.recipeapp.exceptions.NotFoundException;
import org.sfg.recipeapp.repositories.RecipeRepository;

class RecipeServiceImplTest {


	RecipeServiceImpl recipeService;


	@Mock
	RecipeRepository recipeRepository;

	@Mock
	RecipeToRecipeCommand recipeToRecipeCommand;

	@Mock
	RecipeCommandToRecipe recipeCommandToRecipe;



	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);

		recipeService = new RecipeServiceImpl(recipeRepository, recipeCommandToRecipe, recipeToRecipeCommand);
	}

	@Test
	public void getRecipeByIdTest() throws Exception {
		Recipe recipe = new Recipe();
		recipe.setId(1L);
		Optional<Recipe> recipeOptional = Optional.of(recipe);

		when(recipeRepository.findById(ArgumentMatchers.anyLong())).thenReturn(recipeOptional);

		Recipe recipeReturned = recipeService.findById(1L);

		assertNotNull(recipeReturned, "Null recipe returned");
		verify(recipeRepository, times(1)).findById(ArgumentMatchers.anyLong());
		verify(recipeRepository, never()).findAll();
	}

	@Test
	public void getRecipesTest() throws Exception {

		Recipe recipe = new Recipe();
		HashSet receipesData = new HashSet();
		receipesData.add(recipe);

		when(recipeService.getRecipes()).thenReturn(receipesData);

		Set<Recipe> recipes = recipeService.getRecipes();

		assertEquals(recipes.size(), 1);
		verify(recipeRepository, times(1)).findAll();
		verify(recipeRepository, never()).findById(ArgumentMatchers.anyLong());
	}


	@Test
	public void getRecipeCoomandByIdTest() throws Exception {
		Recipe recipe = new Recipe();
		recipe.setId(1L);
		Optional<Recipe> recipeOptional = Optional.of(recipe);

		when(recipeRepository.findById(ArgumentMatchers.anyLong())).thenReturn(recipeOptional);

		RecipeCommand recipeCommand = new RecipeCommand();
		recipeCommand.setId(1L);

		when(recipeToRecipeCommand.convert(ArgumentMatchers.any())).thenReturn(recipeCommand);

		RecipeCommand commandById = recipeService.findCommandById(1L);

		assertNotNull(commandById, "Null recipe returned");
		verify(recipeRepository, times(1)).findById(ArgumentMatchers.anyLong());
		verify(recipeRepository, never()).findAll();
	}


	@Test
	public void testDeleteById() throws Exception {
		//given
		Long idToDelete = Long.valueOf(2L);
		//when
		recipeService.deleteById(idToDelete);
		//then
		verify(recipeRepository, times(1)).deleteById(ArgumentMatchers.anyLong());
	}

	@Test
	void testRecipeNull() throws Exception {
		Optional<Recipe> recipeOptional = Optional.empty();

		when(recipeRepository.findById(ArgumentMatchers.anyLong())).thenReturn(recipeOptional);

		assertThatThrownBy(() -> recipeService.findById(1L)).isInstanceOf(NotFoundException.class).hasMessageContaining("Recipe Not Found. For ID value: ");
	}

}
