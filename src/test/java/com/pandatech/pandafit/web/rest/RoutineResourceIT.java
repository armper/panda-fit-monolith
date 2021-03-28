package com.pandatech.pandafit.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pandatech.pandafit.IntegrationTest;
import com.pandatech.pandafit.domain.Routine;
import com.pandatech.pandafit.repository.RoutineRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link RoutineResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class RoutineResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DATE_STARTED = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_STARTED = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_DATE_ENDED = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_ENDED = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_GOAL_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_GOAL_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_STARTING_BODY_WEIGHT = 1;
    private static final Integer UPDATED_STARTING_BODY_WEIGHT = 2;

    private static final Integer DEFAULT_ENDING_BODY_WEIGHT = 1;
    private static final Integer UPDATED_ENDING_BODY_WEIGHT = 2;

    private static final String ENTITY_API_URL = "/api/routines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private RoutineRepository routineRepository;

    @Mock
    private RoutineRepository routineRepositoryMock;

    @Autowired
    private MockMvc restRoutineMockMvc;

    private Routine routine;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Routine createEntity() {
        Routine routine = new Routine()
            .name(DEFAULT_NAME)
            .dateStarted(DEFAULT_DATE_STARTED)
            .dateEnded(DEFAULT_DATE_ENDED)
            .goalDate(DEFAULT_GOAL_DATE)
            .startingBodyWeight(DEFAULT_STARTING_BODY_WEIGHT)
            .endingBodyWeight(DEFAULT_ENDING_BODY_WEIGHT);
        return routine;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Routine createUpdatedEntity() {
        Routine routine = new Routine()
            .name(UPDATED_NAME)
            .dateStarted(UPDATED_DATE_STARTED)
            .dateEnded(UPDATED_DATE_ENDED)
            .goalDate(UPDATED_GOAL_DATE)
            .startingBodyWeight(UPDATED_STARTING_BODY_WEIGHT)
            .endingBodyWeight(UPDATED_ENDING_BODY_WEIGHT);
        return routine;
    }

    @BeforeEach
    public void initTest() {
        routineRepository.deleteAll();
        routine = createEntity();
    }

    @Test
    void createRoutine() throws Exception {
        int databaseSizeBeforeCreate = routineRepository.findAll().size();
        // Create the Routine
        restRoutineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(routine)))
            .andExpect(status().isCreated());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeCreate + 1);
        Routine testRoutine = routineList.get(routineList.size() - 1);
        assertThat(testRoutine.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRoutine.getDateStarted()).isEqualTo(DEFAULT_DATE_STARTED);
        assertThat(testRoutine.getDateEnded()).isEqualTo(DEFAULT_DATE_ENDED);
        assertThat(testRoutine.getGoalDate()).isEqualTo(DEFAULT_GOAL_DATE);
        assertThat(testRoutine.getStartingBodyWeight()).isEqualTo(DEFAULT_STARTING_BODY_WEIGHT);
        assertThat(testRoutine.getEndingBodyWeight()).isEqualTo(DEFAULT_ENDING_BODY_WEIGHT);
    }

    @Test
    void createRoutineWithExistingId() throws Exception {
        // Create the Routine with an existing ID
        routine.setId("existing_id");

        int databaseSizeBeforeCreate = routineRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRoutineMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(routine)))
            .andExpect(status().isBadRequest());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllRoutines() throws Exception {
        // Initialize the database
        routineRepository.save(routine);

        // Get all the routineList
        restRoutineMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(routine.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].dateStarted").value(hasItem(DEFAULT_DATE_STARTED.toString())))
            .andExpect(jsonPath("$.[*].dateEnded").value(hasItem(DEFAULT_DATE_ENDED.toString())))
            .andExpect(jsonPath("$.[*].goalDate").value(hasItem(DEFAULT_GOAL_DATE.toString())))
            .andExpect(jsonPath("$.[*].startingBodyWeight").value(hasItem(DEFAULT_STARTING_BODY_WEIGHT)))
            .andExpect(jsonPath("$.[*].endingBodyWeight").value(hasItem(DEFAULT_ENDING_BODY_WEIGHT)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRoutinesWithEagerRelationshipsIsEnabled() throws Exception {
        when(routineRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRoutineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(routineRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRoutinesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(routineRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRoutineMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(routineRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getRoutine() throws Exception {
        // Initialize the database
        routineRepository.save(routine);

        // Get the routine
        restRoutineMockMvc
            .perform(get(ENTITY_API_URL_ID, routine.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(routine.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.dateStarted").value(DEFAULT_DATE_STARTED.toString()))
            .andExpect(jsonPath("$.dateEnded").value(DEFAULT_DATE_ENDED.toString()))
            .andExpect(jsonPath("$.goalDate").value(DEFAULT_GOAL_DATE.toString()))
            .andExpect(jsonPath("$.startingBodyWeight").value(DEFAULT_STARTING_BODY_WEIGHT))
            .andExpect(jsonPath("$.endingBodyWeight").value(DEFAULT_ENDING_BODY_WEIGHT));
    }

    @Test
    void getNonExistingRoutine() throws Exception {
        // Get the routine
        restRoutineMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putNewRoutine() throws Exception {
        // Initialize the database
        routineRepository.save(routine);

        int databaseSizeBeforeUpdate = routineRepository.findAll().size();

        // Update the routine
        Routine updatedRoutine = routineRepository.findById(routine.getId()).get();
        updatedRoutine
            .name(UPDATED_NAME)
            .dateStarted(UPDATED_DATE_STARTED)
            .dateEnded(UPDATED_DATE_ENDED)
            .goalDate(UPDATED_GOAL_DATE)
            .startingBodyWeight(UPDATED_STARTING_BODY_WEIGHT)
            .endingBodyWeight(UPDATED_ENDING_BODY_WEIGHT);

        restRoutineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedRoutine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedRoutine))
            )
            .andExpect(status().isOk());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
        Routine testRoutine = routineList.get(routineList.size() - 1);
        assertThat(testRoutine.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRoutine.getDateStarted()).isEqualTo(UPDATED_DATE_STARTED);
        assertThat(testRoutine.getDateEnded()).isEqualTo(UPDATED_DATE_ENDED);
        assertThat(testRoutine.getGoalDate()).isEqualTo(UPDATED_GOAL_DATE);
        assertThat(testRoutine.getStartingBodyWeight()).isEqualTo(UPDATED_STARTING_BODY_WEIGHT);
        assertThat(testRoutine.getEndingBodyWeight()).isEqualTo(UPDATED_ENDING_BODY_WEIGHT);
    }

    @Test
    void putNonExistingRoutine() throws Exception {
        int databaseSizeBeforeUpdate = routineRepository.findAll().size();
        routine.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoutineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, routine.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(routine))
            )
            .andExpect(status().isBadRequest());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchRoutine() throws Exception {
        int databaseSizeBeforeUpdate = routineRepository.findAll().size();
        routine.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoutineMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(routine))
            )
            .andExpect(status().isBadRequest());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamRoutine() throws Exception {
        int databaseSizeBeforeUpdate = routineRepository.findAll().size();
        routine.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoutineMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(routine)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateRoutineWithPatch() throws Exception {
        // Initialize the database
        routineRepository.save(routine);

        int databaseSizeBeforeUpdate = routineRepository.findAll().size();

        // Update the routine using partial update
        Routine partialUpdatedRoutine = new Routine();
        partialUpdatedRoutine.setId(routine.getId());

        partialUpdatedRoutine
            .dateStarted(UPDATED_DATE_STARTED)
            .dateEnded(UPDATED_DATE_ENDED)
            .goalDate(UPDATED_GOAL_DATE)
            .startingBodyWeight(UPDATED_STARTING_BODY_WEIGHT);

        restRoutineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRoutine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRoutine))
            )
            .andExpect(status().isOk());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
        Routine testRoutine = routineList.get(routineList.size() - 1);
        assertThat(testRoutine.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRoutine.getDateStarted()).isEqualTo(UPDATED_DATE_STARTED);
        assertThat(testRoutine.getDateEnded()).isEqualTo(UPDATED_DATE_ENDED);
        assertThat(testRoutine.getGoalDate()).isEqualTo(UPDATED_GOAL_DATE);
        assertThat(testRoutine.getStartingBodyWeight()).isEqualTo(UPDATED_STARTING_BODY_WEIGHT);
        assertThat(testRoutine.getEndingBodyWeight()).isEqualTo(DEFAULT_ENDING_BODY_WEIGHT);
    }

    @Test
    void fullUpdateRoutineWithPatch() throws Exception {
        // Initialize the database
        routineRepository.save(routine);

        int databaseSizeBeforeUpdate = routineRepository.findAll().size();

        // Update the routine using partial update
        Routine partialUpdatedRoutine = new Routine();
        partialUpdatedRoutine.setId(routine.getId());

        partialUpdatedRoutine
            .name(UPDATED_NAME)
            .dateStarted(UPDATED_DATE_STARTED)
            .dateEnded(UPDATED_DATE_ENDED)
            .goalDate(UPDATED_GOAL_DATE)
            .startingBodyWeight(UPDATED_STARTING_BODY_WEIGHT)
            .endingBodyWeight(UPDATED_ENDING_BODY_WEIGHT);

        restRoutineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRoutine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRoutine))
            )
            .andExpect(status().isOk());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
        Routine testRoutine = routineList.get(routineList.size() - 1);
        assertThat(testRoutine.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRoutine.getDateStarted()).isEqualTo(UPDATED_DATE_STARTED);
        assertThat(testRoutine.getDateEnded()).isEqualTo(UPDATED_DATE_ENDED);
        assertThat(testRoutine.getGoalDate()).isEqualTo(UPDATED_GOAL_DATE);
        assertThat(testRoutine.getStartingBodyWeight()).isEqualTo(UPDATED_STARTING_BODY_WEIGHT);
        assertThat(testRoutine.getEndingBodyWeight()).isEqualTo(UPDATED_ENDING_BODY_WEIGHT);
    }

    @Test
    void patchNonExistingRoutine() throws Exception {
        int databaseSizeBeforeUpdate = routineRepository.findAll().size();
        routine.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRoutineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, routine.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(routine))
            )
            .andExpect(status().isBadRequest());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchRoutine() throws Exception {
        int databaseSizeBeforeUpdate = routineRepository.findAll().size();
        routine.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoutineMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(routine))
            )
            .andExpect(status().isBadRequest());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamRoutine() throws Exception {
        int databaseSizeBeforeUpdate = routineRepository.findAll().size();
        routine.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRoutineMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(routine)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Routine in the database
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteRoutine() throws Exception {
        // Initialize the database
        routineRepository.save(routine);

        int databaseSizeBeforeDelete = routineRepository.findAll().size();

        // Delete the routine
        restRoutineMockMvc
            .perform(delete(ENTITY_API_URL_ID, routine.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Routine> routineList = routineRepository.findAll();
        assertThat(routineList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
