package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.season.SeasonRequest;
import com.virtual_paddock.backend.api.dtos.season.SeasonResponse;
import com.virtual_paddock.backend.api.dtos.season.SeasonUpdate;
import com.virtual_paddock.backend.api.dtos.team.TeamResponse;
import com.virtual_paddock.backend.domain.entities.Championship;
import com.virtual_paddock.backend.domain.entities.Season;
import com.virtual_paddock.backend.domain.entities.Team;
import com.virtual_paddock.backend.domain.repositories.ChampionshipRepository;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.RaceEventRepository;
import com.virtual_paddock.backend.domain.repositories.SeasonRepository;
import com.virtual_paddock.backend.domain.repositories.TeamRepository;
import com.virtual_paddock.backend.infrastructure.mapper.SeasonMapper;
import com.virtual_paddock.backend.infrastructure.mapper.TeamMapper;
import com.virtual_paddock.backend.utils.enums.SeasonStatus;
import com.virtual_paddock.backend.utils.exeption.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeasonServiceImplTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private ChampionshipRepository championshipRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private RaceEventRepository raceEventRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private SeasonMapper seasonMapper;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private SeasonServiceImpl seasonService;

    private UUID championshipId;
    private UUID seasonId;
    private Championship championship;
    private Season season;

    @BeforeEach
    void setUp() {
        championshipId = UUID.randomUUID();
        seasonId = UUID.randomUUID();

        championship = new Championship();
        championship.setId(championshipId);
        championship.setGameName("Assetto Corsa Competizione");
        championship.setCategory("GT3");
        championship.setMaxDriversPerTeam(2);

        season = new Season();
        season.setId(seasonId);
        season.setSeasonName("Temporada 1");
        season.setChampionship(championship);
        season.setIsDefault(false);
        season.setStatus(SeasonStatus.ACTIVE);
    }

    @Test
    void create_fallbackToChampionshipCategory_whenCategoryBlank() {
        SeasonRequest request = SeasonRequest.builder()
                .seasonName("Temporada 2026")
                .championshipId(championshipId)
                .categories("")
                .isDefault(false)
                .build();

        Season createdEntity = Season.builder()
                .seasonName("Temporada 2026")
                .categories("")
                .build();

        when(championshipRepository.findById(championshipId)).thenReturn(Optional.of(championship));
        when(seasonMapper.toEntity(request)).thenReturn(createdEntity);
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonResponse response = SeasonResponse.builder()
                .seasonName("Temporada 2026")
                .categories("GT3")
                .build();
        when(seasonMapper.toResponse(any(Season.class))).thenReturn(response);

        SeasonResponse result = seasonService.create(request);

        assertNotNull(result);
        assertEquals("GT3", createdEntity.getCategories());
        verify(seasonRepository).save(createdEntity);
    }

    @Test
    void create_whenIsDefaultTrue_clearsAllDefaults() {
        SeasonRequest request = SeasonRequest.builder()
                .seasonName("Temporada Default")
                .championshipId(championshipId)
                .categories("GT3")
                .isDefault(true)
                .build();

        Season createdEntity = Season.builder()
                .seasonName("Temporada Default")
                .categories("GT3")
                .isDefault(true)
                .build();

        when(championshipRepository.findById(championshipId)).thenReturn(Optional.of(championship));
        when(seasonMapper.toEntity(request)).thenReturn(createdEntity);
        when(seasonRepository.save(any(Season.class))).thenAnswer(inv -> inv.getArgument(0));
        when(seasonMapper.toResponse(any(Season.class))).thenReturn(SeasonResponse.builder().isDefault(true).build());

        SeasonResponse result = seasonService.create(request);

        assertNotNull(result);
        verify(seasonRepository).clearAllDefaults(championshipId);
        verify(seasonRepository).save(createdEntity);
    }

    @Test
    void setDefaultSeason_clearsOtherDefaultsAndSetsNewDefault() {
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(seasonRepository.save(season)).thenReturn(season);

        SeasonResponse expectedResponse = SeasonResponse.builder()
                .id(seasonId)
                .seasonName("Temporada 1")
                .isDefault(true)
                .build();
        when(seasonMapper.toResponse(season)).thenReturn(expectedResponse);

        SeasonResponse result = seasonService.setDefaultSeason(seasonId);

        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(seasonRepository).clearDefaultExcept(championshipId, seasonId);
        assertTrue(season.getIsDefault());
        verify(seasonRepository).save(season);
    }

    @Test
    void setDefaultSeason_seasonNotFound_throwsException() {
        UUID unknownId = UUID.randomUUID();
        when(seasonRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> seasonService.setDefaultSeason(unknownId));
    }

    @Test
    void getTeamsBySeasonId_returnsMappedTeamsWithCapacityLimits() {
        Team team1 = new Team();
        team1.setId(UUID.randomUUID());
        team1.setName("Ferrari AF Corse");
        team1.setMaxDrivers(2);
        team1.setChampionship(championship);

        Team team2 = new Team();
        team2.setId(UUID.randomUUID());
        team2.setName("Porsche Penske");
        team2.setMaxDrivers(2);
        team2.setChampionship(championship);

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(teamRepository.findBySeasonId(seasonId)).thenReturn(List.of(team1, team2));

        TeamResponse resp1 = TeamResponse.builder().id(team1.getId()).name("Ferrari AF Corse").build();
        TeamResponse resp2 = TeamResponse.builder().id(team2.getId()).name("Porsche Penske").build();
        when(teamMapper.toResponse(team1)).thenReturn(resp1);
        when(teamMapper.toResponse(team2)).thenReturn(resp2);

        when(driverRepository.countByTeamId(team1.getId())).thenReturn(2L); // 2 de 2 -> Lleno
        when(driverRepository.countByTeamId(team2.getId())).thenReturn(1L); // 1 de 2 -> Disponible

        List<TeamResponse> result = seasonService.getTeamsBySeasonId(seasonId);

        assertNotNull(result);
        assertEquals(2, result.size());

        TeamResponse ferrari = result.get(0);
        assertEquals(2, ferrari.getMaxDrivers());
        assertEquals(2, ferrari.getCurrentDriversCount());
        assertTrue(ferrari.getIsFull());

        TeamResponse porsche = result.get(1);
        assertEquals(2, porsche.getMaxDrivers());
        assertEquals(1, porsche.getCurrentDriversCount());
        assertFalse(porsche.getIsFull());
    }

    @Test
    void update_updatesCategoriesStatusAndDefault() {
        SeasonUpdate update = SeasonUpdate.builder()
                .seasonName("Temporada Modificada")
                .categories("HYPERCAR")
                .status(SeasonStatus.FINISHED)
                .isDefault(true)
                .build();

        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(seasonRepository.save(season)).thenReturn(season);

        SeasonResponse response = SeasonResponse.builder()
                .id(seasonId)
                .seasonName("Temporada Modificada")
                .categories("HYPERCAR")
                .status(SeasonStatus.FINISHED)
                .isDefault(true)
                .build();
        when(seasonMapper.toResponse(season)).thenReturn(response);

        SeasonResponse result = seasonService.update(seasonId, update);

        assertNotNull(result);
        assertEquals("HYPERCAR", season.getCategories());
        assertEquals(SeasonStatus.FINISHED, season.getStatus());
        assertTrue(season.getIsDefault());
        verify(seasonRepository).clearDefaultExcept(championshipId, seasonId);
        verify(seasonRepository).save(season);
    }
}
