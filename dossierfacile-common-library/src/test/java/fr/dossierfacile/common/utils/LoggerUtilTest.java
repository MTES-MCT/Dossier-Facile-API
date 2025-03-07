package fr.dossierfacile.common.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggerUtilTest {


    static Stream<Arguments> provideUrisForNormalization() {
        return Stream.of(
                Arguments.of("/api/user/123", "/api/user/{id}"),
                Arguments.of("/api/user/456/details", "/api/user/{id}/details"),
                Arguments.of("/api/orders/999/item/42", "/api/orders/{id}/item/{id}"),
                Arguments.of("/simple/path", "/simple/path"),  // Pas de changement si pas de chiffres
                Arguments.of("/user/profile/0", "/user/profile/{id}"), // Gère le "0"
                Arguments.of("/products/123/edit/456", "/products/{id}/edit/{id}"), // Plusieurs IDs
                Arguments.of("/preview/a79b75f3-bd1d-470e-9cd1-569672200d87", "/preview/{uuid}"), // UUID
                Arguments.of("/preview/12/file/a79b75f3-bd1d-470e-9cd1-569672200d87", "/preview/{id}/file/{uuid}"), // UUID
                Arguments.of("/preview/a79b75f3-bd1d-470e-9cd1-569672200d87/file/a79b75f3-bd1d-470e-9cd1-569672200d87", "/preview/{uuid}/file/{uuid}"), // plusieurs UUID
                Arguments.of("/email/nicolas.sagon@gmail.com", "/email/{email}"),
                Arguments.of("/email/nicolas.sagon@gmail.com/profile", "/email/{email}/profile"),
                Arguments.of("/email/nicolas.sagon@gmail.com/profile/123/a79b75f3-bd1d-470e-9cd1-569672200d87", "/email/{email}/profile/{id}/{uuid}")
        );
    }

    @ParameterizedTest
    @MethodSource("provideUrisForNormalization")
    void normalizedUriParametrizedTest(String inputUri, String expectedUri) {
        // Utilisation de la réflexion pour appeler la méthode privée
        String result = LoggerUtil.normalizeUrl(inputUri);

        // Vérification du résultat
        assertThat(result).isEqualTo(expectedUri);
    }

}
