package com.swedbank.bankservice.account.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swedbank.account.application.dto.AccountDto;
import com.swedbank.account.application.dto.AccountTransactionRequest;
import com.swedbank.account.application.dto.ExchangeRequest;
import com.swedbank.account.application.dto.ExchangeResponse;
import com.swedbank.account.application.infrastructure.aop.ExternalLoggingAspect;
import com.swedbank.account.domain.model.CreateAccountRequest;
import com.swedbank.bankservice.common.rest.BaseIntegrationTest;
import com.swedbank.common.application.Dto.MoneyDto;
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

import static com.swedbank.bankservice.common.utils.ApiUtil.mockPostApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTests extends BaseIntegrationTest {

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
				buildAccountReq("Main USD", "USD"),
				buildAccountReq("Main EUR", "EUR"),
				buildAccountReq("Main SEK", "SEK"),
				buildAccountReq("Saving USD", "USD")

				));

		var payload = objectMapper.writeValueAsString(userAccountRequest);

		var mvcResult = mockPostApi(mockMvc, payload, "/account/user", null, null)
				.andExpect(status().isCreated())
				.andReturn();

		List<AccountDto> accounts = objectMapper.readValue(
				mvcResult.getResponse().getContentAsString(),
				objectMapper.getTypeFactory().constructCollectionType(List.class, AccountDto.class)
		);

		assertThat(accounts).hasSize(4);
		return accounts;
	}

	@Test
	@DisplayName("Should successfully create a batch of user accounts")
	void userAccountsCreationTest() throws Exception {
		List<AccountDto> createdAccounts = performBulkAccountCreation("user1@gmail.com");
		assertThat(createdAccounts).isNotEmpty();
	}

	@Test
	@DisplayName("Should update account balance upon successful deposit")
	void accountDepositTest() throws Exception {
		var email = "user2@gmail.com";
		var accounts = performBulkAccountCreation(email);
		AccountTransactionRequest depositRequest = new AccountTransactionRequest();
		depositRequest.setAccountNumber(accounts.get(0).getAccountNumber());
		depositRequest.setValue(MoneyDto.builder()
				.amount(new BigDecimal("45.54"))
				.currency(Currency.getInstance("USD"))
				.build());
		depositRequest.setReference("Initial Deposit");

		var payload = objectMapper.writeValueAsString(depositRequest);

		var result = mockPostApi(mockMvc, payload, "/account/deposit", createTestUser(email), null)
				.andExpect(status().isOk())
				.andReturn();

		AccountDto updatedAccount = objectMapper.readValue(result.getResponse().getContentAsString(), AccountDto.class);

		assertThat(updatedAccount.getBalance().getAmount()).isEqualByComparingTo("45.54");
		assertThat(updatedAccount.getBalance().getCurrency()).isEqualTo(Currency.getInstance("USD"));
	}

	@Test
	@DisplayName("Should process withdrawal and log event to external tracker")
	void accountWithdrawalTest() throws Exception {
		var email = "user3@gmail.com";
		var accounts = performBulkAccountCreation(email);
		var accountNumber = accounts.get(0).getAccountNumber();

		AccountTransactionRequest deposit = new AccountTransactionRequest();
		deposit.setAccountNumber(accountNumber);
		deposit.setValue(MoneyDto.builder().amount(new BigDecimal("1000.00")).currency(Currency.getInstance("USD")).build());
		mockPostApi(mockMvc, objectMapper.writeValueAsString(deposit), "/account/deposit", createTestUser(email), null);

		AccountTransactionRequest withdrawal = new AccountTransactionRequest();
		withdrawal.setAccountNumber(accountNumber);
		withdrawal.setValue(MoneyDto.builder().amount(new BigDecimal("145.10")).currency(Currency.getInstance("USD")).build());

		doNothing().when(externalLoggingAspect).logToExternalSystem(any());

		var result = mockPostApi(mockMvc, objectMapper.writeValueAsString(withdrawal), "/account/withdraw", createTestUser(email), null)
				.andExpect(status().isOk())
				.andReturn();

		AccountDto updatedAccount = objectMapper.readValue(result.getResponse().getContentAsString(), AccountDto.class);

		assertThat(updatedAccount.getBalance().getAmount()).isEqualByComparingTo("854.90");
	}

	@Test
	@DisplayName("Should execute multi-currency cross rate exchanges correctly using base triangulation rules")
	void accountCurrencyExchangeTest() throws Exception {
		var email = "user4@gmail.com";
		var accounts = performBulkAccountCreation(email);
		var accountUSD = accounts.get(0).getAccountNumber();
		var accountEUR = accounts.get(1).getAccountNumber();
		var accountSEK = accounts.get(2).getAccountNumber();
		var accountSavingUSD = accounts.get(3).getAccountNumber();

		AccountTransactionRequest deposit = new AccountTransactionRequest();
		deposit.setAccountNumber(accountUSD);
		deposit.setValue(MoneyDto.builder().amount(new BigDecimal("5000.00")).currency(Currency.getInstance("USD")).build());
		mockPostApi(mockMvc, objectMapper.writeValueAsString(deposit), "/account/deposit", createTestUser(email), null);

		ExchangeRequest usdToEurRequest = new ExchangeRequest();
		usdToEurRequest.setSourceAccountNumber(accountUSD);
		usdToEurRequest.setDestinationAccountNumber(accountEUR);
		usdToEurRequest.setValue(MoneyDto.builder().amount(new BigDecimal("500.00")).currency(Currency.getInstance("USD")).build());

		var resultEur = mockPostApi(mockMvc, objectMapper.writeValueAsString(usdToEurRequest), "/account/exchange", createTestUser(email), null)
				.andExpect(status().isOk())
				.andReturn();

		ExchangeResponse responseEur = objectMapper.readValue(resultEur.getResponse().getContentAsString(), ExchangeResponse.class);
		assertThat(responseEur.getSourceAccount().getBalance().getAmount()).isEqualByComparingTo("4500.00");
		assertThat(responseEur.getDestinationAccount().getBalance().getAmount()).isEqualByComparingTo("454.55");

		ExchangeRequest usdToSekRequest = new ExchangeRequest();
		usdToSekRequest.setSourceAccountNumber(accountUSD);
		usdToSekRequest.setDestinationAccountNumber(accountSEK);
		usdToSekRequest.setValue(MoneyDto.builder().amount(new BigDecimal("500.00")).currency(Currency.getInstance("USD")).build());

		var resultSek = mockPostApi(mockMvc, objectMapper.writeValueAsString(usdToSekRequest), "/account/exchange", createTestUser(email), null)
				.andExpect(status().isOk())
				.andReturn();

		ExchangeResponse responseSek = objectMapper.readValue(resultSek.getResponse().getContentAsString(), ExchangeResponse.class);
		assertThat(responseSek.getSourceAccount().getBalance().getAmount()).isEqualByComparingTo("4000.00");
		assertThat(responseSek.getDestinationAccount().getBalance().getAmount()).isEqualByComparingTo("5227.27");

		ExchangeRequest usdToUsdRequest = new ExchangeRequest();
		usdToUsdRequest.setSourceAccountNumber(accountUSD);
		usdToUsdRequest.setDestinationAccountNumber(accountSavingUSD);
		usdToUsdRequest.setValue(MoneyDto.builder().amount(new BigDecimal("500.00")).currency(Currency.getInstance("USD")).build());

		var resultUsd = mockPostApi(mockMvc, objectMapper.writeValueAsString(usdToUsdRequest), "/account/exchange", createTestUser(email), null)
				.andExpect(status().isOk())
				.andReturn();

		ExchangeResponse responseUsd = objectMapper.readValue(resultUsd.getResponse().getContentAsString(), ExchangeResponse.class);
		assertThat(responseUsd.getSourceAccount().getBalance().getAmount()).isEqualByComparingTo("3500.00");
		assertThat(responseUsd.getDestinationAccount().getBalance().getAmount()).isEqualByComparingTo("500.00");
	}
}