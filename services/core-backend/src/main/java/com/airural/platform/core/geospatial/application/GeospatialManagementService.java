/*
 * Purpose: Implements administrative hierarchy, spatial search, infrastructure, household, and boundary use cases.
 * Why it exists: Controllers need a transactional boundary that enforces hierarchy integrity, GPS validation, linkage validation, and audit logging.
 * Architecture fit: Application service for Milestone 5 Enterprise Geospatial Intelligence.
 */
package com.airural.platform.core.geospatial.application;

import static com.airural.platform.core.geospatial.infrastructure.GeospatialSpecifications.*;

import com.airural.platform.core.evidence.infrastructure.EvidenceRepository;
import com.airural.platform.core.geospatial.domain.*;
import com.airural.platform.core.geospatial.infrastructure.*;
import com.airural.platform.core.geospatial.web.dto.GeospatialDtos.*;
import com.airural.platform.core.identity.application.AuditService;
import com.airural.platform.core.identity.domain.AuditOutcome;
import com.airural.platform.core.survey.infrastructure.SurveyRepository;
import java.math.*;
import java.util.*;
import java.util.function.Function;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Transactional application service for geospatial management. */
@Service
public class GeospatialManagementService {
    private final CountryRepository countries;
    private final StateRepository states;
    private final DistrictRepository districts;
    private final MandalRepository mandals;
    private final BlockRepository blocks;
    private final GramPanchayatRepository gramPanchayats;
    private final VillageRepository villages;
    private final WardRepository wards;
    private final HamletRepository hamlets;
    private final HouseholdRepository households;
    private final GeoBoundaryRepository boundaries;
    private final InfrastructureAssetRepository assets;
    private final AdministrativeHierarchyRepository hierarchy;
    private final SurveyRepository surveyRepository;
    private final EvidenceRepository evidenceRepository;
    private final GeospatialValidationService validation;
    private final SpatialCalculationService spatial;
    private final GeospatialMapper mapper;
    private final AuditService auditService;

