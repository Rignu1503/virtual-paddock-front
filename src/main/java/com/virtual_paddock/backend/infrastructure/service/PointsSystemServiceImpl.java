package com.virtual_paddock.backend.infrastructure.service;

import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.pointssystem.*;
import com.virtual_paddock.backend.domain.entities.League;
import com.virtual_paddock.backend.domain.entities.PointRule;
import com.virtual_paddock.backend.domain.entities.PointsSystem;
import com.virtual_paddock.backend.domain.repositories.LeagueRepository;
import com.virtual_paddock.backend.domain.repositories.PointsSystemRepository;
import com.virtual_paddock.backend.infrastructure.abstract_service.IPointsSystemService;
import com.virtual_paddock.backend.infrastructure.mapper.PointsSystemMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PointsSystemServiceImpl implements IPointsSystemService {

    private final PointsSystemRepository pointsSystemRepository;
    private final LeagueRepository leagueRepository;
    private final PointsSystemMapper pointsSystemMapper;

    @Override
    public PointsSystemResponse create(PointsSystemRequest request) {
        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new EntityNotFoundException("Liga no encontrada con ID: " + request.getLeagueId()));

        PointsSystem pointsSystem = pointsSystemMapper.toEntity(request);
        pointsSystem.setLeague(league);

        if (request.getRules() != null && !request.getRules().isEmpty()) {
            List<PointRule> rules = new ArrayList<>();
            for (PointRuleDto ruleDto : request.getRules()) {
                PointRule rule = pointsSystemMapper.toRuleEntity(ruleDto);
                rule.setPointsSystem(pointsSystem);
                rules.add(rule);
            }
            pointsSystem.setRules(rules);
        }

        PointsSystem saved = pointsSystemRepository.save(pointsSystem);
        return pointsSystemMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PointsSystemResponse getById(Long id) {
        PointsSystem pointsSystem = pointsSystemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sistema de puntos no encontrado con ID: " + id));
        return pointsSystemMapper.toResponse(pointsSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointsSystemResponse> getByLeagueId(Long leagueId) {
        List<PointsSystem> list = pointsSystemRepository.findByLeagueId(leagueId);
        return list.stream().map(pointsSystemMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PointsSystemBasicResponse> getByLeagueIdPaged(Long leagueId, int page, int size) {
        Page<PointsSystem> paged = pointsSystemRepository.findByLeagueId(leagueId, PageRequest.of(page, size));
        return PageResponse.<PointsSystemBasicResponse>builder()
                .content(paged.getContent().stream().map(pointsSystemMapper::toBasicResponse).toList())
                .pageNumber(paged.getNumber())
                .pageSize(paged.getSize())
                .totalElements(paged.getTotalElements())
                .totalPages(paged.getTotalPages())
                .last(paged.isLast())
                .build();
    }

    @Override
    public PointsSystemResponse update(Long id, PointsSystemUpdate update) {
        PointsSystem pointsSystem = pointsSystemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sistema de puntos no encontrado con ID: " + id));

        pointsSystemMapper.updateEntityFromDto(update, pointsSystem);

        if (update.getRules() != null) {
            pointsSystem.getRules().clear();
            for (PointRuleDto ruleDto : update.getRules()) {
                PointRule rule = pointsSystemMapper.toRuleEntity(ruleDto);
                rule.setPointsSystem(pointsSystem);
                pointsSystem.getRules().add(rule);
            }
        }

        PointsSystem updated = pointsSystemRepository.save(pointsSystem);
        return pointsSystemMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!pointsSystemRepository.existsById(id)) {
            throw new EntityNotFoundException("Sistema de puntos no encontrado con ID: " + id);
        }
        pointsSystemRepository.deleteById(id);
    }
}
