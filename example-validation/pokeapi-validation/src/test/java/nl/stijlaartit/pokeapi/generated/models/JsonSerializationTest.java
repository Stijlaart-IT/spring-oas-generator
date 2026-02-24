package nl.stijlaartit.pokeapi.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private <T> void assertSerializesSymmetrical(T original, Class<T> type) {
        Objects.requireNonNull(original, "Model instance must not be null");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, type);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void abilityChange() {
        var original = new AbilityChange(null, null);
        assertSerializesSymmetrical(original, AbilityChange.class);
    }

    @Test
    void abilityChangeEffectText() {
        var original = new AbilityChangeEffectText(null, null);
        assertSerializesSymmetrical(original, AbilityChangeEffectText.class);
    }

    @Test
    void abilityDetail() {
        var original = new AbilityDetail(null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, AbilityDetail.class);
    }

    @Test
    void abilityDetailPokemon() {
        var original = new AbilityDetailPokemonItem(null, null, null);
        assertSerializesSymmetrical(original, AbilityDetailPokemonItem.class);
    }

    @Test
    void abilityEffectText() {
        var original = new AbilityEffectText(null, null, null);
        assertSerializesSymmetrical(original, AbilityEffectText.class);
    }

    @Test
    void abilityFlavorText() {
        var original = new AbilityFlavorText(null, null, null);
        assertSerializesSymmetrical(original, AbilityFlavorText.class);
    }

    @Test
    void abilityName() {
        var original = new AbilityName(null, null);
        assertSerializesSymmetrical(original, AbilityName.class);
    }

    @Test
    void abilitySummary() {
        var original = new AbilitySummary(null, null);
        assertSerializesSymmetrical(original, AbilitySummary.class);
    }

    @Test
    void apiV2PokemonEncountersRetrieve200ResponseItem() {
        var original = new ApiV2PokemonEncountersRetrieve200ResponseItem(null, null);
        assertSerializesSymmetrical(original, ApiV2PokemonEncountersRetrieve200ResponseItem.class);
    }

    @Test
    void apiV2PokemonEncountersRetrieve200ResponseItemVersionDetails() {
        var original = new ApiV2PokemonEncountersRetrieve200ResponseItemVersionDetailsItem(null, null, null);
        assertSerializesSymmetrical(original, ApiV2PokemonEncountersRetrieve200ResponseItemVersionDetailsItem.class);
    }

    @Test
    void apiV2PokemonEncountersRetrieve200ResponseItemVersionDetailsEncounterDetails() {
        var original = new ApiV2PokemonEncountersRetrieve200ResponseItemVersionDetailsItemEncounterDetailsItem(null, null, null, null, null);
        assertSerializesSymmetrical(original, ApiV2PokemonEncountersRetrieve200ResponseItemVersionDetailsItemEncounterDetailsItem.class);
    }

    @Test
    void berryDetail() {
        var original = new BerryDetail(null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, BerryDetail.class);
    }

    @Test
    void berryDetailFlavors() {
        var original = new BerryDetailFlavorsItem(null, null);
        assertSerializesSymmetrical(original, BerryDetailFlavorsItem.class);
    }

    @Test
    void berryFirmnessDetail() {
        var original = new BerryFirmnessDetail(null, null, null, null);
        assertSerializesSymmetrical(original, BerryFirmnessDetail.class);
    }

    @Test
    void berryFlavorDetail() {
        var original = new BerryFlavorDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, BerryFlavorDetail.class);
    }

    @Test
    void berryFlavorDetailBerries() {
        var original = new BerryFlavorDetailBerriesItem(null, null);
        assertSerializesSymmetrical(original, BerryFlavorDetailBerriesItem.class);
    }


    @Test
    void characteristicDescription() {
        var original = new CharacteristicDescription(null, null);
        assertSerializesSymmetrical(original, CharacteristicDescription.class);
    }

    @Test
    void characteristicDetail() {
        var original = new CharacteristicDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, CharacteristicDetail.class);
    }

    @Test
    void characteristicSummary() {
        var original = new CharacteristicSummary(null);
        assertSerializesSymmetrical(original, CharacteristicSummary.class);
    }

    @Test
    void contestEffectDetail() {
        var original = new ContestEffectDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, ContestEffectDetail.class);
    }


    @Test
    void contestEffectFlavorText() {
        var original = new ContestEffectFlavorText(null, null);
        assertSerializesSymmetrical(original, ContestEffectFlavorText.class);
    }


    @Test
    void contestTypeDetail() {
        var original = new ContestTypeDetail(null, null, null, null);
        assertSerializesSymmetrical(original, ContestTypeDetail.class);
    }

    @Test
    void contestTypeName() {
        var original = new ContestTypeName(null, null, null);
        assertSerializesSymmetrical(original, ContestTypeName.class);
    }


    @Test
    void eggGroupDetail() {
        var original = new EggGroupDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EggGroupDetail.class);
    }

    @Test
    void eggGroupDetailPokemonSpecies() {
        var original = new EggGroupDetailPokemonSpeciesItem("name", "url", Map.of(), Map.of());
        assertSerializesSymmetrical(original, EggGroupDetailPokemonSpeciesItem.class);
    }


    @Test
    void encounterConditionDetail() {
        var original = new EncounterConditionDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EncounterConditionDetail.class);
    }


    @Test
    void encounterConditionValueDetail() {
        var original = new EncounterConditionValueDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EncounterConditionValueDetail.class);
    }


    @Test
    void encounterMethodDetail() {
        var original = new EncounterMethodDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EncounterMethodDetail.class);
    }


    @Test
    void evolutionChainDetail() {
        var original = new EvolutionChainDetail(null, null, null);
        assertSerializesSymmetrical(original, EvolutionChainDetail.class);
    }

    @Test
    void evolutionChainDetailChain() {
        var original = new EvolutionChainDetailChain(null, null, null, null);
        assertSerializesSymmetrical(original, EvolutionChainDetailChain.class);
    }

    @Test
    void evolutionChainDetailChainEvolvesTo() {
        var original = new EvolutionChainDetailChainEvolvesToItem(List.<EvolutionChainDetailChainEvolvesToItemEvolutionDetailsItem>of(), false, (AbilitySummary) null, Map.of());
        assertSerializesSymmetrical(original, EvolutionChainDetailChainEvolvesToItem.class);
    }

    @Test
    void evolutionChainDetailChainEvolvesToEvolutionDetails() {
        var original = new EvolutionChainDetailChainEvolvesToItemEvolutionDetailsItem(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, EvolutionChainDetailChainEvolvesToItemEvolutionDetailsItem.class);
    }


    @Test
    void evolutionTriggerDetail() {
        var original = new EvolutionTriggerDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EvolutionTriggerDetail.class);
    }


    @Test
    void experience() {
        var original = new Experience(null, null);
        assertSerializesSymmetrical(original, Experience.class);
    }

    @Test
    void genderDetail() {
        var original = new GenderDetail(null, null, null, null);
        assertSerializesSymmetrical(original, GenderDetail.class);
    }

    @Test
    void genderDetailPokemonSpeciesDetails() {
        var original = new GenderDetailPokemonSpeciesDetailsItem(null, null);
        assertSerializesSymmetrical(original, GenderDetailPokemonSpeciesDetailsItem.class);
    }


    @Test
    void generationDetail() {
        var original = new GenerationDetail(null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, GenerationDetail.class);
    }


    @Test
    void growthRateDetail() {
        var original = new GrowthRateDetail(null, null, null, null, null, null);
        assertSerializesSymmetrical(original, GrowthRateDetail.class);
    }


    @Test
    void itemAttributeDetail() {
        var original = new ItemAttributeDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, ItemAttributeDetail.class);
    }


    @Test
    void itemCategoryDetail() {
        var original = new ItemCategoryDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, ItemCategoryDetail.class);
    }


    @Test
    void itemDetail() {
        var original = new ItemDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, ItemDetail.class);
    }

    @Test
    void itemDetailHeldByPokemon() {
        var original = new ItemDetailHeldByPokemonItem(null, null);
        assertSerializesSymmetrical(original, ItemDetailHeldByPokemonItem.class);
    }

    @Test
    void itemDetailHeldByPokemonVersionDetails() {
        var original = new ItemDetailHeldByPokemonItemVersionDetailsItem(null, null);
        assertSerializesSymmetrical(original, ItemDetailHeldByPokemonItemVersionDetailsItem.class);
    }

    @Test
    void itemDetailMachines() {
        var original = new ItemDetailMachinesItem(null, null);
        assertSerializesSymmetrical(original, ItemDetailMachinesItem.class);
    }

    @Test
    void itemDetailSprites() {
        var original = new ItemDetailSprites(null);
        assertSerializesSymmetrical(original, ItemDetailSprites.class);
    }


    @Test
    void itemFlavorText() {
        var original = new ItemFlavorText(null, null, null);
        assertSerializesSymmetrical(original, ItemFlavorText.class);
    }

    @Test
    void itemFlingEffectDetail() {
        var original = new ItemFlingEffectDetail(null, null, null, null);
        assertSerializesSymmetrical(original, ItemFlingEffectDetail.class);
    }


    @Test
    void itemGameIndex() {
        var original = new ItemGameIndex(null, null);
        assertSerializesSymmetrical(original, ItemGameIndex.class);
    }


    @Test
    void itemPocketDetail() {
        var original = new ItemPocketDetail(null, null, null, null);
        assertSerializesSymmetrical(original, ItemPocketDetail.class);
    }


    @Test
    void languageDetail() {
        var original = new LanguageDetail(null, null, null, null, null, null);
        assertSerializesSymmetrical(original, LanguageDetail.class);
    }


    @Test
    void locationAreaDetail() {
        var original = new LocationAreaDetail(null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, LocationAreaDetail.class);
    }

    @Test
    void locationAreaDetailEncounterMethodRates() {
        var original = new LocationAreaDetailEncounterMethodRatesItem(null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailEncounterMethodRatesItem.class);
    }

    @Test
    void locationAreaDetailEncounterMethodRatesVersionDetails() {
        var original = new LocationAreaDetailEncounterMethodRatesItemVersionDetailsItem(null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailEncounterMethodRatesItemVersionDetailsItem.class);
    }

    @Test
    void locationAreaDetailPokemonEncounters() {
        var original = new LocationAreaDetailPokemonEncountersItem(null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailPokemonEncountersItem.class);
    }

    @Test
    void locationAreaDetailPokemonEncountersVersionDetails() {
        var original = new LocationAreaDetailPokemonEncountersItemVersionDetailsItem(null, null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailPokemonEncountersItemVersionDetailsItem.class);
    }

    @Test
    void locationAreaDetailPokemonEncountersVersionDetailsEncounterDetails() {
        var original = new LocationAreaDetailPokemonEncountersItemVersionDetailsItemEncounterDetails(1, 2, null, null, null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailPokemonEncountersItemVersionDetailsItemEncounterDetails.class);
    }


    @Test
    void locationDetail() {
        var original = new LocationDetail(null, null, null, null, null, null);
        assertSerializesSymmetrical(original, LocationDetail.class);
    }


    @Test
    void machineDetail() {
        var original = new MachineDetail(null, null, null, null);
        assertSerializesSymmetrical(original, MachineDetail.class);
    }


    @Test
    void moveBattleStyleDetail() {
        var original = new MoveBattleStyleDetail(null, null, null);
        assertSerializesSymmetrical(original, MoveBattleStyleDetail.class);
    }


    @Test
    void moveChange() {
        var original = new MoveChange(null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveChange.class);
    }

    @Test
    void moveChangeEffectEntries() {
        var original = new MoveChangeEffectEntriesItem(null, null, null);
        assertSerializesSymmetrical(original, MoveChangeEffectEntriesItem.class);
    }


    @Test
    void moveDamageClassDetail() {
        var original = new MoveDamageClassDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveDamageClassDetail.class);
    }


    @Test
    void moveDetail() {
        var original = new MoveDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveDetail.class);
    }

    @Test
    void moveDetailContestCombos() {
        var original = new MoveDetailContestCombos(null, null);
        assertSerializesSymmetrical(original, MoveDetailContestCombos.class);
    }

    @Test
    void moveDetailContestCombosNormal() {
        var original = new MoveDetailContestCombosNormal(null, null);
        assertSerializesSymmetrical(original, MoveDetailContestCombosNormal.class);
    }

    @Test
    void moveDetailEffectChanges() {
        var original = new MoveDetailEffectChangesItem(null, null);
        assertSerializesSymmetrical(original, MoveDetailEffectChangesItem.class);
    }

    @Test
    void moveDetailEffectChangesEffectEntries() {
        var original = new MoveDetailEffectChangesItemEffectEntriesItem(null, null);
        assertSerializesSymmetrical(original, MoveDetailEffectChangesItemEffectEntriesItem.class);
    }

    @Test
    void moveDetailMachines() {
        var original = new MoveDetailMachinesItem(null, null);
        assertSerializesSymmetrical(original, MoveDetailMachinesItem.class);
    }

    @Test
    void moveDetailStatChanges() {
        var original = new MoveDetailStatChangesItem(null, null);
        assertSerializesSymmetrical(original, MoveDetailStatChangesItem.class);
    }


    @Test
    void moveLearnMethodDetail() {
        var original = new MoveLearnMethodDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveLearnMethodDetail.class);
    }


    @Test
    void moveMeta() {
        var original = new MoveMeta(null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveMeta.class);
    }

    @Test
    void moveMetaAilmentDetail() {
        var original = new MoveMetaAilmentDetail(null, null, null, null);
        assertSerializesSymmetrical(original, MoveMetaAilmentDetail.class);
    }


    @Test
    void moveMetaCategoryDetail() {
        var original = new MoveMetaCategoryDetail(null, null, null, null);
        assertSerializesSymmetrical(original, MoveMetaCategoryDetail.class);
    }


    @Test
    void moveTargetDetail() {
        var original = new MoveTargetDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveTargetDetail.class);
    }


    @Test
    void natureBattleStylePreference() {
        var original = new NatureBattleStylePreference(null, null, null);
        assertSerializesSymmetrical(original, NatureBattleStylePreference.class);
    }

    @Test
    void natureDetail() {
        var original = new NatureDetail(null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, NatureDetail.class);
    }

    @Test
    void natureDetailPokeathlonStatChanges() {
        var original = new NatureDetailPokeathlonStatChangesItem(null, null);
        assertSerializesSymmetrical(original, NatureDetailPokeathlonStatChangesItem.class);
    }


    @Test
    void paginatedAbilitySummaryList() {
        var original = new PaginatedAbilitySummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedAbilitySummaryList.class);
    }

    @Test
    void paginatedBerryFirmnessSummaryList() {
        var original = new PaginatedBerryFirmnessSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedBerryFirmnessSummaryList.class);
    }

    @Test
    void paginatedBerryFlavorSummaryList() {
        var original = new PaginatedBerryFlavorSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedBerryFlavorSummaryList.class);
    }

    @Test
    void paginatedBerrySummaryList() {
        var original = new PaginatedBerrySummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedBerrySummaryList.class);
    }

    @Test
    void paginatedCharacteristicSummaryList() {
        var original = new PaginatedCharacteristicSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedCharacteristicSummaryList.class);
    }

    @Test
    void paginatedContestEffectSummaryList() {
        var original = new PaginatedContestEffectSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedContestEffectSummaryList.class);
    }

    @Test
    void paginatedContestTypeSummaryList() {
        var original = new PaginatedContestTypeSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedContestTypeSummaryList.class);
    }

    @Test
    void paginatedEggGroupSummaryList() {
        var original = new PaginatedEggGroupSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedEggGroupSummaryList.class);
    }

    @Test
    void paginatedEncounterConditionSummaryList() {
        var original = new PaginatedEncounterConditionSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedEncounterConditionSummaryList.class);
    }

    @Test
    void paginatedEncounterConditionValueSummaryList() {
        var original = new PaginatedEncounterConditionValueSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedEncounterConditionValueSummaryList.class);
    }

    @Test
    void paginatedEncounterMethodSummaryList() {
        var original = new PaginatedEncounterMethodSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedEncounterMethodSummaryList.class);
    }

    @Test
    void paginatedEvolutionChainSummaryList() {
        var original = new PaginatedEvolutionChainSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedEvolutionChainSummaryList.class);
    }

    @Test
    void paginatedEvolutionTriggerSummaryList() {
        var original = new PaginatedEvolutionTriggerSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedEvolutionTriggerSummaryList.class);
    }

    @Test
    void paginatedGenderSummaryList() {
        var original = new PaginatedGenderSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedGenderSummaryList.class);
    }

    @Test
    void paginatedGenerationSummaryList() {
        var original = new PaginatedGenerationSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedGenerationSummaryList.class);
    }

    @Test
    void paginatedGrowthRateSummaryList() {
        var original = new PaginatedGrowthRateSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedGrowthRateSummaryList.class);
    }

    @Test
    void paginatedItemAttributeSummaryList() {
        var original = new PaginatedItemAttributeSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedItemAttributeSummaryList.class);
    }

    @Test
    void paginatedItemCategorySummaryList() {
        var original = new PaginatedItemCategorySummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedItemCategorySummaryList.class);
    }

    @Test
    void paginatedItemFlingEffectSummaryList() {
        var original = new PaginatedItemFlingEffectSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedItemFlingEffectSummaryList.class);
    }

    @Test
    void paginatedItemPocketSummaryList() {
        var original = new PaginatedItemPocketSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedItemPocketSummaryList.class);
    }

    @Test
    void paginatedItemSummaryList() {
        var original = new PaginatedItemSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedItemSummaryList.class);
    }

    @Test
    void paginatedLanguageSummaryList() {
        var original = new PaginatedLanguageSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedLanguageSummaryList.class);
    }

    @Test
    void paginatedLocationAreaSummaryList() {
        var original = new PaginatedLocationAreaSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedLocationAreaSummaryList.class);
    }

    @Test
    void paginatedLocationSummaryList() {
        var original = new PaginatedLocationSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedLocationSummaryList.class);
    }

    @Test
    void paginatedMachineSummaryList() {
        var original = new PaginatedMachineSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMachineSummaryList.class);
    }

    @Test
    void paginatedMoveBattleStyleSummaryList() {
        var original = new PaginatedMoveBattleStyleSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMoveBattleStyleSummaryList.class);
    }

    @Test
    void paginatedMoveDamageClassSummaryList() {
        var original = new PaginatedMoveDamageClassSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMoveDamageClassSummaryList.class);
    }

    @Test
    void paginatedMoveLearnMethodSummaryList() {
        var original = new PaginatedMoveLearnMethodSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMoveLearnMethodSummaryList.class);
    }

    @Test
    void paginatedMoveMetaAilmentSummaryList() {
        var original = new PaginatedMoveMetaAilmentSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMoveMetaAilmentSummaryList.class);
    }

    @Test
    void paginatedMoveMetaCategorySummaryList() {
        var original = new PaginatedMoveMetaCategorySummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMoveMetaCategorySummaryList.class);
    }

    @Test
    void paginatedMoveSummaryList() {
        var original = new PaginatedMoveSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMoveSummaryList.class);
    }

    @Test
    void paginatedMoveTargetSummaryList() {
        var original = new PaginatedMoveTargetSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedMoveTargetSummaryList.class);
    }

    @Test
    void paginatedNatureSummaryList() {
        var original = new PaginatedNatureSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedNatureSummaryList.class);
    }

    @Test
    void paginatedPalParkAreaSummaryList() {
        var original = new PaginatedPalParkAreaSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPalParkAreaSummaryList.class);
    }

    @Test
    void paginatedPokeathlonStatSummaryList() {
        var original = new PaginatedPokeathlonStatSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokeathlonStatSummaryList.class);
    }

    @Test
    void paginatedPokedexSummaryList() {
        var original = new PaginatedPokedexSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokedexSummaryList.class);
    }

    @Test
    void paginatedPokemonColorSummaryList() {
        var original = new PaginatedPokemonColorSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokemonColorSummaryList.class);
    }

    @Test
    void paginatedPokemonFormSummaryList() {
        var original = new PaginatedPokemonFormSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokemonFormSummaryList.class);
    }

    @Test
    void paginatedPokemonHabitatSummaryList() {
        var original = new PaginatedPokemonHabitatSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokemonHabitatSummaryList.class);
    }

    @Test
    void paginatedPokemonShapeSummaryList() {
        var original = new PaginatedPokemonShapeSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokemonShapeSummaryList.class);
    }

    @Test
    void paginatedPokemonSpeciesSummaryList() {
        var original = new PaginatedPokemonSpeciesSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokemonSpeciesSummaryList.class);
    }

    @Test
    void paginatedPokemonSummaryList() {
        var original = new PaginatedPokemonSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedPokemonSummaryList.class);
    }

    @Test
    void paginatedRegionSummaryList() {
        var original = new PaginatedRegionSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedRegionSummaryList.class);
    }

    @Test
    void paginatedStatSummaryList() {
        var original = new PaginatedStatSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedStatSummaryList.class);
    }

    @Test
    void paginatedSuperContestEffectSummaryList() {
        var original = new PaginatedSuperContestEffectSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedSuperContestEffectSummaryList.class);
    }

    @Test
    void paginatedTypeSummaryList() {
        var original = new PaginatedTypeSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedTypeSummaryList.class);
    }

    @Test
    void paginatedVersionGroupSummaryList() {
        var original = new PaginatedVersionGroupSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedVersionGroupSummaryList.class);
    }

    @Test
    void paginatedVersionSummaryList() {
        var original = new PaginatedVersionSummaryList(null, null, null, null);
        assertSerializesSymmetrical(original, PaginatedVersionSummaryList.class);
    }

    @Test
    void palParkAreaDetail() {
        var original = new PalParkAreaDetail(null, null, null, null);
        assertSerializesSymmetrical(original, PalParkAreaDetail.class);
    }

    @Test
    void palParkAreaDetailPokemonEncounters() {
        var original = new PalParkAreaDetailPokemonEncountersItem(null, null, null);
        assertSerializesSymmetrical(original, PalParkAreaDetailPokemonEncountersItem.class);
    }


    @Test
    void pokeathlonStatDetail() {
        var original = new PokeathlonStatDetail(null, null, null, null);
        assertSerializesSymmetrical(original, PokeathlonStatDetail.class);
    }

    @Test
    void pokeathlonStatDetailAffectingNatures() {
        var original = new PokeathlonStatDetailAffectingNatures(null, null);
        assertSerializesSymmetrical(original, PokeathlonStatDetailAffectingNatures.class);
    }

    @Test
    void pokeathlonStatDetailAffectingNaturesDecrease() {
        var original = new PokeathlonStatDetailAffectingNaturesDecreaseItem(null, null);
        assertSerializesSymmetrical(original, PokeathlonStatDetailAffectingNaturesDecreaseItem.class);
    }


    @Test
    void pokedexDetail() {
        var original = new PokedexDetail(null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, PokedexDetail.class);
    }

    @Test
    void pokedexDetailPokemonEntries() {
        var original = new PokedexDetailPokemonEntriesItem(null, null);
        assertSerializesSymmetrical(original, PokedexDetailPokemonEntriesItem.class);
    }


    @Test
    void pokemonColorDetail() {
        var original = new PokemonColorDetail(null, null, null, null);
        assertSerializesSymmetrical(original, PokemonColorDetail.class);
    }


    @Test
    void pokemonDetail() {
        var original = new PokemonDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, PokemonDetail.class);
    }

    @Test
    void pokemonDetailAbilities() {
        var original = new PokemonDetailAbilitiesItem(null, null, null);
        assertSerializesSymmetrical(original, PokemonDetailAbilitiesItem.class);
    }

    @Test
    void pokemonDetailCries() {
        var original = new PokemonDetailCries(null, null);
        assertSerializesSymmetrical(original, PokemonDetailCries.class);
    }

    @Test
    void pokemonDetailHeldItems() {
        var original = new PokemonDetailHeldItemsItem(null, null);
        assertSerializesSymmetrical(original, PokemonDetailHeldItemsItem.class);
    }

    @Test
    void pokemonDetailMoves() {
        var original = new PokemonDetailMovesItem(null, null);
        assertSerializesSymmetrical(original, PokemonDetailMovesItem.class);
    }

    @Test
    void pokemonDetailMovesVersionGroupDetails() {
        var original = new PokemonDetailMovesItemVersionGroupDetailsItem(null, null, null);
        assertSerializesSymmetrical(original, PokemonDetailMovesItemVersionGroupDetailsItem.class);
    }

    @Test
    void pokemonDetailPastAbilities() {
        var original = new PokemonDetailPastAbilitiesItem(null, null);
        assertSerializesSymmetrical(original, PokemonDetailPastAbilitiesItem.class);
    }

    @Test
    void pokemonDetailPastTypes() {
        var original = new PokemonDetailPastTypesItem(null, null);
        assertSerializesSymmetrical(original, PokemonDetailPastTypesItem.class);
    }

    @Test
    void pokemonDetailTypes() {
        var original = new PokemonDetailTypesItem(null, null);
        assertSerializesSymmetrical(original, PokemonDetailTypesItem.class);
    }

    @Test
    void pokemonDexEntry() {
        var original = new PokemonDexEntry(null, null);
        assertSerializesSymmetrical(original, PokemonDexEntry.class);
    }

    @Test
    void pokemonFormDetail() {
        var original = new PokemonFormDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, PokemonFormDetail.class);
    }

    @Test
    void pokemonFormDetailFormNames() {
        var original = new PokemonFormDetailFormNamesItem(null, null);
        assertSerializesSymmetrical(original, PokemonFormDetailFormNamesItem.class);
    }


    @Test
    void pokemonGameIndex() {
        var original = new PokemonGameIndex(null, null);
        assertSerializesSymmetrical(original, PokemonGameIndex.class);
    }

    @Test
    void pokemonHabitatDetail() {
        var original = new PokemonHabitatDetail(null, null, null, null);
        assertSerializesSymmetrical(original, PokemonHabitatDetail.class);
    }


    @Test
    void pokemonShapeDetail() {
        var original = new PokemonShapeDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, PokemonShapeDetail.class);
    }

    @Test
    void pokemonShapeDetailAwesomeNames() {
        var original = new PokemonShapeDetailAwesomeNamesItem(null, null);
        assertSerializesSymmetrical(original, PokemonShapeDetailAwesomeNamesItem.class);
    }


    @Test
    void pokemonSpeciesDetail() {
        var original = new PokemonSpeciesDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetail.class);
    }

    @Test
    void pokemonSpeciesDetailGenera() {
        var original = new PokemonSpeciesDetailGeneraItem(null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetailGeneraItem.class);
    }

    @Test
    void pokemonSpeciesDetailPalParkEncounters() {
        var original = new PokemonSpeciesDetailPalParkEncountersItem(null, null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetailPalParkEncountersItem.class);
    }

    @Test
    void pokemonSpeciesDetailVarieties() {
        var original = new PokemonSpeciesDetailVarietiesItem(null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetailVarietiesItem.class);
    }

    @Test
    void pokemonSpeciesFlavorText() {
        var original = new PokemonSpeciesFlavorText(null, null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesFlavorText.class);
    }


    @Test
    void pokemonStat() {
        var original = new PokemonStat(null, null, null);
        assertSerializesSymmetrical(original, PokemonStat.class);
    }


    @Test
    void regionDetail() {
        var original = new RegionDetail(null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, RegionDetail.class);
    }


    @Test
    void statDetail() {
        var original = new StatDetail(null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, StatDetail.class);
    }


    @Test
    void superContestEffectDetail() {
        var original = new SuperContestEffectDetail(null, null, null, null);
        assertSerializesSymmetrical(original, SuperContestEffectDetail.class);
    }


    @Test
    void typeDetail() {
        var original = new TypeDetail(null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, TypeDetail.class);
    }

    @Test
    void typeDetailDamageRelations() {
        var original = new TypeDetailDamageRelations(null, null, null, null, null, null);
        assertSerializesSymmetrical(original, TypeDetailDamageRelations.class);
    }

    @Test
    void typeDetailPastDamageRelations() {
        var original = new TypeDetailPastDamageRelationsItem(null, null);
        assertSerializesSymmetrical(original, TypeDetailPastDamageRelationsItem.class);
    }

    @Test
    void typeDetailPokemon() {
        var original = new TypeDetailPokemonItem(1, null, null, null);
        assertSerializesSymmetrical(original, TypeDetailPokemonItem.class);
    }


    @Test
    void versionDetail() {
        var original = new VersionDetail(null, null, null, null);
        assertSerializesSymmetrical(original, VersionDetail.class);
    }

    @Test
    void versionGroupDetail() {
        var original = new VersionGroupDetail(null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, VersionGroupDetail.class);
    }


}
