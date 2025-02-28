package ru.netology.test;

import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.SQLHelper;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static ru.netology.data.SQLHelper.cleanAuthCodes;
import static ru.netology.data.SQLHelper.cleanDatabase;

public class BankLoginTest {
    LoginPage loginPage;

    @AfterEach
    void tearDown() {
        cleanAuthCodes();
    }

    @AfterAll
    static void tearDownAll() {
        cleanDatabase();
    }

    @BeforeEach
    void setUp() {
        loginPage = open("http://localhost:9999", LoginPage.class);
    }

    @Test
    @DisplayName("Should login with defauld data")
    void shouldLoginCorrectlyTest() {
        var authInfo = DataHelper.getAuthInfoWithTestData();
        var verificationPage = loginPage.login(authInfo);
        verificationPage.verifyVerificationPageVisibility();
        var verificationCode = SQLHelper.getVerificationCode();
        verificationPage.validVerify(verificationCode);
    }

    @Test
    @DisplayName("Should get error if unknown user")
    void shouldGetErrorIfRandomUserLoginTest() {
        var authInfo = DataHelper.generateRandomUser();
        loginPage.login(authInfo);
        loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
    }

    @Test
    @DisplayName("Should get error if existing user send random verification code")
    void shouldGetErrorNotificationIfWrongVerificationCode() {
        var authInfo = DataHelper.getAuthInfoWithTestData();
        var verificationPage = loginPage.login(authInfo);
        verificationPage.verifyVerificationPageVisibility();
        var verificationCode = DataHelper.generateRandomVerificationCode();
        verificationPage.verify(verificationCode.getCode());
        verificationPage.verifyErrorNotification("Ошибка! Неверно указан код! Попробуйте ещё раз.");
    }

    @Test
    @DisplayName("Should block user after three incorrect password attempts")
    void shouldBlockUserAfterThreeFailedAttempts() {
        var authInfo = DataHelper.getAuthInfoWithTestData();

        for (int i = 0; i < 3; i++) {
            loginPage.login(new DataHelper.AuthInfo(authInfo.getLogin(), DataHelper.generateWrongPassword(authInfo)));
            loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
        }
        loginPage.login(authInfo);
        loginPage.verifyErrorNotification("Ошибка! Превышено количество попыток. Пользователь заблокирован.");
    }

    @Test
    @DisplayName("Should not login with incorrect login and correct password")
    void shouldNotLoginWithIncorrectLogin() {
        var authInfo = DataHelper.getAuthInfoWithTestData();
        var wrongAuthInfo = new DataHelper.AuthInfo("wrongUser", authInfo.getPassword());
        loginPage.login(wrongAuthInfo);
        loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
    }

    @Test
    @DisplayName("Should not login with correct login and incorrect password")
    void shouldNotLoginWithIncorrectPassword() {
        var authInfo = DataHelper.getAuthInfoWithTestData();
        var wrongAuthInfo = new DataHelper.AuthInfo(authInfo.getLogin(), "wrongPassword");
        loginPage.login(wrongAuthInfo);
        loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
    }
}