    public GeospatialManagementService(
            CountryRepository countries,
            StateRepository states,
            DistrictRepository districts,
            MandalRepository mandals,
            BlockRepository blocks,
            GramPanchayatRepository gramPanchayats,
            VillageRepository villages,
            WardRepository wards,
            HamletRepository hamlets,
            HouseholdRepository households,
            GeoBoundaryRepository boundaries,
            InfrastructureAssetRepository assets,
            AdministrativeHierarchyRepository hierarchy,
            SurveyRepository surveyRepository,
            EvidenceRepository evidenceRepository,
            GeospatialValidationService validation,
            SpatialCalculationService spatial,
            GeospatialMapper mapper,
            AuditService auditService) {
        this.countries = countries;
        this.states = states;
        this.districts = districts;
        this.mandals = mandals;
        this.blocks = blocks;
        this.gramPanchayats = gramPanchayats;
        this.villages = villages;
        this.wards = wards;
        this.hamlets = hamlets;
        this.households = households;
        this.boundaries = boundaries;
        this.assets = assets;
        this.hierarchy = hierarchy;
        this.surveyRepository = surveyRepository;
        this.evidenceRepository = evidenceRepository;
        this.validation = validation;
        this.spatial = spatial;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional
    public AdminUnitResponse createCountry(CreateCountryRequest request, UUID actorUserId) {
        validation.optionalPoint(request.latitude(), request.longitude());
        if (countries.existsByCode(request.code())) {
            throw conflict("COUNTRY_CODE_EXISTS", "Country code already exists");
        }
        CountryEntity entity = countries.save(new CountryEntity(request.code(), request.name(), request.isoCode(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_COUNTRY_CREATED", entity.id());
        return mapper.country(entity);
    }

    @Transactional
    public AdminUnitResponse createState(CreateAdminUnitRequest request, UUID actorUserId) {
        CountryEntity parent = countries.findByIdAndIsActiveTrue(request.parentId()).orElseThrow(() -> notFound("COUNTRY_NOT_FOUND", "Country was not found"));
        validation.optionalPoint(request.latitude(), request.longitude());
        ensure(!states.existsByCountryIdAndCode(parent.id(), request.code()), "STATE_CODE_EXISTS", "State code already exists under country");
        StateEntity entity = states.save(new StateEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_STATE_CREATED", entity.id());
        return mapper.state(entity);
    }

    @Transactional
    public AdminUnitResponse createDistrict(CreateAdminUnitRequest request, UUID actorUserId) {
        StateEntity parent = states.findByIdAndIsActiveTrue(request.parentId()).orElseThrow(() -> notFound("STATE_NOT_FOUND", "State was not found"));
        validation.optionalPoint(request.latitude(), request.longitude());
        ensure(!districts.existsByStateIdAndCode(parent.id(), request.code()), "DISTRICT_CODE_EXISTS", "District code already exists under state");
        DistrictEntity entity = districts.save(new DistrictEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_DISTRICT_CREATED", entity.id());
        return mapper.district(entity);
    }

    @Transactional
    public AdminUnitResponse createMandal(CreateAdminUnitRequest request, UUID actorUserId) {
        DistrictEntity parent = districts.findByIdAndIsActiveTrue(request.parentId()).orElseThrow(() -> notFound("DISTRICT_NOT_FOUND", "District was not found"));
        validation.optionalPoint(request.latitude(), request.longitude());
        ensure(!mandals.existsByDistrictIdAndCode(parent.id(), request.code()), "MANDAL_CODE_EXISTS", "Mandal code already exists under district");
        MandalEntity entity = mandals.save(new MandalEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_MANDAL_CREATED", entity.id());
        return mapper.mandal(entity);
    }

    @Transactional
    public AdminUnitResponse createBlock(CreateAdminUnitRequest request, UUID actorUserId) {
        MandalEntity parent = mandals.findByIdAndIsActiveTrue(request.parentId()).orElseThrow(() -> notFound("MANDAL_NOT_FOUND", "Mandal was not found"));
        validation.optionalPoint(request.latitude(), request.longitude());
        ensure(!blocks.existsByMandalIdAndCode(parent.id(), request.code()), "BLOCK_CODE_EXISTS", "Block code already exists under mandal");
        BlockEntity entity = blocks.save(new BlockEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_BLOCK_CREATED", entity.id());
        return mapper.block(entity);
    }

    @Transactional
    public AdminUnitResponse createGramPanchayat(CreateAdminUnitRequest request, UUID actorUserId) {
        BlockEntity parent = blocks.findByIdAndIsActiveTrue(request.parentId()).orElseThrow(() -> notFound("BLOCK_NOT_FOUND", "Block was not found"));
        validation.optionalPoint(request.latitude(), request.longitude());
        ensure(!gramPanchayats.existsByBlockIdAndCode(parent.id(), request.code()), "GRAM_PANCHAYAT_CODE_EXISTS", "Gram panchayat code already exists under block");
        GramPanchayatEntity entity = gramPanchayats.save(new GramPanchayatEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_GRAM_PANCHAYAT_CREATED", entity.id());
        return mapper.gramPanchayat(entity);
    }

    @Transactional
    public VillageResponse createVillage(CreateVillageRequest request, UUID actorUserId) {
        GramPanchayatEntity parent = gramPanchayats.findByIdAndIsActiveTrue(request.gramPanchayatId()).orElseThrow(() -> notFound("GRAM_PANCHAYAT_NOT_FOUND", "Gram panchayat was not found"));
        validation.requiredPoint(request.latitude(), request.longitude());
        if (request.geojson() != null && !request.geojson().isBlank()) {
            validation.geojson(request.geojson());
        }
        if (request.minLatitude() != null || request.minLongitude() != null || request.maxLatitude() != null || request.maxLongitude() != null) {
            validation.boundingBox(request.minLatitude(), request.minLongitude(), request.maxLatitude(), request.maxLongitude());
        }
        ensure(!villages.existsByGramPanchayatIdAndCode(parent.id(), request.code()), "VILLAGE_CODE_EXISTS", "Village code already exists under gram panchayat");
        VillageEntity entity = villages.save(new VillageEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude(), request.elevationMeters(), request.areaSqKm(), request.population(), request.householdCount(), request.geojson(), request.minLatitude(), request.minLongitude(), request.maxLatitude(), request.maxLongitude()));
        audit(actorUserId, "GEO_VILLAGE_CREATED", entity.id());
        return mapper.village(entity);
    }

    @Transactional
    public AdminUnitResponse createWard(CreateAdminUnitRequest request, UUID actorUserId) {
        VillageEntity parent = villages.findByIdAndIsActiveTrue(request.parentId()).orElseThrow(() -> notFound("VILLAGE_NOT_FOUND", "Village was not found"));
        validation.optionalPoint(request.latitude(), request.longitude());
        ensure(!wards.existsByVillageIdAndCode(parent.id(), request.code()), "WARD_CODE_EXISTS", "Ward code already exists under village");
        WardEntity entity = wards.save(new WardEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_WARD_CREATED", entity.id());
        return mapper.ward(entity);
    }

    @Transactional
    public AdminUnitResponse createHamlet(CreateAdminUnitRequest request, UUID actorUserId) {
        WardEntity parent = wards.findByIdAndIsActiveTrue(request.parentId()).orElseThrow(() -> notFound("WARD_NOT_FOUND", "Ward was not found"));
        validation.optionalPoint(request.latitude(), request.longitude());
        ensure(!hamlets.existsByWardIdAndCode(parent.id(), request.code()), "HAMLET_CODE_EXISTS", "Hamlet code already exists under ward");
        HamletEntity entity = hamlets.save(new HamletEntity(parent.id(), request.code(), request.name(), request.latitude(), request.longitude()));
        audit(actorUserId, "GEO_HAMLET_CREATED", entity.id());
        return mapper.hamlet(entity);
    }

    @Transactional
    public HouseholdResponse createHousehold(CreateHouseholdRequest request, UUID actorUserId) {
        HamletEntity hamlet = hamlets.findByIdAndIsActiveTrue(request.hamletId()).orElseThrow(() -> notFound("HAMLET_NOT_FOUND", "Hamlet was not found"));
        validation.requiredPoint(request.latitude(), request.longitude());
        ensure(!households.existsByHamletIdAndHouseholdCode(hamlet.id(), request.householdCode()), "HOUSEHOLD_CODE_EXISTS", "Household code already exists under hamlet");
        if (request.surveyId() != null && !surveyRepository.existsById(request.surveyId())) {
            throw notFound("SURVEY_NOT_FOUND", "Survey was not found");
        }
        if (request.evidenceId() != null && !evidenceRepository.existsById(request.evidenceId())) {
            throw notFound("EVIDENCE_NOT_FOUND", "Evidence was not found");
        }
        HouseholdEntity entity = households.save(new HouseholdEntity(hamlet.id(), request.householdCode(), request.headOfHousehold(), request.address(), request.latitude(), request.longitude(), request.surveyId(), request.evidenceId(), request.iotMetadataJson()));
        createHierarchyProjection(entity, hamlet);
        audit(actorUserId, "GEO_HOUSEHOLD_CREATED", entity.id());
        return mapper.household(entity);
    }

    @Transactional
    public InfrastructureAssetResponse createInfrastructureAsset(CreateInfrastructureAssetRequest request, UUID actorUserId) {
        if (request.villageId() != null) {
            villages.findByIdAndIsActiveTrue(request.villageId()).orElseThrow(() -> notFound("VILLAGE_NOT_FOUND", "Village was not found"));
        }
        validation.requiredPoint(request.latitude(), request.longitude());
        ensure(!assets.existsByAssetTypeAndCode(request.assetType(), request.code()), "INFRASTRUCTURE_ASSET_CODE_EXISTS", "Infrastructure asset code already exists for this type");
        InfrastructureAssetEntity entity = assets.save(new InfrastructureAssetEntity(request.villageId(), request.assetType(), request.code(), request.name(), request.description(), request.latitude(), request.longitude(), request.metadataJson()));
        audit(actorUserId, "GEO_INFRASTRUCTURE_ASSET_CREATED", entity.id());
        return mapper.asset(entity);
    }

    @Transactional
    public GeoBoundaryResponse createBoundary(CreateGeoBoundaryRequest request, UUID actorUserId) {
        validateEntityReference(request.entityType(), request.entityId());
        validation.geojson(request.geojson());
        validation.boundingBox(request.minLatitude(), request.minLongitude(), request.maxLatitude(), request.maxLongitude());
        ensure(!boundaries.existsByEntityTypeAndEntityIdAndIsActiveTrue(request.entityType(), request.entityId()), "BOUNDARY_EXISTS", "Boundary already exists for this administrative entity");
        GeoBoundaryEntity entity = boundaries.save(new GeoBoundaryEntity(request.entityType(), request.entityId(), request.geojson(), request.minLatitude(), request.minLongitude(), request.maxLatitude(), request.maxLongitude(), request.areaSqKm()));
        audit(actorUserId, "GEO_BOUNDARY_CREATED", entity.id());
        return mapper.boundary(entity);
    }

    @Transactional(readOnly = true)
    public Page<VillageResponse> searchVillages(String name, UUID gramPanchayatId, Pageable pageable) {
        Specification<VillageEntity> spec = Specification.where(activeVillages()).and(villageNameContains(name)).and(villageGramPanchayatEquals(gramPanchayatId));
        return villages.findAll(spec, pageable).map(mapper::village);
    }

    @Transactional(readOnly = true)
    public Page<VillageResponse> villagesInRadius(BigDecimal latitude, BigDecimal longitude, double radiusKm, Pageable pageable) {
        validation.requiredPoint(latitude, longitude);
        SpatialCalculationService.BoundingBox box = spatial.boundingBox(latitude, longitude, radiusKm);
        GeoPoint origin = new GeoPoint(latitude, longitude);
        return villages.findAll(Specification.where(activeVillages()).and(villageWithinBox(box.minLatitude(), box.minLongitude(), box.maxLatitude(), box.maxLongitude())), pageable)
                .map(v -> mapper.village(v, spatial.distanceKm(origin, new GeoPoint(v.latitude(), v.longitude()))));
    }

    @Transactional(readOnly = true)
    public Page<VillageResponse> villagesInBoundingBox(BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude, Pageable pageable) {
        validation.boundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
        return villages.findAll(Specification.where(activeVillages()).and(villageWithinBox(minLatitude, minLongitude, maxLatitude, maxLongitude)), pageable).map(mapper::village);
    }

    @Transactional(readOnly = true)
    public Page<InfrastructureAssetResponse> searchAssets(UUID villageId, InfrastructureAssetType assetType, String name, Pageable pageable) {
        Specification<InfrastructureAssetEntity> spec = Specification.where(activeAssets()).and(assetVillageEquals(villageId)).and(assetTypeEquals(assetType)).and(assetNameContains(name));
        return assets.findAll(spec, pageable).map(mapper::asset);
    }

    @Transactional(readOnly = true)
    public List<InfrastructureAssetResponse> nearestAssets(BigDecimal latitude, BigDecimal longitude, InfrastructureAssetType assetType, double radiusKm, int limit) {
        validation.requiredPoint(latitude, longitude);
        SpatialCalculationService.BoundingBox box = spatial.boundingBox(latitude, longitude, radiusKm);
        GeoPoint origin = new GeoPoint(latitude, longitude);
        return assets.findAll(Specification.where(activeAssets()).and(assetTypeEquals(assetType)).and(assetWithinBox(box.minLatitude(), box.minLongitude(), box.maxLatitude(), box.maxLongitude())))
                .stream()
                .map(asset -> mapper.asset(asset, spatial.distanceKm(origin, new GeoPoint(asset.latitude(), asset.longitude()))))
                .sorted(Comparator.comparing(InfrastructureAssetResponse::distanceKm))
                .limit(Math.max(1, Math.min(limit, 100)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<HouseholdResponse> searchHouseholds(UUID hamletId, BigDecimal minLatitude, BigDecimal minLongitude, BigDecimal maxLatitude, BigDecimal maxLongitude, Pageable pageable) {
        Specification<HouseholdEntity> spec = Specification.where(activeHouseholds()).and(householdHamletEquals(hamletId));
        if (minLatitude != null || minLongitude != null || maxLatitude != null || maxLongitude != null) {
            validation.boundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude);
            spec = spec.and(householdWithinBox(minLatitude, minLongitude, maxLatitude, maxLongitude));
        }
        return households.findAll(spec, pageable).map(mapper::household);
    }

    @Transactional(readOnly = true)
    public HouseholdResponse getHousehold(UUID householdId) {
        return mapper.household(households.findByIdAndIsActiveTrue(householdId).orElseThrow(() -> notFound("HOUSEHOLD_NOT_FOUND", "Household was not found")));
    }

    @Transactional(readOnly = true)
    public Optional<HierarchyPathResponse> householdHierarchy(UUID householdId) {
        return hierarchy.findByHouseholdId(householdId).map(mapper::hierarchy);
    }

    @Transactional(readOnly = true)
    public Optional<GeoBoundaryResponse> boundary(AdministrativeLevel entityType, UUID entityId) {
        return boundaries.findByEntityTypeAndEntityIdAndIsActiveTrue(entityType, entityId).map(mapper::boundary);
    }

    @Transactional(readOnly = true)
    public DistanceResponse distance(BigDecimal fromLatitude, BigDecimal fromLongitude, BigDecimal toLatitude, BigDecimal toLongitude) {
        validation.requiredPoint(fromLatitude, fromLongitude);
        validation.requiredPoint(toLatitude, toLongitude);
        return new DistanceResponse(fromLatitude, fromLongitude, toLatitude, toLongitude, spatial.distanceKm(new GeoPoint(fromLatitude, fromLongitude), new GeoPoint(toLatitude, toLongitude)));
    }

    @Transactional(readOnly = true)
    public List<VillageClusterResponse> villageClusters(double gridSizeDegrees) {
        Map<String, List<VillageEntity>> grouped = new LinkedHashMap<>();
        villages.findByIsActiveTrueOrderByNameAsc().forEach(v -> grouped.computeIfAbsent(spatial.gridCell(v.latitude(), v.longitude(), gridSizeDegrees), ignored -> new ArrayList<>()).add(v));
        return grouped.entrySet().stream()
                .map(entry -> {
                    BigDecimal lat = average(entry.getValue(), VillageEntity::latitude);
                    BigDecimal lon = average(entry.getValue(), VillageEntity::longitude);
                    return new VillageClusterResponse(entry.getKey(), entry.getValue().size(), lat, lon);
                })
                .toList();
    }

    private void createHierarchyProjection(HouseholdEntity household, HamletEntity hamlet) {
        WardEntity ward = wards.findByIdAndIsActiveTrue(hamlet.wardId()).orElseThrow(() -> notFound("WARD_NOT_FOUND", "Ward was not found"));
        VillageEntity village = villages.findByIdAndIsActiveTrue(ward.villageId()).orElseThrow(() -> notFound("VILLAGE_NOT_FOUND", "Village was not found"));
        GramPanchayatEntity gp = gramPanchayats.findByIdAndIsActiveTrue(village.gramPanchayatId()).orElseThrow(() -> notFound("GRAM_PANCHAYAT_NOT_FOUND", "Gram panchayat was not found"));
        BlockEntity block = blocks.findByIdAndIsActiveTrue(gp.blockId()).orElseThrow(() -> notFound("BLOCK_NOT_FOUND", "Block was not found"));
        MandalEntity mandal = mandals.findByIdAndIsActiveTrue(block.mandalId()).orElseThrow(() -> notFound("MANDAL_NOT_FOUND", "Mandal was not found"));
        DistrictEntity district = districts.findByIdAndIsActiveTrue(mandal.districtId()).orElseThrow(() -> notFound("DISTRICT_NOT_FOUND", "District was not found"));
        StateEntity state = states.findByIdAndIsActiveTrue(district.stateId()).orElseThrow(() -> notFound("STATE_NOT_FOUND", "State was not found"));
        CountryEntity country = countries.findByIdAndIsActiveTrue(state.countryId()).orElseThrow(() -> notFound("COUNTRY_NOT_FOUND", "Country was not found"));
        String pathCode = String.join("/", country.code(), state.code(), district.code(), mandal.code(), block.code(), gp.code(), village.code(), ward.code(), hamlet.code(), household.householdCode());
        String pathName = String.join(" / ", country.name(), state.name(), district.name(), mandal.name(), block.name(), gp.name(), village.name(), ward.name(), hamlet.name(), household.address());
        hierarchy.save(new AdministrativeHierarchyEntity(country.id(), state.id(), district.id(), mandal.id(), block.id(), gp.id(), village.id(), ward.id(), hamlet.id(), household.id(), pathCode, pathName));
    }

    private void validateEntityReference(AdministrativeLevel level, UUID id) {
        boolean exists = switch (level) {
            case COUNTRY -> countries.findByIdAndIsActiveTrue(id).isPresent();
            case STATE -> states.findByIdAndIsActiveTrue(id).isPresent();
            case DISTRICT -> districts.findByIdAndIsActiveTrue(id).isPresent();
            case MANDAL -> mandals.findByIdAndIsActiveTrue(id).isPresent();
            case BLOCK -> blocks.findByIdAndIsActiveTrue(id).isPresent();
            case GRAM_PANCHAYAT -> gramPanchayats.findByIdAndIsActiveTrue(id).isPresent();
            case VILLAGE -> villages.findByIdAndIsActiveTrue(id).isPresent();
            case WARD -> wards.findByIdAndIsActiveTrue(id).isPresent();
            case HAMLET -> hamlets.findByIdAndIsActiveTrue(id).isPresent();
            case HOUSEHOLD -> households.findByIdAndIsActiveTrue(id).isPresent();
        };
        if (!exists) {
            throw notFound("BOUNDARY_ENTITY_NOT_FOUND", "Boundary entity was not found");
        }
    }

    private BigDecimal average(List<VillageEntity> values, Function<VillageEntity, BigDecimal> reader) {
        BigDecimal sum = values.stream().map(reader).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 7, RoundingMode.HALF_UP);
    }

    private void audit(UUID actorUserId, String eventType, UUID entityId) {
        auditService.record(actorUserId, eventType, AuditOutcome.SUCCESS, null, null, "geospatialEntityId=" + entityId);
    }

    private void ensure(boolean condition, String code, String message) {
        if (!condition) {
            throw conflict(code, message);
        }
    }

    private GeospatialException conflict(String code, String message) {
        return new GeospatialException(code, message, HttpStatus.CONFLICT);
    }

    private GeospatialException notFound(String code, String message) {
        return new GeospatialException(code, message, HttpStatus.NOT_FOUND);
    }
}
