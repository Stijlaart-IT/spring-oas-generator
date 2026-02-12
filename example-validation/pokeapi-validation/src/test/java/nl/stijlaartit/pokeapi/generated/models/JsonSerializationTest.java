package nl.stijlaartit.pokeapi.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
        var original = new AbilityDetailPokemon(null, null, null);
        assertSerializesSymmetrical(original, AbilityDetailPokemon.class);
    }

    @Test
    void abilityDetailPokemonPokemon() {
        var original = new AbilityDetailPokemonPokemon(null, null);
        assertSerializesSymmetrical(original, AbilityDetailPokemonPokemon.class);
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
    void apiV2PokemonEncountersRetrieveResponseItem() {
        var original = new ApiV2PokemonEncountersRetrieveResponseItem(null, null);
        assertSerializesSymmetrical(original, ApiV2PokemonEncountersRetrieveResponseItem.class);
    }

    @Test
    void apiV2PokemonEncountersRetrieveResponseItemVersionDetails() {
        var original = new ApiV2PokemonEncountersRetrieveResponseItemVersionDetails(null, null, null);
        assertSerializesSymmetrical(original, ApiV2PokemonEncountersRetrieveResponseItemVersionDetails.class);
    }

    @Test
    void apiV2PokemonEncountersRetrieveResponseItemVersionDetailsEncounterDetails() {
        var original = new ApiV2PokemonEncountersRetrieveResponseItemVersionDetailsEncounterDetails(null, null, null, null, null);
        assertSerializesSymmetrical(original, ApiV2PokemonEncountersRetrieveResponseItemVersionDetailsEncounterDetails.class);
    }

    @Test
    void berryDetail() {
        var original = new BerryDetail(null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, BerryDetail.class);
    }

    @Test
    void berryDetailFlavors() {
        var original = new BerryDetailFlavors(null, null);
        assertSerializesSymmetrical(original, BerryDetailFlavors.class);
    }

    @Test
    void berryDetailFlavorsFlavor() {
        var original = new BerryDetailFlavorsFlavor(null, null);
        assertSerializesSymmetrical(original, BerryDetailFlavorsFlavor.class);
    }

    @Test
    void berryFirmnessDetail() {
        var original = new BerryFirmnessDetail(null, null, null, null);
        assertSerializesSymmetrical(original, BerryFirmnessDetail.class);
    }

    @Test
    void berryFirmnessName() {
        var original = new BerryFirmnessName(null, null);
        assertSerializesSymmetrical(original, BerryFirmnessName.class);
    }

    @Test
    void berryFirmnessSummary() {
        var original = new BerryFirmnessSummary(null, null);
        assertSerializesSymmetrical(original, BerryFirmnessSummary.class);
    }

    @Test
    void berryFlavorDetail() {
        var original = new BerryFlavorDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, BerryFlavorDetail.class);
    }

    @Test
    void berryFlavorDetailBerries() {
        var original = new BerryFlavorDetailBerries(null, null);
        assertSerializesSymmetrical(original, BerryFlavorDetailBerries.class);
    }

    @Test
    void berryFlavorName() {
        var original = new BerryFlavorName(null, null);
        assertSerializesSymmetrical(original, BerryFlavorName.class);
    }

    @Test
    void berryFlavorSummary() {
        var original = new BerryFlavorSummary(null, null);
        assertSerializesSymmetrical(original, BerryFlavorSummary.class);
    }

    @Test
    void berrySummary() {
        var original = new BerrySummary(null, null);
        assertSerializesSymmetrical(original, BerrySummary.class);
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
    void contestEffectEffectText() {
        var original = new ContestEffectEffectText(null, null);
        assertSerializesSymmetrical(original, ContestEffectEffectText.class);
    }

    @Test
    void contestEffectFlavorText() {
        var original = new ContestEffectFlavorText(null, null);
        assertSerializesSymmetrical(original, ContestEffectFlavorText.class);
    }

    @Test
    void contestEffectSummary() {
        var original = new ContestEffectSummary(null);
        assertSerializesSymmetrical(original, ContestEffectSummary.class);
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
    void contestTypeSummary() {
        var original = new ContestTypeSummary(null, null);
        assertSerializesSymmetrical(original, ContestTypeSummary.class);
    }

    @Test
    void eggGroupDetail() {
        var original = new EggGroupDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EggGroupDetail.class);
    }

    @Test
    void eggGroupDetailPokemonSpecies() {
        var original = new EggGroupDetailPokemonSpecies(null, null);
        assertSerializesSymmetrical(original, EggGroupDetailPokemonSpecies.class);
    }

    @Test
    void eggGroupName() {
        var original = new EggGroupName(null, null);
        assertSerializesSymmetrical(original, EggGroupName.class);
    }

    @Test
    void eggGroupSummary() {
        var original = new EggGroupSummary(null, null);
        assertSerializesSymmetrical(original, EggGroupSummary.class);
    }

    @Test
    void encounterConditionDetail() {
        var original = new EncounterConditionDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EncounterConditionDetail.class);
    }

    @Test
    void encounterConditionName() {
        var original = new EncounterConditionName(null, null);
        assertSerializesSymmetrical(original, EncounterConditionName.class);
    }

    @Test
    void encounterConditionSummary() {
        var original = new EncounterConditionSummary(null, null);
        assertSerializesSymmetrical(original, EncounterConditionSummary.class);
    }

    @Test
    void encounterConditionValueDetail() {
        var original = new EncounterConditionValueDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EncounterConditionValueDetail.class);
    }

    @Test
    void encounterConditionValueName() {
        var original = new EncounterConditionValueName(null, null);
        assertSerializesSymmetrical(original, EncounterConditionValueName.class);
    }

    @Test
    void encounterConditionValueSummary() {
        var original = new EncounterConditionValueSummary(null, null);
        assertSerializesSymmetrical(original, EncounterConditionValueSummary.class);
    }

    @Test
    void encounterMethodDetail() {
        var original = new EncounterMethodDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EncounterMethodDetail.class);
    }

    @Test
    void encounterMethodName() {
        var original = new EncounterMethodName(null, null);
        assertSerializesSymmetrical(original, EncounterMethodName.class);
    }

    @Test
    void encounterMethodSummary() {
        var original = new EncounterMethodSummary(null, null);
        assertSerializesSymmetrical(original, EncounterMethodSummary.class);
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
        var original = new EvolutionChainDetailChainEvolvesTo(null, null, null);
        assertSerializesSymmetrical(original, EvolutionChainDetailChainEvolvesTo.class);
    }

    @Test
    void evolutionChainDetailChainEvolvesToEvolutionDetails() {
        var original = new EvolutionChainDetailChainEvolvesToEvolutionDetails(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, EvolutionChainDetailChainEvolvesToEvolutionDetails.class);
    }

    @Test
    void evolutionChainSummary() {
        var original = new EvolutionChainSummary(null);
        assertSerializesSymmetrical(original, EvolutionChainSummary.class);
    }

    @Test
    void evolutionTriggerDetail() {
        var original = new EvolutionTriggerDetail(null, null, null, null);
        assertSerializesSymmetrical(original, EvolutionTriggerDetail.class);
    }

    @Test
    void evolutionTriggerName() {
        var original = new EvolutionTriggerName(null, null);
        assertSerializesSymmetrical(original, EvolutionTriggerName.class);
    }

    @Test
    void evolutionTriggerSummary() {
        var original = new EvolutionTriggerSummary(null, null);
        assertSerializesSymmetrical(original, EvolutionTriggerSummary.class);
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
        var original = new GenderDetailPokemonSpeciesDetails(null, null);
        assertSerializesSymmetrical(original, GenderDetailPokemonSpeciesDetails.class);
    }

    @Test
    void genderSummary() {
        var original = new GenderSummary(null, null);
        assertSerializesSymmetrical(original, GenderSummary.class);
    }

    @Test
    void generationDetail() {
        var original = new GenerationDetail(null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, GenerationDetail.class);
    }

    @Test
    void generationName() {
        var original = new GenerationName(null, null);
        assertSerializesSymmetrical(original, GenerationName.class);
    }

    @Test
    void generationSummary() {
        var original = new GenerationSummary(null, null);
        assertSerializesSymmetrical(original, GenerationSummary.class);
    }

    @Test
    void growthRateDescription() {
        var original = new GrowthRateDescription(null, null);
        assertSerializesSymmetrical(original, GrowthRateDescription.class);
    }

    @Test
    void growthRateDetail() {
        var original = new GrowthRateDetail(null, null, null, null, null, null);
        assertSerializesSymmetrical(original, GrowthRateDetail.class);
    }

    @Test
    void growthRateSummary() {
        var original = new GrowthRateSummary(null, null);
        assertSerializesSymmetrical(original, GrowthRateSummary.class);
    }

    @Test
    void itemAttributeDescription() {
        var original = new ItemAttributeDescription(null, null);
        assertSerializesSymmetrical(original, ItemAttributeDescription.class);
    }

    @Test
    void itemAttributeDetail() {
        var original = new ItemAttributeDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, ItemAttributeDetail.class);
    }

    @Test
    void itemAttributeName() {
        var original = new ItemAttributeName(null, null);
        assertSerializesSymmetrical(original, ItemAttributeName.class);
    }

    @Test
    void itemAttributeSummary() {
        var original = new ItemAttributeSummary(null, null);
        assertSerializesSymmetrical(original, ItemAttributeSummary.class);
    }

    @Test
    void itemCategoryDetail() {
        var original = new ItemCategoryDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, ItemCategoryDetail.class);
    }

    @Test
    void itemCategoryName() {
        var original = new ItemCategoryName(null, null);
        assertSerializesSymmetrical(original, ItemCategoryName.class);
    }

    @Test
    void itemCategorySummary() {
        var original = new ItemCategorySummary(null, null);
        assertSerializesSymmetrical(original, ItemCategorySummary.class);
    }

    @Test
    void itemDetail() {
        var original = new ItemDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, ItemDetail.class);
    }

    @Test
    void itemDetailHeldByPokemon() {
        var original = new ItemDetailHeldByPokemon(null, null);
        assertSerializesSymmetrical(original, ItemDetailHeldByPokemon.class);
    }

    @Test
    void itemDetailHeldByPokemonVersionDetails() {
        var original = new ItemDetailHeldByPokemonVersionDetails(null, null);
        assertSerializesSymmetrical(original, ItemDetailHeldByPokemonVersionDetails.class);
    }

    @Test
    void itemDetailMachines() {
        var original = new ItemDetailMachines(null, null);
        assertSerializesSymmetrical(original, ItemDetailMachines.class);
    }

    @Test
    void itemDetailSprites() {
        var original = new ItemDetailSprites(null);
        assertSerializesSymmetrical(original, ItemDetailSprites.class);
    }

    @Test
    void itemEffectText() {
        var original = new ItemEffectText(null, null, null);
        assertSerializesSymmetrical(original, ItemEffectText.class);
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
    void itemFlingEffectEffectText() {
        var original = new ItemFlingEffectEffectText(null, null);
        assertSerializesSymmetrical(original, ItemFlingEffectEffectText.class);
    }

    @Test
    void itemFlingEffectSummary() {
        var original = new ItemFlingEffectSummary(null, null);
        assertSerializesSymmetrical(original, ItemFlingEffectSummary.class);
    }

    @Test
    void itemGameIndex() {
        var original = new ItemGameIndex(null, null);
        assertSerializesSymmetrical(original, ItemGameIndex.class);
    }

    @Test
    void itemName() {
        var original = new ItemName(null, null);
        assertSerializesSymmetrical(original, ItemName.class);
    }

    @Test
    void itemPocketDetail() {
        var original = new ItemPocketDetail(null, null, null, null);
        assertSerializesSymmetrical(original, ItemPocketDetail.class);
    }

    @Test
    void itemPocketName() {
        var original = new ItemPocketName(null, null);
        assertSerializesSymmetrical(original, ItemPocketName.class);
    }

    @Test
    void itemPocketSummary() {
        var original = new ItemPocketSummary(null, null);
        assertSerializesSymmetrical(original, ItemPocketSummary.class);
    }

    @Test
    void itemSummary() {
        var original = new ItemSummary(null, null);
        assertSerializesSymmetrical(original, ItemSummary.class);
    }

    @Test
    void languageDetail() {
        var original = new LanguageDetail(null, null, null, null, null, null);
        assertSerializesSymmetrical(original, LanguageDetail.class);
    }

    @Test
    void languageName() {
        var original = new LanguageName(null, null);
        assertSerializesSymmetrical(original, LanguageName.class);
    }

    @Test
    void languageSummary() {
        var original = new LanguageSummary(null, null);
        assertSerializesSymmetrical(original, LanguageSummary.class);
    }

    @Test
    void locationAreaDetail() {
        var original = new LocationAreaDetail(null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, LocationAreaDetail.class);
    }

    @Test
    void locationAreaDetailEncounterMethodRates() {
        var original = new LocationAreaDetailEncounterMethodRates(null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailEncounterMethodRates.class);
    }

    @Test
    void locationAreaDetailEncounterMethodRatesVersionDetails() {
        var original = new LocationAreaDetailEncounterMethodRatesVersionDetails(null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailEncounterMethodRatesVersionDetails.class);
    }

    @Test
    void locationAreaDetailPokemonEncounters() {
        var original = new LocationAreaDetailPokemonEncounters(null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailPokemonEncounters.class);
    }

    @Test
    void locationAreaDetailPokemonEncountersVersionDetails() {
        var original = new LocationAreaDetailPokemonEncountersVersionDetails(null, null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailPokemonEncountersVersionDetails.class);
    }

    @Test
    void locationAreaDetailPokemonEncountersVersionDetailsEncounterDetails() {
        var original = new LocationAreaDetailPokemonEncountersVersionDetailsEncounterDetails(null, null, null, null, null);
        assertSerializesSymmetrical(original, LocationAreaDetailPokemonEncountersVersionDetailsEncounterDetails.class);
    }

    @Test
    void locationAreaName() {
        var original = new LocationAreaName(null, null);
        assertSerializesSymmetrical(original, LocationAreaName.class);
    }

    @Test
    void locationAreaSummary() {
        var original = new LocationAreaSummary(null, null);
        assertSerializesSymmetrical(original, LocationAreaSummary.class);
    }

    @Test
    void locationDetail() {
        var original = new LocationDetail(null, null, null, null, null, null);
        assertSerializesSymmetrical(original, LocationDetail.class);
    }

    @Test
    void locationGameIndex() {
        var original = new LocationGameIndex(null, null);
        assertSerializesSymmetrical(original, LocationGameIndex.class);
    }

    @Test
    void locationName() {
        var original = new LocationName(null, null);
        assertSerializesSymmetrical(original, LocationName.class);
    }

    @Test
    void locationSummary() {
        var original = new LocationSummary(null, null);
        assertSerializesSymmetrical(original, LocationSummary.class);
    }

    @Test
    void machineDetail() {
        var original = new MachineDetail(null, null, null, null);
        assertSerializesSymmetrical(original, MachineDetail.class);
    }

    @Test
    void machineSummary() {
        var original = new MachineSummary(null);
        assertSerializesSymmetrical(original, MachineSummary.class);
    }

    @Test
    void moveBattleStyleDetail() {
        var original = new MoveBattleStyleDetail(null, null, null);
        assertSerializesSymmetrical(original, MoveBattleStyleDetail.class);
    }

    @Test
    void moveBattleStyleName() {
        var original = new MoveBattleStyleName(null, null);
        assertSerializesSymmetrical(original, MoveBattleStyleName.class);
    }

    @Test
    void moveBattleStyleSummary() {
        var original = new MoveBattleStyleSummary(null, null);
        assertSerializesSymmetrical(original, MoveBattleStyleSummary.class);
    }

    @Test
    void moveChange() {
        var original = new MoveChange(null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveChange.class);
    }

    @Test
    void moveChangeEffectEntries() {
        var original = new MoveChangeEffectEntries(null, null, null);
        assertSerializesSymmetrical(original, MoveChangeEffectEntries.class);
    }

    @Test
    void moveDamageClassDescription() {
        var original = new MoveDamageClassDescription(null, null);
        assertSerializesSymmetrical(original, MoveDamageClassDescription.class);
    }

    @Test
    void moveDamageClassDetail() {
        var original = new MoveDamageClassDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveDamageClassDetail.class);
    }

    @Test
    void moveDamageClassName() {
        var original = new MoveDamageClassName(null, null);
        assertSerializesSymmetrical(original, MoveDamageClassName.class);
    }

    @Test
    void moveDamageClassSummary() {
        var original = new MoveDamageClassSummary(null, null);
        assertSerializesSymmetrical(original, MoveDamageClassSummary.class);
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
        var original = new MoveDetailEffectChanges(null, null);
        assertSerializesSymmetrical(original, MoveDetailEffectChanges.class);
    }

    @Test
    void moveDetailEffectChangesEffectEntries() {
        var original = new MoveDetailEffectChangesEffectEntries(null, null);
        assertSerializesSymmetrical(original, MoveDetailEffectChangesEffectEntries.class);
    }

    @Test
    void moveDetailMachines() {
        var original = new MoveDetailMachines(null, null);
        assertSerializesSymmetrical(original, MoveDetailMachines.class);
    }

    @Test
    void moveDetailStatChanges() {
        var original = new MoveDetailStatChanges(null, null);
        assertSerializesSymmetrical(original, MoveDetailStatChanges.class);
    }

    @Test
    void moveFlavorText() {
        var original = new MoveFlavorText(null, null, null);
        assertSerializesSymmetrical(original, MoveFlavorText.class);
    }

    @Test
    void moveLearnMethodDescription() {
        var original = new MoveLearnMethodDescription(null, null);
        assertSerializesSymmetrical(original, MoveLearnMethodDescription.class);
    }

    @Test
    void moveLearnMethodDetail() {
        var original = new MoveLearnMethodDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveLearnMethodDetail.class);
    }

    @Test
    void moveLearnMethodName() {
        var original = new MoveLearnMethodName(null, null);
        assertSerializesSymmetrical(original, MoveLearnMethodName.class);
    }

    @Test
    void moveLearnMethodSummary() {
        var original = new MoveLearnMethodSummary(null, null);
        assertSerializesSymmetrical(original, MoveLearnMethodSummary.class);
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
    void moveMetaAilmentName() {
        var original = new MoveMetaAilmentName(null, null);
        assertSerializesSymmetrical(original, MoveMetaAilmentName.class);
    }

    @Test
    void moveMetaAilmentSummary() {
        var original = new MoveMetaAilmentSummary(null, null);
        assertSerializesSymmetrical(original, MoveMetaAilmentSummary.class);
    }

    @Test
    void moveMetaCategoryDescription() {
        var original = new MoveMetaCategoryDescription(null, null);
        assertSerializesSymmetrical(original, MoveMetaCategoryDescription.class);
    }

    @Test
    void moveMetaCategoryDetail() {
        var original = new MoveMetaCategoryDetail(null, null, null, null);
        assertSerializesSymmetrical(original, MoveMetaCategoryDetail.class);
    }

    @Test
    void moveMetaCategorySummary() {
        var original = new MoveMetaCategorySummary(null, null);
        assertSerializesSymmetrical(original, MoveMetaCategorySummary.class);
    }

    @Test
    void moveName() {
        var original = new MoveName(null, null);
        assertSerializesSymmetrical(original, MoveName.class);
    }

    @Test
    void moveSummary() {
        var original = new MoveSummary(null, null);
        assertSerializesSymmetrical(original, MoveSummary.class);
    }

    @Test
    void moveTargetDescription() {
        var original = new MoveTargetDescription(null, null);
        assertSerializesSymmetrical(original, MoveTargetDescription.class);
    }

    @Test
    void moveTargetDetail() {
        var original = new MoveTargetDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, MoveTargetDetail.class);
    }

    @Test
    void moveTargetName() {
        var original = new MoveTargetName(null, null);
        assertSerializesSymmetrical(original, MoveTargetName.class);
    }

    @Test
    void moveTargetSummary() {
        var original = new MoveTargetSummary(null, null);
        assertSerializesSymmetrical(original, MoveTargetSummary.class);
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
        var original = new NatureDetailPokeathlonStatChanges(null, null);
        assertSerializesSymmetrical(original, NatureDetailPokeathlonStatChanges.class);
    }

    @Test
    void natureName() {
        var original = new NatureName(null, null);
        assertSerializesSymmetrical(original, NatureName.class);
    }

    @Test
    void natureSummary() {
        var original = new NatureSummary(null, null);
        assertSerializesSymmetrical(original, NatureSummary.class);
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
        var original = new PalParkAreaDetailPokemonEncounters(null, null, null);
        assertSerializesSymmetrical(original, PalParkAreaDetailPokemonEncounters.class);
    }

    @Test
    void palParkAreaName() {
        var original = new PalParkAreaName(null, null);
        assertSerializesSymmetrical(original, PalParkAreaName.class);
    }

    @Test
    void palParkAreaSummary() {
        var original = new PalParkAreaSummary(null, null);
        assertSerializesSymmetrical(original, PalParkAreaSummary.class);
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
        var original = new PokeathlonStatDetailAffectingNaturesDecrease(null, null);
        assertSerializesSymmetrical(original, PokeathlonStatDetailAffectingNaturesDecrease.class);
    }

    @Test
    void pokeathlonStatName() {
        var original = new PokeathlonStatName(null, null);
        assertSerializesSymmetrical(original, PokeathlonStatName.class);
    }

    @Test
    void pokeathlonStatSummary() {
        var original = new PokeathlonStatSummary(null, null);
        assertSerializesSymmetrical(original, PokeathlonStatSummary.class);
    }

    @Test
    void pokedexDescription() {
        var original = new PokedexDescription(null, null);
        assertSerializesSymmetrical(original, PokedexDescription.class);
    }

    @Test
    void pokedexDetail() {
        var original = new PokedexDetail(null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, PokedexDetail.class);
    }

    @Test
    void pokedexDetailPokemonEntries() {
        var original = new PokedexDetailPokemonEntries(null, null);
        assertSerializesSymmetrical(original, PokedexDetailPokemonEntries.class);
    }

    @Test
    void pokedexName() {
        var original = new PokedexName(null, null);
        assertSerializesSymmetrical(original, PokedexName.class);
    }

    @Test
    void pokedexSummary() {
        var original = new PokedexSummary(null, null);
        assertSerializesSymmetrical(original, PokedexSummary.class);
    }

    @Test
    void pokemonColorDetail() {
        var original = new PokemonColorDetail(null, null, null, null);
        assertSerializesSymmetrical(original, PokemonColorDetail.class);
    }

    @Test
    void pokemonColorName() {
        var original = new PokemonColorName(null, null);
        assertSerializesSymmetrical(original, PokemonColorName.class);
    }

    @Test
    void pokemonColorSummary() {
        var original = new PokemonColorSummary(null, null);
        assertSerializesSymmetrical(original, PokemonColorSummary.class);
    }

    @Test
    void pokemonDetail() {
        var original = new PokemonDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, PokemonDetail.class);
    }

    @Test
    void pokemonDetailAbilities() {
        var original = new PokemonDetailAbilities(null, null, null);
        assertSerializesSymmetrical(original, PokemonDetailAbilities.class);
    }

    @Test
    void pokemonDetailCries() {
        var original = new PokemonDetailCries(null, null);
        assertSerializesSymmetrical(original, PokemonDetailCries.class);
    }

    @Test
    void pokemonDetailHeldItems() {
        var original = new PokemonDetailHeldItems(null, null);
        assertSerializesSymmetrical(original, PokemonDetailHeldItems.class);
    }

    @Test
    void pokemonDetailMoves() {
        var original = new PokemonDetailMoves(null, null);
        assertSerializesSymmetrical(original, PokemonDetailMoves.class);
    }

    @Test
    void pokemonDetailMovesVersionGroupDetails() {
        var original = new PokemonDetailMovesVersionGroupDetails(null, null, null);
        assertSerializesSymmetrical(original, PokemonDetailMovesVersionGroupDetails.class);
    }

    @Test
    void pokemonDetailPastAbilities() {
        var original = new PokemonDetailPastAbilities(null, null);
        assertSerializesSymmetrical(original, PokemonDetailPastAbilities.class);
    }

    @Test
    void pokemonDetailPastTypes() {
        var original = new PokemonDetailPastTypes(null, null);
        assertSerializesSymmetrical(original, PokemonDetailPastTypes.class);
    }

    @Test
    void pokemonDetailTypes() {
        var original = new PokemonDetailTypes(null, null);
        assertSerializesSymmetrical(original, PokemonDetailTypes.class);
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
        var original = new PokemonFormDetailFormNames(null, null);
        assertSerializesSymmetrical(original, PokemonFormDetailFormNames.class);
    }

    @Test
    void pokemonFormSummary() {
        var original = new PokemonFormSummary(null, null);
        assertSerializesSymmetrical(original, PokemonFormSummary.class);
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
    void pokemonHabitatName() {
        var original = new PokemonHabitatName(null, null);
        assertSerializesSymmetrical(original, PokemonHabitatName.class);
    }

    @Test
    void pokemonHabitatSummary() {
        var original = new PokemonHabitatSummary(null, null);
        assertSerializesSymmetrical(original, PokemonHabitatSummary.class);
    }

    @Test
    void pokemonShapeDetail() {
        var original = new PokemonShapeDetail(null, null, null, null, null);
        assertSerializesSymmetrical(original, PokemonShapeDetail.class);
    }

    @Test
    void pokemonShapeDetailAwesomeNames() {
        var original = new PokemonShapeDetailAwesomeNames(null, null);
        assertSerializesSymmetrical(original, PokemonShapeDetailAwesomeNames.class);
    }

    @Test
    void pokemonShapeSummary() {
        var original = new PokemonShapeSummary(null, null);
        assertSerializesSymmetrical(original, PokemonShapeSummary.class);
    }

    @Test
    void pokemonSpeciesDescription() {
        var original = new PokemonSpeciesDescription(null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDescription.class);
    }

    @Test
    void pokemonSpeciesDetail() {
        var original = new PokemonSpeciesDetail(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetail.class);
    }

    @Test
    void pokemonSpeciesDetailGenera() {
        var original = new PokemonSpeciesDetailGenera(null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetailGenera.class);
    }

    @Test
    void pokemonSpeciesDetailPalParkEncounters() {
        var original = new PokemonSpeciesDetailPalParkEncounters(null, null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetailPalParkEncounters.class);
    }

    @Test
    void pokemonSpeciesDetailVarieties() {
        var original = new PokemonSpeciesDetailVarieties(null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesDetailVarieties.class);
    }

    @Test
    void pokemonSpeciesFlavorText() {
        var original = new PokemonSpeciesFlavorText(null, null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesFlavorText.class);
    }

    @Test
    void pokemonSpeciesSummary() {
        var original = new PokemonSpeciesSummary(null, null);
        assertSerializesSymmetrical(original, PokemonSpeciesSummary.class);
    }

    @Test
    void pokemonStat() {
        var original = new PokemonStat(null, null, null);
        assertSerializesSymmetrical(original, PokemonStat.class);
    }

    @Test
    void pokemonSummary() {
        var original = new PokemonSummary(null, null);
        assertSerializesSymmetrical(original, PokemonSummary.class);
    }

    @Test
    void regionDetail() {
        var original = new RegionDetail(null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, RegionDetail.class);
    }

    @Test
    void regionName() {
        var original = new RegionName(null, null);
        assertSerializesSymmetrical(original, RegionName.class);
    }

    @Test
    void regionSummary() {
        var original = new RegionSummary(null, null);
        assertSerializesSymmetrical(original, RegionSummary.class);
    }

    @Test
    void statDetail() {
        var original = new StatDetail(null, null, null, null, null, null, null, null, null);
        assertSerializesSymmetrical(original, StatDetail.class);
    }

    @Test
    void statName() {
        var original = new StatName(null, null);
        assertSerializesSymmetrical(original, StatName.class);
    }

    @Test
    void statSummary() {
        var original = new StatSummary(null, null);
        assertSerializesSymmetrical(original, StatSummary.class);
    }

    @Test
    void superContestEffectDetail() {
        var original = new SuperContestEffectDetail(null, null, null, null);
        assertSerializesSymmetrical(original, SuperContestEffectDetail.class);
    }

    @Test
    void superContestEffectFlavorText() {
        var original = new SuperContestEffectFlavorText(null, null);
        assertSerializesSymmetrical(original, SuperContestEffectFlavorText.class);
    }

    @Test
    void superContestEffectSummary() {
        var original = new SuperContestEffectSummary(null);
        assertSerializesSymmetrical(original, SuperContestEffectSummary.class);
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
        var original = new TypeDetailPastDamageRelations(null, null);
        assertSerializesSymmetrical(original, TypeDetailPastDamageRelations.class);
    }

    @Test
    void typeDetailPokemon() {
        var original = new TypeDetailPokemon(null, null);
        assertSerializesSymmetrical(original, TypeDetailPokemon.class);
    }

    @Test
    void typeDetailSprites() {
        var original = new TypeDetailSprites(null);
        assertSerializesSymmetrical(original, TypeDetailSprites.class);
    }

    @Test
    void typeGameIndex() {
        var original = new TypeGameIndex(null, null);
        assertSerializesSymmetrical(original, TypeGameIndex.class);
    }

    @Test
    void typeSummary() {
        var original = new TypeSummary(null, null);
        assertSerializesSymmetrical(original, TypeSummary.class);
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

    @Test
    void versionGroupSummary() {
        var original = new VersionGroupSummary(null, null);
        assertSerializesSymmetrical(original, VersionGroupSummary.class);
    }

    @Test
    void versionName() {
        var original = new VersionName(null, null);
        assertSerializesSymmetrical(original, VersionName.class);
    }

    @Test
    void versionSummary() {
        var original = new VersionSummary(null, null);
        assertSerializesSymmetrical(original, VersionSummary.class);
    }

}
