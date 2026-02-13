package com.dztech.auth.controller;

import com.dztech.auth.dto.HelpCategoryRequest;
import com.dztech.auth.dto.HelpCategoryResponse;
import com.dztech.auth.dto.HelpItemRequest;
import com.dztech.auth.dto.HelpItemResponse;
import com.dztech.auth.service.HelpCategoryServiceSimple;
import com.dztech.auth.service.HelpItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(HelpController.class)
public class HelpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HelpCategoryServiceSimple helpCategoryService;

    @MockBean
    private HelpItemService helpItemService;

    @Test
    public void testGetAllHelpCategories() throws Exception {
        // Mock data
        HelpItemResponse item1 = new HelpItemResponse();
        item1.setId(1L);
        item1.setTitle("Customer not reachable");
        item1.setDescription("If the customer is not reachable, please try calling once and wait for 5 minutes before cancelling.");
        item1.setEmail("trip@yourapp.com");
        item1.setCreatedAt(Instant.now());
        item1.setUpdatedAt(Instant.now());

        HelpCategoryResponse category = new HelpCategoryResponse();
        category.setId(1L);
        category.setTitle("Trip Related");
        category.setCategoryKey("trip");
        category.setAppId("rydd");
        category.setItems(Arrays.asList(item1));
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());

        when(helpCategoryService.getAllHelpCategories(anyString())).thenReturn(Arrays.asList(category));

        mockMvc.perform(get("/api/help")
                .header("appId", "rydd")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Trip Related"))
                .andExpect(jsonPath("$[0].categoryKey").value("trip"))
                .andExpect(jsonPath("$[0].items[0].title").value("Customer not reachable"));
    }

    @Test
    public void testGetHelpCategoryByCategoryKey() throws Exception {
        HelpItemResponse item = new HelpItemResponse();
        item.setId(1L);
        item.setTitle("Customer not reachable");
        item.setDescription("If the customer is not reachable, please try calling once and wait for 5 minutes before cancelling.");
        item.setEmail("trip@yourapp.com");
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());

        HelpCategoryResponse category = new HelpCategoryResponse();
        category.setId(1L);
        category.setTitle("Trip Related");
        category.setCategoryKey("trip");
        category.setAppId("rydd");
        category.setItems(Arrays.asList(item));
        category.setCreatedAt(Instant.now());
        category.setUpdatedAt(Instant.now());

        when(helpCategoryService.getHelpCategoryByCategoryKey(anyString(), anyString())).thenReturn(category);

        mockMvc.perform(get("/api/help/trip")
                .header("appId", "rydd")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Trip Related"))
                .andExpect(jsonPath("$.categoryKey").value("trip"));
    }

    @Test
    public void testCreateHelpCategory() throws Exception {
        HelpCategoryRequest request = new HelpCategoryRequest();
        request.setTitle("Trip Related");
        request.setCategoryKey("trip");
        request.setAppId("rydd");

        HelpCategoryResponse response = new HelpCategoryResponse();
        response.setId(1L);
        response.setTitle("Trip Related");
        response.setCategoryKey("trip");
        response.setAppId("rydd");
        response.setItems(Collections.emptyList());
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(helpCategoryService.createHelpCategory(any(HelpCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/help")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Trip Related\",\"categoryKey\":\"trip\",\"appId\":\"rydd\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Trip Related"))
                .andExpect(jsonPath("$.categoryKey").value("trip"));
    }

    @Test
    public void testCreateHelpItem() throws Exception {
        HelpItemRequest request = new HelpItemRequest();
        request.setCategoryId(1L);
        request.setTitle("Customer not reachable");
        request.setDescription("If the customer is not reachable, please try calling once and wait for 5 minutes before cancelling.");
        request.setEmail("trip@yourapp.com");

        HelpItemResponse response = new HelpItemResponse();
        response.setId(1L);
        response.setTitle("Customer not reachable");
        response.setDescription("If the customer is not reachable, please try calling once and wait for 5 minutes before cancelling.");
        response.setEmail("trip@yourapp.com");
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());

        when(helpItemService.createHelpItem(any(HelpItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/help/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1,\"title\":\"Customer not reachable\",\"description\":\"If the customer is not reachable, please try calling once and wait for 5 minutes before cancelling.\",\"email\":\"trip@yourapp.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Customer not reachable"))
                .andExpect(jsonPath("$.email").value("trip@yourapp.com"));
    }
}