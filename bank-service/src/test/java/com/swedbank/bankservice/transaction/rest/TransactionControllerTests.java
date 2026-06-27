package com.swedbank.bankservice.transaction.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.account.application.dto.AccountTransactionRequest;
import com.swedbank.account.application.dto.ExchangeRequest;
import com.swedbank.account.application.dto.ExchangeResponse;
import com.swedbank.account.application.infrastructure.aop.ExternalLoggingAspect;
import com.swedbank.account.domain.model.CreateAccountRequest;
import com.swedbank.bankservice.common.rest.BaseIntegrationTest;
import com.swedbank.common.application.Dto.ErrorResponse;
import com.swedbank.common.application.Dto.MoneyDto;
import com.swedbank.common.application.exception.ExternalSystemException;
import com.swedbank.transaction.application.dto.PagedResult;
import com.swedbank.transaction.application.dto.TransactionDto;
import com.swedbank.transaction.application.dto.TransactionSearch;
import com.swedbank.user.application.dto.UserAccountRequest;
import com.swedbank.user.application.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static com.swedbank.bankservice.common.utils.ApiUtil.mockGetApi;
import static com.swedbank.bankservice.common.utils.ApiUtil.mockPostApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTests extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ExternalLoggingAspect externalLoggingAspect;

	private UserDto createTestUser(String email) {
		UserDto userDto = new UserDto();
		userDto.setFirstName("FirstName");
		userDto.setLastName("LastName");
		userDto.setEmail(email);
		userDto.setPassword("password");
		return userDto;
	}

	private CreateAccountRequest buildAccountReq(String name, String currencyCode) {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setAccountName(name);
		request.setCurrency(Currency.getInstance(currencyCode));
		return request;
	}

	/**
	 * Helper method to initialize a standard base state of accounts for tests.
	 */
	private List<AccountDto> performBulkAccountCreation(String email) throws Exception {
		UserAccountRequest userAccountRequest = new UserAccountRequest();
		userAccountRequest.setUser(createTestUser(email));

		userAccountRequest.setCreateAccounts(List.of(
				buildAccountReq("Main EUR", "EUR"),
				buildAccountReq("Main USD", "USD")
				));

		var payload = objectMapper.writeValueAsString(userAccountRequest);

		var mvcResult = mockPostApi(mockMvc, payload, "/account/user", null, null)
				.andExpect(status().isCreated())
				.andReturn();

		List<AccountDto> accounts = objectMapper.readValue(
				mvcResult.getResponse().getContentAsString(),
				objectMapper.getTypeFactory().constructCollectionType(List.class, AccountDto.class)
		);

		assertThat(accounts).hasSize(2);
		return accounts;
	}

	private void performAccountTransactions(String email, AccountDto eurAccount, AccountDto usdAccount) throws Exception {

		AccountTransactionRequest depositRequest = new AccountTransactionRequest();
		depositRequest.setAccountNumber(eurAccount.getAccountNumber());
		depositRequest.setValue(MoneyDto.builder()
				.amount(new BigDecimal("1000"))
				.currency(Currency.getInstance("EUR"))
				.build());
		depositRequest.setReference("Initial Deposit");

		var result = mockPostApi(mockMvc, objectMapper.writeValueAsString(depositRequest),
				"/account/deposit", createTestUser(email), null)
				.andExpect(status().isOk())
				.andReturn();

		AccountDto updatedAccount = objectMapper.readValue(result.getResponse().getContentAsString(), AccountDto.class);

		assertThat(updatedAccount.getBalance().getAmount()).isEqualByComparingTo("1000");
		assertThat(updatedAccount.getBalance().getCurrency()).isEqualTo(Currency.getInstance("EUR"));

		for (int i = 1; i <= 10; i++) {

			AccountTransactionRequest withdrawal = new AccountTransactionRequest();
			withdrawal.setAccountNumber(eurAccount.getAccountNumber());
			withdrawal.setValue(MoneyDto.builder().amount(new BigDecimal(i * 10)).currency(Currency.getInstance("EUR")).build());

			doNothing().when(externalLoggingAspect).logToExternalSystem(any());

			mockPostApi(mockMvc, objectMapper.writeValueAsString(withdrawal), "/account/withdraw", createTestUser(email), null)
					.andExpect(status().isOk())
					.andReturn();
		}

		ExchangeRequest eurToUsdRequest = new ExchangeRequest();
		eurToUsdRequest.setSourceAccountNumber(eurAccount.getAccountNumber());
		eurToUsdRequest.setDestinationAccountNumber(usdAccount.getAccountNumber());
		eurToUsdRequest.setValue(MoneyDto.builder().amount(new BigDecimal("150.00")).currency(Currency.getInstance("EUR")).build());

		var resultUsd = mockPostApi(mockMvc, objectMapper.writeValueAsString(eurToUsdRequest), "/account/exchange", createTestUser(email), null)
				.andExpect(status().isOk())
				.andReturn();

		ExchangeResponse responseUsd = objectMapper.readValue(resultUsd.getResponse().getContentAsString(), ExchangeResponse.class);
		assertThat(responseUsd.getSourceAccount().getBalance().getAmount()).isEqualByComparingTo("300.00");
	}


	@Test
	@DisplayName("Should fetch account transaction made by user")
	void accountTransactionTest() throws Exception {
		var email = "user6@gmail.com";
		var accounts = performBulkAccountCreation(email);
		var eurAccount = accounts.get(0);
		var usdAccount = accounts.get(1);

		performAccountTransactions(email, eurAccount, usdAccount);

		TransactionSearch transactionSearch = new TransactionSearch();

		var body = objectMapper.writeValueAsString(transactionSearch);

		var result = mockPostApi(mockMvc, body,
				"/transaction/account/" + eurAccount.getAccountNumber() + "/history",
				createTestUser(email),
				null).andExpect(status().isOk())
				.andReturn();

		PagedResult<TransactionDto> transactionDtoPagedResult = objectMapper.readValue(
				result.getResponse().getContentAsString(),
                new TypeReference<>() {}
		);

		assertThat(transactionDtoPagedResult.getContent().size()).isEqualTo(13);
		assertThat(transactionDtoPagedResult.getContent().get(12).getBalance().getAmount()).isEqualByComparingTo("300.00");

	}
}