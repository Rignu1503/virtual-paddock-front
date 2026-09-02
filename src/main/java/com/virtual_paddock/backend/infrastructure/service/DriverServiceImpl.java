package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.driver.*;
import com.virtual_paddock.backend.domain.entities.Driver;
import com.virtual_paddock.backend.domain.entities.Team;
import com.virtual_paddock.backend.domain.repositories.DriverRepository;
import com.virtual_paddock.backend.domain.repositories.TeamRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IDriverService;
import com.virtual_paddock.backend.infrastructure.mapper.DriverMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements IDriverService {

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final DriverMapper driverMapper;

    @Override
    public DriverResponse create(DriverRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new EntityNotFoundException("Equipo no encontrado con ID: " + request.getTeamId()));
        Driver driver = driverMapper.toEntity(request);
        driver.setTeam(team);
        Driver saved = driverRepository.save(driver);
        return driverMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Piloto no encontrado con ID: " + id));
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DriverBasicResponse> getAll(int page, int size) {
        Page<Driver> driverPage = driverRepository.findAll(PageRequest.of(page, size));
        return buildPageResponse(driverPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DriverBasicResponse> getByTeamId(Long teamId, int page, int size) {
        Page<Driver> driverPage = driverRepository.findByTeamId(teamId, PageRequest.of(page, size));
        return buildPageResponse(driverPage);
    }

    @Override
    public DriverResponse update(Long id, DriverUpdate update) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Piloto no encontrado con ID: " + id));

        if (update.getTeamId() != null) {
            Team team = teamRepository.findById(update.getTeamId())
                    .orElseThrow(() -> new EntityNotFoundException("Equipo no encontrado con ID: " + update.getTeamId()));
            driver.setTeam(team);
        }

        driverMapper.updateEntityFromDto(update, driver);
        Driver updated = driverRepository.save(driver);
        return driverMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new EntityNotFoundException("Piloto no encontrado con ID: " + id);
        }
        driverRepository.deleteById(id);
    }

    private PageResponse<DriverBasicResponse> buildPageResponse(Page<Driver> page) {
        return PageResponse.<DriverBasicResponse>builder()
                .content(page.getContent().stream().map(driverMapper::toBasicResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
