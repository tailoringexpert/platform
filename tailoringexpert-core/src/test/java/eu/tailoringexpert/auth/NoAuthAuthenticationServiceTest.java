package eu.tailoringexpert.auth;

import eu.tailoringexpert.domain.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoAuthAuthenticationServiceTest {

    NoAuthAuthenticationService service;

    @BeforeEach
    void beforeEach() {
        this.service = new NoAuthAuthenticationService();
    }

    @Test
    void authenticate_UserIdGiven_AuthenticationWithoutTokensRetuned() {
        // arrange

        // act
        Authentication actual = service.authenticate("demo", "test1234");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getUserId()).isEqualTo("demo");
        assertThat(actual.getAccessToken()).isNull();
        assertThat(actual.getRefreshToken()).isNull();
    }

    @Test
    void refreshToken_ParamsGiven_AuthenticationWithoutTokensRetuned() {
        // arrange

        // act
        Authentication actual = service.refresh("demo", "hey");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getUserId()).isEqualTo("demo");
        assertThat(actual.getAccessToken()).isNull();
        assertThat(actual.getRefreshToken()).isNull();
    }
}
