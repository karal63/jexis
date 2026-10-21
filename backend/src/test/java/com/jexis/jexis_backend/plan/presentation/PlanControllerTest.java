package com.jexis.jexis_backend.plan.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.plan.application.dto.CreatePriceDto;
import com.jexis.jexis_backend.plan.application.dto.SavePlanEntitlementDto;
import com.jexis.jexis_backend.plan.application.dto.UpdatePriceDto;
import com.jexis.jexis_backend.plan.application.useCases.*;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CreatePlanUseCase createPlanUseCase;
    @Mock
    private GetAllPlansUseCase getAllPlansUseCase;
    @Mock
    private GetPlanEntitlementsUseCase getPlanEntitlementsUseCase;
    @Mock
    private SavePlanEntitlementsUseCase savePlanEntitlementsUseCase;
    @Mock
    private DeletePlanUseCase deletePlanUseCase;
    @Mock
    private UpdatePlanUseCase updatePlanUseCase;
    @Mock
    private GetPlanPricesUseCase getPlanPricesUseCase;
    @Mock
    private CreatePriceUseCase createPriceUseCase;
    @Mock
    private GetPlanPriceUseCase getPlanPriceUseCase;
    @Mock
    private UpdatePriceUseCase updatePriceUseCase;

    @InjectMocks
    private PlanController planController;

    private Plan plan;
    private UUID planId;
    private UUID priceId;
    private Price price;
    private UUID entitlementId;
    private Entitlement entitlement;
    private PlanEntitlement planEntitlement;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(planController).build();
        objectMapper = new ObjectMapper();

        planId = UUID.randomUUID();
        plan = new Plan("price_123", "Basic", "BASIC", "Description", true, com.jexis.jexis_backend.plan.domain.enums.PlanStatus.DRAFT);
        plan.setId(planId);

        priceId = UUID.randomUUID();
        price = new Price(plan, "price_stripe_123", "usd", 1000L, "month", 1, true);
        price.setId(priceId);

        entitlementId = UUID.randomUUID();
        entitlement = new Entitlement("storage", "NUMERIC", "Storage limit");
        entitlement.setId(entitlementId);

        planEntitlement = new PlanEntitlement(plan, entitlement, "10GB");
        planEntitlement.setId(UUID.randomUUID());
    }

    @Test
    void savePlanEntitlements_ShouldCallUseCaseWithList() throws Exception {
        List<SavePlanEntitlementDto> dtos = List.of(
                new SavePlanEntitlementDto(entitlementId, "10GB")
        );
        when(savePlanEntitlementsUseCase.execute(eq(planId), any())).thenReturn(List.of(planEntitlement));

        mockMvc.perform(post("/admin/plans/{id}/entitlements", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value("10GB"));

        verify(savePlanEntitlementsUseCase).execute(eq(planId), any());
    }

    @Test
    void getPlanPrices_ShouldReturnPlanPrices() throws Exception {
        when(getPlanPricesUseCase.execute(planId)).thenReturn(List.of(price));

        mockMvc.perform(get("/admin/plans/{id}/prices", planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(priceId.toString()))
                .andExpect(jsonPath("$[0].currency").value("usd"))
                .andExpect(jsonPath("$[0].unitAmount").value(1000));

        verify(getPlanPricesUseCase).execute(planId);
    }

    @Test
    void createPrice_ShouldCallUseCaseAndReturnPrice() throws Exception {
        CreatePriceDto dto = new CreatePriceDto("usd", 1000L, "month", 1, true);
        when(createPriceUseCase.execute(eq(planId), any(CreatePriceDto.class))).thenReturn(price);

        mockMvc.perform(post("/admin/plans/{id}/prices", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(priceId.toString()))
                .andExpect(jsonPath("$.unitAmount").value(1000));

        verify(createPriceUseCase).execute(eq(planId), any(CreatePriceDto.class));
    }

    @Test
    void getPrice_ShouldReturnPriceById() throws Exception {
        when(getPlanPriceUseCase.execute(priceId)).thenReturn(price);

        mockMvc.perform(get("/admin/plans/prices/{priceId}", priceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(priceId.toString()));

        verify(getPlanPriceUseCase).execute(priceId);
    }

    @Test
    void updatePrice_ShouldCallUseCaseAndReturnPrice() throws Exception {
        UpdatePriceDto dto = new UpdatePriceDto(false);
        price.setActive(false);
        when(updatePriceUseCase.execute(eq(priceId), any(UpdatePriceDto.class))).thenReturn(price);

        mockMvc.perform(patch("/admin/plans/prices/{priceId}", priceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(updatePriceUseCase).execute(eq(priceId), any(UpdatePriceDto.class));
    }
}
