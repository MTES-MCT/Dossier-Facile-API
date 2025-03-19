package fr.dossierfacile.api.front.controller.registerController;

import fr.dossierfacile.api.front.TestApplication;
import fr.dossierfacile.api.front.config.ResourceServerConfig;
import fr.dossierfacile.api.front.controller.RegisterController;
import fr.dossierfacile.api.front.controller.RegisterGuarantorNaturalPersonController;
import fr.dossierfacile.api.front.mapper.PropertyOMapperImpl;
import fr.dossierfacile.api.front.mapper.TenantMapperImpl;
import fr.dossierfacile.api.front.register.form.tenant.DocumentFinancialForm;
import fr.dossierfacile.api.front.register.form.tenant.DocumentResidencyForm;
import fr.dossierfacile.api.front.repository.FileRepository;
import fr.dossierfacile.api.front.repository.GuarantorRepository;
import fr.dossierfacile.api.front.security.interfaces.AuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.validator.NumberOfPagesValidator;
import fr.dossierfacile.common.config.GlobalExceptionHandler;
import fr.dossierfacile.common.mapper.VersionedCategoriesMapper;
import fr.dossierfacile.common.service.interfaces.LogService;
import fr.dossierfacile.parameterizedtest.ArgumentBuilder;
import fr.dossierfacile.parameterizedtest.ControllerParameter;
import fr.dossierfacile.parameterizedtest.ParameterizedTestHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RegisterGuarantorNaturalPersonController.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        TestApplication.class,
        TenantMapperImpl.class,
        PropertyOMapperImpl.class,
        VersionedCategoriesMapper.class,
        GlobalExceptionHandler.class,
        NumberOfPagesValidator.class,
        ResourceServerConfig.class
}
)
@TestPropertySource(properties = {"application.api.version = 4", "dossierfacile.common.global.exception.handler=true"})
class RegisterGuarantorNaturalPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private static TenantService tenantService;

    @MockBean
    private static AuthenticationFacade authenticationFacade;

    @MockBean
    private static LogService logService;

    @MockBean
    private static FileRepository fileRepository;

    @MockBean
    private static GuarantorRepository guarantorRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Nested
    class DocumentFinancialTest {

        private record DocumentFinancialParameter(DocumentFinancialForm documentFinancialForm) {
        }

        static List<Arguments> provideDocumentFinancialParameter() {
            SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtTokenWithDossier = jwt().authorities(new SimpleGrantedAuthority("SCOPE_dossier"));

            return ArgumentBuilder.buildListOfArguments(
                    Pair.of("Should respond 401 when not jwt is passed",
                            new ControllerParameter<>(
                                    null,
                                    401,
                                    null,
                                    null,
                                    Collections.emptyList()
                            )
                    ),
                    Pair.of("Should respond 400 when document financial form missing noDocument",
                            new ControllerParameter<>(
                                    new DocumentFinancialParameter(Helper.invalidDocumentBecauseNoDocument()),
                                    400,
                                    jwtTokenWithDossier,
                                    null,
                                    List.of(
                                            jsonPath("$.errors").isArray(),
                                            jsonPath("$.errors").value(hasItem("noDocument: must not be null"))
                                    )
                            )
                    ),
                    Pair.of("Should respond 400 when document financial form wrong type",
                            new ControllerParameter<>(
                                    new DocumentFinancialParameter(Helper.invalidDocumentBecauseWrongType()),
                                    400,
                                    jwtTokenWithDossier,
                                    null,
                                    List.of(
                                            jsonPath("$.errors").isArray(),
                                            jsonPath("$.errors").value(hasItem("typeDocumentFinancial: must be one of [SALARY, SOCIAL_SERVICE, RENT, PENSION, SCHOLARSHIP]"))
                                    )
                            )
                    ),
                    Pair.of("Should respond 400 when document financial form wrong step",
                            new ControllerParameter<>(
                                    new DocumentFinancialParameter(Helper.invalidDocumentBecauseWrongStep()),
                                    400,
                                    jwtTokenWithDossier,
                                    null,
                                    List.of(
                                            jsonPath("$.errors").isArray(),
                                            jsonPath("$.errors").value(hasItem("categoryStep: PENSION_NO_STATEMENT is not valid for document sub category SALARY"))
                                    )
                            )
                    ),
                    Pair.of("Should respond 400 when document financial form no step",
                            new ControllerParameter<>(
                                    new DocumentFinancialParameter(Helper.invalidDocumentBecauseNoStep()),
                                    400,
                                    jwtTokenWithDossier,
                                    null,
                                    List.of(
                                            jsonPath("$.errors").isArray(),
                                            jsonPath("$.errors").value(hasItem("categoryStep: For document sub category SCHOLARSHIP category step has to be null"))
                                    )
                            )
                    )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideDocumentFinancialParameter")
        void parameterizedTests(ControllerParameter<DocumentFinancialParameter> parameter) throws Exception {
            List<MockPart> parts = new ArrayList<>();
            if (parameter.getParameterData() != null) {
                var documentFinancialForm = parameter.getParameterData().documentFinancialForm;
                Field[] fields = documentFinancialForm.getClass().getDeclaredFields();
                Arrays.stream(fields).map(field -> {
                    field.setAccessible(true);
                    try {
                        if (field.get(documentFinancialForm) != null) {
                            return new MockPart(field.getName(), field.get(documentFinancialForm).toString().getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (IllegalAccessException e) {
                        System.err.println(e.getMessage());
                    }
                    return null;
                }).filter(Objects::nonNull).forEach(parts::add);
            }

            MockPart[] arrayOfParts = parts.toArray(new MockPart[0]);

            var mockMvcRequestBuilder = multipart("/api/register/guarantorNaturalPerson/documentFinancial");

            if (arrayOfParts.length > 0) {
                mockMvcRequestBuilder.part(arrayOfParts);
            }

            mockMvcRequestBuilder.contentType(MediaType.MULTIPART_FORM_DATA_VALUE);

            ParameterizedTestHelper.runControllerTest(
                    mockMvc,
                    mockMvcRequestBuilder,
                    parameter
            );
        }

    }

    @Nested
    class DocumentResidencyTest {

        private record DocumentResidencyParameter(DocumentResidencyForm documentResidencyForm) {
        }

        static List<Arguments> provideDocumentResidencyParameter() {
            SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtTokenWithDossier = jwt().authorities(new SimpleGrantedAuthority("SCOPE_dossier"));

            return ArgumentBuilder.buildListOfArguments(
                    Pair.of("Should respond 401 when no jwt is passed",
                            new ControllerParameter<>(
                                    null,
                                    401,
                                    null,
                                    null,
                                    Collections.emptyList()
                            )
                    ),
                    Pair.of("Should respond 400 when document residency form wrong type",
                            new ControllerParameter<>(
                                    new DocumentResidencyParameter(Helper.invalidDocumentResidencyBecauseWrongType()),
                                    400,
                                    jwtTokenWithDossier,
                                    null,
                                    List.of(
                                            jsonPath("$.errors").isArray(),
                                            jsonPath("$.errors").value(hasItem("typeDocumentResidency: must be one of [TENANT, OWNER, GUEST, GUEST_COMPANY, GUEST_ORGANISM, SHORT_TERM_RENTAL, OTHER_RESIDENCY]"))
                                    )
                            )
                    ),
                    Pair.of("Should respond 400 when document residency form tenant step cateogory defined",
                            new ControllerParameter<>(
                                    new DocumentResidencyParameter(Helper.invalidDocumentResidencyForGuarantor()),
                                    400,
                                    jwtTokenWithDossier,
                                    null,
                                    List.of(
                                            jsonPath("$.errors").isArray(),
                                            jsonPath("$.errors").value(hasItem("categoryStep: For document sub category TENANT category step has to be null"))
                                    )
                            )
                    ),
                    Pair.of("Should respond 400 when document residency form no step",
                            new ControllerParameter<>(
                                    new DocumentResidencyParameter(Helper.invalidDocumentResidencyBecauseNoStepGuarantor()),
                                    400,
                                    jwtTokenWithDossier,
                                    null,
                                    List.of(
                                            jsonPath("$.errors").isArray(),
                                            jsonPath("$.errors").value(hasItem("categoryStep: For document sub category GUEST category step can not be null"))
                                    )
                            )
                    )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideDocumentResidencyParameter")
        void parameterizedTests(ControllerParameter<DocumentResidencyParameter> parameter) throws Exception {
            List<MockPart> parts = new ArrayList<>();
            if (parameter.getParameterData() != null) {
                var documentResidencyForm = parameter.getParameterData().documentResidencyForm;
                Field[] fields = documentResidencyForm.getClass().getDeclaredFields();
                Arrays.stream(fields).map(field -> {
                    field.setAccessible(true);
                    try {
                        if (field.get(documentResidencyForm) != null) {
                            return new MockPart(field.getName(), field.get(documentResidencyForm).toString().getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (IllegalAccessException e) {
                        System.err.println(e.getMessage());
                    }
                    return null;
                }).filter(Objects::nonNull).forEach(parts::add);
            }

            MockPart[] arrayOfParts = parts.toArray(new MockPart[0]);

            var mockMvcRequestBuilder = multipart("/api/register/guarantorNaturalPerson/documentResidency");

            if (arrayOfParts.length > 0) {
                mockMvcRequestBuilder.part(arrayOfParts);
            }

            mockMvcRequestBuilder.contentType(MediaType.MULTIPART_FORM_DATA_VALUE);

            ParameterizedTestHelper.runControllerTest(
                    mockMvc,
                    mockMvcRequestBuilder,
                    parameter
            );
        }

    }

}
