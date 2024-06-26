package com.github.memoryh.nutritionfacts.api.exception.controller;

import com.github.memoryh.nutritionfacts.api.exception.NoSuchDBException;
import com.github.memoryh.nutritionfacts.api.service.ProductTypeCheck;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExceptionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductTypeCheck productTypeCheck;

    @Test
    @DisplayName("데이터베이스에 저장되지 않은 제품명을 조회한 경우")
    void queryForNonexistentProductNameInDatabase() throws Exception {
        mockMvc.perform(get("/api/product")
                        .param("name", "테스트")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"등록되지 않은 제품명입니다.\"}",
                        true)
                )
                .andDo(print());
    }

    @Test
    @DisplayName("데이터베이스에 저장되지 않은 제품명을 조회한 경우 예외처리")
    void exceptionHandlingForNonexistentProductNameQuery() {
        // 데이터베이스에 저장되지 않은 제품명으로 findByProductInfo() 메서드를 호출했을 때 발생하는 예외를 캡처한다.
        Throwable thrown = catchThrowable(() -> productTypeCheck.checkProductType("테스트 제품"));

        // Assert: 캡처된 예외가 'RuntimeException' 타입인지 검증한다.
        // 'productService.findByProductInfo()' 메서드가 'RuntimeException'을 정상적으로 발생시키는지 확인한다.
        assertThat(thrown).isInstanceOf(RuntimeException.class);

        // Assert: 발생한 'RuntimeException'의 원인이 또 다른 'RuntimeException' 타입인지 검증한다.
        // 일반적으로 예외가 감싸진 경우 이런 검증이 필요할 수 있음
        assertThat(thrown.getCause()).isInstanceOf(RuntimeException.class);

        // Assert: 두 번째 'RuntimeException'의 원인이 'NoSuchDBException' 타입인지 검증한다.
        // 이 검증은 우리가 예상하는 예외가 예외 체인의 어딘가에 포함되어 있는지 확인하는 데 사용된다.
        assertThat(thrown.getCause().getCause()).isInstanceOf(NoSuchDBException.class);
    }

}